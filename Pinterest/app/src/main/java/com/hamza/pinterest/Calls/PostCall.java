package com.hamza.pinterest.Calls;

import com.hamza.pinterest.Models.Post;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface PostCall {
    @GET("posts/getAll")
    Call<List<Post>> getPosts () ;

    @GET("posts/get/{id}")
    Call<List<Post>> getPostsByUser (@Path("id") String id) ;

    @POST("posts/create")
    Call<Post> createPost (@Body Post post , @Header("Authorization") String authorization);

    @POST("posts/like/{id}")
    Call<Post> likePost (@Path("id") String id , @Header("Authorization") String authorization);

    @DELETE("posts/delete/{id}")
    Call<Post> deletePost (@Path("id") String id , @Header("Authorization") String authorization);

    @PUT("posts/update")
    Call<Post> updatePost ( @Body Post updatedPost, @Header("Authorization") String authorization);


}
