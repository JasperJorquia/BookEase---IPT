package com.example.bookease.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "BookEaseSession";
    private static final String KEY_TOKEN    = "token";
    private static final String KEY_USER_ID  = "user_id";
    private static final String KEY_NAME     = "name";
    private static final String KEY_EMAIL    = "email";
    private static final String KEY_LOCATION = "location";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context ctx) {
        prefs  = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveSession(String token, int id, String name, String email, String location) {
        editor.putString(KEY_TOKEN, token);
        editor.putInt(KEY_USER_ID, id);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_LOCATION, location != null ? location : "Davao City");
        editor.apply();
    }

    public boolean isLoggedIn()    { return prefs.getString(KEY_TOKEN, null) != null; }
    public String getToken()       { return prefs.getString(KEY_TOKEN, ""); }
    public int getUserId()         { return prefs.getInt(KEY_USER_ID, -1); }
    public String getName()        { return prefs.getString(KEY_NAME, ""); }
    public String getEmail()       { return prefs.getString(KEY_EMAIL, ""); }
    public String getLocation()    { return prefs.getString(KEY_LOCATION, "Davao City"); }

    public void updateName(String name)         { editor.putString(KEY_NAME, name).apply(); }
    public void updateLocation(String location) { editor.putString(KEY_LOCATION, location).apply(); }

    public void logout() { editor.clear().apply(); }
}
