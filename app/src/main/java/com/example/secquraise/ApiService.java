package com.example.secquraise;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("capture")
    Call<ApiResponse> captureData(@Body CaptureData capturedData);
}

