package com.ahsailabs.dashboardinfo;


import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by ahmad s on 29/03/22.
 */
public interface ApiService {
    @FormUrlEncoded
    @POST("login")
    Call<LoginResponse> login(@Field("user_name") String username, @Field("password") String password);

    @Multipart
    @POST("save_WL")
    Call<SaveWLResponse> saveWL(@Part MultipartBody.Part partFile);
}
