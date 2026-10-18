package com.example.mygameslibapk.laravelclient

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @Headers("Accept: application/json", "Content-Type: application/json")
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @Headers("Accept: application/json")
    @POST("logout")
    suspend fun logout(): Response<AuthResponse>

    @Headers("Accept: application/json")
    @GET("me")
    suspend fun getProfile(): Response<AuthResponse>

    @Headers("Accept: application/json")
    @GET("projects")
    suspend fun getProjects(): Response<ProjectListResponse>

    @Headers("Accept: application/json")
    @GET("projects/{id}")
    suspend fun getProjectDetails(@Path("id") id: Int): Response<ProjectDto>

    // Hire Positions & Applications
    @Headers("Accept: application/json")
    @GET("projects/{id}/vacancies")
    suspend fun getVacancies(@Path("id") projectId: Int): Response<ApiResponse<HirePositionDto>>

    @Headers("Accept: application/json")
    @POST("projects/{id}/vacancies")
    suspend fun createVacancy(
        @Path("id") projectId: Int,
        @Body request: CreatePositionRequest
    ): Response<ApiResponse<HirePositionDto>>

    @Headers("Accept: application/json")
    @POST("positions/{id}/apply")
    suspend fun applyForPosition(
        @Path("id") positionId: Int,
        @Body request: ApplyTicketRequest
    ): Response<ApiResponse<Unit>>

    @Headers("Accept: application/json")
    @GET("projects/{id}/vacancies/applicants")
    suspend fun getApplicants(@Path("id") projectId: Int): Response<ApiResponse<PositionTicketDto>>

    @Headers("Accept: application/json")
    @POST("tickets/{id}/review")
    suspend fun reviewTicket(
        @Path("id") ticketId: Int,
        @Body request: ReviewTicketRequest
    ): Response<ApiResponse<Unit>>
}

object ApiClient {
    private const val BASE_URL = "http://192.168.1.47:8000/api/"

    fun getService(context: Context): ApiService {
        val tokenManager = TokenManager(context)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()

                // Default headers for all outgoing requests
                requestBuilder.addHeader("Accept", "application/json")
                requestBuilder.addHeader("Content-Type", "application/json")

                // Automatically attach Sanctum Bearer token if stored
                tokenManager.getToken()?.let { token ->
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }

                chain.proceed(requestBuilder.build())
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}