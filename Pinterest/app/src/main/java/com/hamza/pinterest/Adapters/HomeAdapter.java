package com.hamza.pinterest.Adapters;

import static com.hamza.pinterest.Utils.Constant.BASE_URL;
import static com.hamza.pinterest.Utils.Constant.KEY_SHARED_JWT;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;
import com.google.android.material.snackbar.Snackbar;
import com.hamza.pinterest.Calls.PostCall;
import com.hamza.pinterest.Models.Comment;
import com.hamza.pinterest.Models.Post;
import com.hamza.pinterest.PostDetailsActivity;
import com.hamza.pinterest.R;
import com.hamza.pinterest.ViewHolders.HomeViewHolder;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeAdapter extends RecyclerView.Adapter<HomeViewHolder> {
    Context context ;
    List<Post> postList = new ArrayList<>();

    public HomeAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HomeViewHolder(LayoutInflater.from(context).inflate(R.layout.home_recycler_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull HomeViewHolder holder, int position) {
        String token = context.getSharedPreferences(KEY_SHARED_JWT , Context.MODE_PRIVATE).getString("jwt" ,"");
        JWT jwt = new JWT(token);
        Map<String, Claim> claims = jwt.getClaims();
        String id = claims.get("id").asString();
        boolean isLiked = postList.get(position).getLikes().contains(id);
        // Set the like button icon based on whether the current user has liked the post or not
        if (isLiked) {
           Picasso.get().load(R.drawable.filled_heart_16).placeholder(R.drawable.progress_animation).into(holder.likeButtonIcon);
        } else {
            Picasso.get().load(R.drawable.heart_16).placeholder(R.drawable.progress_animation).into(holder.likeButtonIcon);
        }

        Picasso.get().load(postList.get(position).getMedia())
                .into(holder.imageView_item, new Callback() {
                    @Override
                    public void onSuccess() {
                        holder.circularProgress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        holder.circularProgress.setVisibility(View.GONE);
                    }
                });

        holder.textView_item_description.setText(postList.get(position).getDescription());

        holder.likeButtonIcon.setOnClickListener(view -> {
            Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()).build();
            PostCall postCall = retrofit.create(PostCall.class);
            Call<Post> post_response = postCall.likePost(postList.get(position).get_id() , "Bearer "+token);
            post_response.enqueue(new retrofit2.Callback<Post>() {
                @Override
                public void onResponse(Call<Post> call, Response<Post> response) {
                    if(!response.isSuccessful()){
                        Toast.makeText(context , "User is not Authenticated" , Toast.LENGTH_LONG);
                    }

                    postList.get(holder.getAdapterPosition()).setLikes(response.body().getLikes());
                    notifyDataSetChanged();

                }

                @Override
                public void onFailure(Call<Post> call, Throwable t) {
                    Toast.makeText(context , "failure " +t.getMessage() , Toast.LENGTH_LONG);
                }
            });
        });


        holder.card_home_container.setOnClickListener(view -> {
            Intent intent = new Intent(context , PostDetailsActivity.class);
            intent.putExtra("id" ,postList.get(position).get_id() ); // utile pour update or delete

            intent.putExtra("media" ,postList.get(position).getMedia() );
            intent.putExtra("likes" ,postList.get(position).getLikes().size() );

            intent.putExtra("createdAt" ,postList.get(position).getCreatedAt().toString() );
            intent.putExtra("description" ,postList.get(position).getDescription() );
            intent.putExtra("owner" ,postList.get(position).getOwner() );

          //  intent.putExtra("comments" , postList.get(position).getCommentList());

          //  System.out.println("dddzzz " +postList.get(position).getCommentList().size() );

            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void filterList(List<Post> filteredListPost) {
        postList = filteredListPost ;
        notifyDataSetChanged();
    }


    public void removeItem(Post deletedPost) {
        postList.removeIf(post -> post.get_id().equals(deletedPost.get_id()));
        notifyDataSetChanged();
    }

    public void updateItem(Post updatedPost) {
        int index = -1;
        for (int i = 0 ; i< postList.size() ; i ++) {
            if(postList.get(i).get_id().equals(updatedPost.get_id())){
                index = i ;
                break;
            }
        }
        if(index != -1){
            postList.set(index , updatedPost);
            notifyDataSetChanged();
        }

    }
}
