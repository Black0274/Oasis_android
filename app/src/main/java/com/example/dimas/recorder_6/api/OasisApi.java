package com.example.dimas.recorder_6.api;

import com.example.dimas.recorder_6.PostRes;
import com.example.dimas.recorder_6.RegistrationBody;
import com.example.dimas.recorder_6.RegistrationResponse;
import com.example.dimas.recorder_6.SearchBody;
import com.example.dimas.recorder_6.SearchResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OasisApi {
    //@FormUrlEncoded
   // @Headers("Content-Type")
    @POST("{choose}")
    Call<RegistrationResponse> registerUser(@Path("choose") String choose, @Body RegistrationBody registrationBody);
                                      //      @Header("login") String login,
                                       //     @Header("password") String password,
                                        //    @Header("lang") String lang);
    /*Call<RegistrationResponse> registerUser(
            @Field("login") String Login,
            @Field("password") String Password,
            @Field("lang") String Lang);*/

    @Multipart
    @POST("search_audio")
    Call<SearchResponse> search_audio(@Part("token") RequestBody token,      //@Query("token") String token,      //@Part("description") RequestBody description,
                                      @Part MultipartBody.Part file);
}
