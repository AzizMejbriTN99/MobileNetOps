package com.mejbri.netopssynchromobile.network;

import com.mejbri.netopssynchromobile.model.*;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;
import java.util.Map;

public interface TechnicianApi {

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest body);

    @GET("api/technician/demandes")
    Call<List<Demande>> getMyDemandes();

    @GET("api/technician/demandes/{id}")
    Call<Demande> getDemande(@Path("id") long id);

    @PATCH("api/technician/demandes/{id}/status")
    Call<Demande> updateStatus(@Path("id") long id, @Body Map<String, String> body);

    @POST("api/technician/demandes/{id}/actions")
    Call<DemandeAction> addAction(@Path("id") long id, @Body Map<String, String> body);

    @GET("api/technician/demandes/{id}/timeline")
    Call<List<DemandeAction>> getTimeline(@Path("id") long id);

    @Multipart
    @POST("api/technician/demandes/{id}/photos")
    Call<Map<String, Object>> uploadPhoto(@Path("id") long id, @Part MultipartBody.Part photo);

    @GET("api/technician/demandes/{id}/photos")
    Call<List<PhotoInfo>> getPhotos(@Path("id") long id);

    @POST("api/technician/location")
    Call<Map<String, String>> updateLocation(@Body LocationUpdate body);

    @GET("api/technician/resources")
    Call<List<TechnicianResource>> getResources();

    @POST("api/technician/resources")
    Call<Map<String, String>> addResource(@Body Map<String, Object> body);

    @DELETE("api/technician/resources/{id}")
    Call<Map<String, String>> deleteResource(@Path("id") long id);

    // Profile
    @GET("api/profile")
    Call<ProfileResponse> getProfile();

    @PUT("api/profile")
    Call<ProfileResponse> updateProfile(
            @Body ProfileUpdateRequest body
    );

    @Multipart
    @POST("api/profile/avatar")
    Call<ProfileResponse> uploadAvatar(
            @Part MultipartBody.Part file
    );

    @GET("api/profile/avatar")
    Call<okhttp3.ResponseBody> getAvatar();

    @DELETE("api/profile/avatar")
    Call<ProfileResponse> deleteAvatar();



    // Notifications
    @GET("api/notifications")
    Call<List<Notification>> getNotifications();

    @GET("api/notifications/unread-count")
    Call<Map<String, Long>> getUnreadCount();

    @PATCH("api/notifications/mark-all-read")
    Call<Map<String,String>> markAllRead();

    @PATCH("api/notifications/{id}/read")
    Call<Map<String,String>> markOneRead(
            @Path("id") long id
    );
}

