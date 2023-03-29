package com.hamza.pinterest.Calls;

import com.hamza.pinterest.Utils.AvatarModel;
import com.hamza.pinterest.Utils.UserLoginModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserCall {
    @POST("users/signin")
    Call<String> signIn(@Body UserLoginModel user) ;

    @POST("users/signup")
    Call<String> signUp(@Body UserLoginModel user) ;

    @GET("users/{id}")
    Call<UserLoginModel> getPostByUserId(@Path("id") String id);

    @PUT("users/update/profil")
    Call<String> updateUserAvatar(@Body AvatarModel avatar , @Header("Authorization") String authorization);
}

