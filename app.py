from flask import Flask, request, jsonify
from flask_cors import CORS
import mysql.connector
import bcrypt
import jwt
import datetime
from functools import wraps
import os
import logging

app = Flask(__name__)   
CORS(app)
app.config['SECRET_KEY'] = 'bookease_secret_key_ipt102'

# logging
logging.basicConfig(level=logging.DEBUG)

# ─── DB CONFIG ────────────────────────────────────────────────────────────────
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': '',
    'database': 'bookease_db'
}

def get_db():
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        logging.debug('Connected to MySQL: %s@%s/%s', DB_CONFIG['user'], DB_CONFIG['host'], DB_CONFIG['database'])
        return conn
    except mysql.connector.Error as e:
        logging.exception('MySQL connection failed')
        raise

# ─── JWT DECORATOR ────────────────────────────────────────────────────────────
def token_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = request.headers.get('Authorization', '').replace('Bearer ', '')
        if not token:
            return jsonify({'error': 'Token missing'}), 401
        try:
            data = jwt.decode(token, app.config['SECRET_KEY'], algorithms=['HS256'])
            request.user_id = data['user_id']
        except:
            return jsonify({'error': 'Invalid token'}), 401
        return f(*args, **kwargs)
    return decorated

# ─── AUTH ─────────────────────────────────────────────────────────────────────
@app.route('/api/register', methods=['POST'])
def register():
    data = request.get_json()
    name = data.get('name')
    email = data.get('email')
    password = data.get('password')
    if not all([name, email, password]):
        return jsonify({'error': 'All fields required'}), 400
    hashed = bcrypt.hashpw(password.encode(), bcrypt.gensalt())
    db = get_db(); cur = db.cursor()
    try:
        cur.execute("INSERT INTO users (name, email, password) VALUES (%s,%s,%s)",
                    (name, email, hashed.decode()))
        db.commit()
        return jsonify({'message': 'Account created successfully'}), 201
    except mysql.connector.IntegrityError:
        return jsonify({'error': 'Email already registered'}), 409
    finally:
        cur.close(); db.close()

@app.route('/api/login', methods=['POST'])
def login():
    data = request.get_json()
    email = data.get('email')
    password = data.get('password')
    db = get_db(); cur = db.cursor(dictionary=True)
    cur.execute("SELECT * FROM users WHERE email=%s", (email,))
    user = cur.fetchone(); cur.close(); db.close()
    if not user or not bcrypt.checkpw(password.encode(), user['password'].encode()):
        return jsonify({'error': 'Invalid credentials'}), 401
    token = jwt.encode({
        'user_id': user['id'],
        'exp': datetime.datetime.utcnow() + datetime.timedelta(days=7)
    }, app.config['SECRET_KEY'], algorithm='HS256')
    return jsonify({'token': token, 'name': user['name'], 'email': user['email'],
                    'id': user['id'], 'location': user['location']})

@app.route('/api/profile', methods=['GET'])
@token_required
def get_profile():
    db = get_db(); cur = db.cursor(dictionary=True)
    cur.execute("SELECT id, name, email, location FROM users WHERE id=%s", (request.user_id,))
    user = cur.fetchone(); cur.close(); db.close()
    return jsonify(user)

@app.route('/api/profile', methods=['PUT'])
@token_required
def update_profile():
    data = request.get_json()
    db = get_db(); cur = db.cursor()
    cur.execute("UPDATE users SET name=%s, location=%s WHERE id=%s",
                (data.get('name'), data.get('location'), request.user_id))
    db.commit(); cur.close(); db.close()
    return jsonify({'message': 'Profile updated'})

@app.route('/api/change-password', methods=['PUT'])
@token_required
def change_password():
    data = request.get_json()
    db = get_db(); cur = db.cursor(dictionary=True)
    cur.execute("SELECT password FROM users WHERE id=%s", (request.user_id,))
    user = cur.fetchone()
    if not bcrypt.checkpw(data['old_password'].encode(), user['password'].encode()):
        cur.close(); db.close()
        return jsonify({'error': 'Current password incorrect'}), 400
    hashed = bcrypt.hashpw(data['new_password'].encode(), bcrypt.gensalt())
    cur.execute("UPDATE users SET password=%s WHERE id=%s", (hashed.decode(), request.user_id))
    db.commit(); cur.close(); db.close()
    return jsonify({'message': 'Password changed'})

