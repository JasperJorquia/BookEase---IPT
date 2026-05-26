package com.example.bookease.network;

import com.example.bookease.models.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    @POST("api/register")
    Call<MessageResponse> register(@Body RegisterRequest body);

    @POST("api/login")
    Call<LoginResponse> login(@Body LoginRequest body);


    @GET("api/profile")
    Call<User> getProfile();

    @PUT("api/profile")
    Call<MessageResponse> updateProfile(@Body UpdateProfileRequest body);

    @PUT("api/change-password")
    Call<MessageResponse> changePassword(@Body ChangePasswordRequest body);

    // ── Branches ────────────────────────────────────────────────────────────
    @GET("api/branches")
    Call<List<Branch>> getBranches(@Query("location") String location);

    // ── Services ────────────────────────────────────────────────────────────
    @GET("api/services")
    Call<List<Service>> getServices();

    // ── Appointments ────────────────────────────────────────────────────────
    @GET("api/appointments")
    Call<List<Appointment>> getAppointments(@Query("status") String status);

    @POST("api/appointments")
    Call<MessageResponse> createAppointment(@Body AppointmentRequest body);

    @DELETE("api/appointments/{id}")
    Call<MessageResponse> cancelAppointment(@Path("id") int id);

    @DELETE("api/appointments/clear")
    Call<MessageResponse> clearAppointments();

    // ── Stats ────────────────────────────────────────────────────────────────
    @GET("api/stats")
    Call<Stats> getStats();
}
