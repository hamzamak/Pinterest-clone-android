package com.hamza.pinterest.Calls;

import com.hamza.pinterest.Models.Comment;
import com.hamza.pinterest.Utils.CommentAdd;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CommentCall {

    @GET("comments/get/{id}")
    Call<List<Comment>> getPostComments (@Path("id") String id) ;

    @POST("comments/post")
    Call<Comment> addComment (@Body CommentAdd commentInfo) ;
}
