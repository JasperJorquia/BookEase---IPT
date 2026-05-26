    package com.example.bookease.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ApiClient {
    private static final String BASE_URL = "http://192.168.1.4:5000/";
    private static Retrofit retrofit = null;
    private static String authToken = "";

    public static void setToken(String token) {
        authToken = token;
        retrofit = null;
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder builder = original.newBuilder()
                        .header("Authorization", "Bearer " + authToken)
                        .header("Content-Type", "application/json");
                    return chain.proceed(builder.build());
                }).build();

            retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }
}