# ─── BRANCHES ─────────────────────────────────────────────────────────────────
@app.route('/api/branches', methods=['GET'])
@token_required
def get_branches():
    location = request.args.get('location', '')
    db = get_db(); cur = db.cursor(dictionary=True)
    if location:
        cur.execute("SELECT * FROM branches WHERE location LIKE %s", (f'%{location}%',))
    else:
        cur.execute("SELECT * FROM branches")
    branches = cur.fetchall(); cur.close(); db.close()
    return jsonify(branches)

# ─── SERVICES ─────────────────────────────────────────────────────────────────
@app.route('/api/services', methods=['GET'])
@token_required
def get_services():
    db = get_db(); cur = db.cursor(dictionary=True)
    cur.execute("SELECT * FROM services")
    services = cur.fetchall(); cur.close(); db.close()
    return jsonify(services)

# ─── APPOINTMENTS ─────────────────────────────────────────────────────────────
@app.route('/api/appointments', methods=['GET'])
@token_required
def get_appointments():
    status = request.args.get('status', '')
    db = get_db(); cur = db.cursor(dictionary=True)
    query = """SELECT a.*, b.name AS branch_name, b.address AS branch_address,
               s.name AS service_name FROM appointments a
               JOIN branches b ON a.branch_id = b.id
               JOIN services s ON a.service_id = s.id
               WHERE a.user_id = %s"""
    params = [request.user_id]
    if status and status != 'all':
        query += " AND a.status = %s"
        params.append(status)
    query += " ORDER BY a.created_at DESC"
    cur.execute(query, params)
    appts = cur.fetchall()
    for a in appts:
        if a.get('appointment_date'):
            a['appointment_date'] = str(a['appointment_date'])
        if a.get('created_at'):
            a['created_at'] = str(a['created_at'])
    cur.close(); db.close()
    return jsonify(appts)

@app.route('/api/appointments', methods=['POST'])
@token_required
def create_appointment():
    data = request.get_json()
    db = get_db(); cur = db.cursor()
    cur.execute("""INSERT INTO appointments
                   (user_id, branch_id, service_id, appointment_date, appointment_time, notes, status)
                   VALUES (%s,%s,%s,%s,%s,%s,'confirmed')""",
                (request.user_id, data['branch_id'], data['service_id'],
                 data['appointment_date'], data['appointment_time'], data.get('notes', '')))
    db.commit()
    appt_id = cur.lastrowid
    cur.close(); db.close()
    return jsonify({'message': 'Appointment booked', 'id': appt_id}), 201

@app.route('/api/appointments/<int:appt_id>', methods=['DELETE'])
@token_required
def cancel_appointment(appt_id):
    db = get_db(); cur = db.cursor()
    cur.execute("UPDATE appointments SET status='cancelled' WHERE id=%s AND user_id=%s",
                (appt_id, request.user_id))
    db.commit(); cur.close(); db.close()
    return jsonify({'message': 'Appointment cancelled'})

@app.route('/api/appointments/clear', methods=['DELETE'])
@token_required
def clear_appointments():
    db = get_db(); cur = db.cursor()
    cur.execute("DELETE FROM appointments WHERE user_id=%s", (request.user_id,))
    db.commit(); cur.close(); db.close()
    return jsonify({'message': 'History cleared'})

# ─── STATS ────────────────────────────────────────────────────────────────────
@app.route('/api/stats', methods=['GET'])
@token_required
def get_stats():
    db = get_db(); cur = db.cursor(dictionary=True)
    cur.execute("SELECT COUNT(*) AS total FROM appointments WHERE user_id=%s AND status='confirmed'",
                (request.user_id,))
    confirmed = cur.fetchone()['total']
    cur.execute("SELECT COUNT(*) AS total FROM appointments WHERE user_id=%s AND status='cancelled'",
                (request.user_id,))
    recent = cur.fetchone()['total']
    cur.execute("SELECT COUNT(*) AS total FROM branches")
    branches = cur.fetchone()['total']
    cur.close(); db.close()
    return jsonify({'current_appointments': confirmed, 'recent_appointments': recent,
                    'branches_available': branches})


@app.route('/api/db-test', methods=['GET'])
def db_test():
    """Simple endpoint to verify DB connectivity and server can query MySQL."""
    try:
        db = get_db(); cur = db.cursor()
        cur.execute('SELECT VERSION()')
        version = cur.fetchone()[0]
        cur.close(); db.close()
        return jsonify({'status': 'ok', 'mysql_version': version})
    except Exception as e:
        logging.exception('DB test failed')
        return jsonify({'status': 'error', 'error': str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)
