package com.ahsailabs.dashboardinfo;


import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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

    //tools_avaibility_pic, team_avaibility_pic, team_avaibility, tools_avaibility, space_avaibility, progress_status

    @Multipart
    @POST("save_evidence")
    Call<SaveEvidenceResponse> saveEvidence(@Part MultipartBody.Part partFile1, @Part MultipartBody.Part partFile2,
                                            @Part("team_avaibility") RequestBody teamAvaibility,
                                            @Part("tools_avaibility") RequestBody toolsAvaibility,
                                            @Part("space_avaibility") RequestBody spaceAvaibility,
                                            @Part("progress_status") RequestBody progressAvaibility);
}
