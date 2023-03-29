package com.hamza.pinterest;

import static com.hamza.pinterest.Utils.Constant.BASE_URL;
import static com.hamza.pinterest.Utils.Constant.KEY_SHARED_JWT;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.hamza.pinterest.Adapters.CommentAdapter;
import com.hamza.pinterest.Calls.CommentCall;
import com.hamza.pinterest.Calls.PostCall;
import com.hamza.pinterest.Calls.UserCall;
import com.hamza.pinterest.Models.Comment;
import com.hamza.pinterest.Models.Post;
import com.hamza.pinterest.Utils.CommentAdd;
import com.hamza.pinterest.Utils.UserLoginModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PostDetailsActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    ImageView imageView_post;
    TextView post_description, createdAt_post, textView_username_owner, textView_likes;
    CircleImageView profile_owner;
    ImageButton imgButton_menu, downloadButtonIcon,commentButtonIcon;
    List<Comment> listComments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel downloadChannel = new NotificationChannel("download_channel",
                    "Download Channel", NotificationManager.IMPORTANCE_LOW);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(downloadChannel);
        }

        setContentView(R.layout.activity_post_details);

        String token = getSharedPreferences(KEY_SHARED_JWT, Context.MODE_PRIVATE)
                .getString("jwt", null);
        if (token == null) return;
        JWT jwt = new JWT(token);
        Map<String, Claim> claims = jwt.getClaims();
        String userId = claims.get("id").asString();

        imageView_post = findViewById(R.id.imageView_post);
        post_description = findViewById(R.id.post_description);
        createdAt_post = findViewById(R.id.createdAt_post);
        textView_username_owner = findViewById(R.id.textView_username_owner);
        profile_owner = findViewById(R.id.profile_owner);
        textView_likes = findViewById(R.id.textView_likes);
        imgButton_menu = findViewById(R.id.imgButton_menu);
        downloadButtonIcon = findViewById(R.id.downloadButtonIcon);
        commentButtonIcon = findViewById(R.id.commentButtonIcon);

        post_description.setText(getIntent().getStringExtra("description"));
        createdAt_post.setText(getIntent().getStringExtra("createdAt"));
        Picasso.get().load(getIntent().getStringExtra("media")).into(imageView_post);
        imageView_post.setColorFilter(Color.TRANSPARENT);
        textView_likes.setText(String.valueOf(getIntent().getIntExtra("likes", 0)));
        String ownerOfPost = getIntent().getStringExtra("owner");







        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        UserCall userCall = retrofit.create(UserCall.class);

        Call<UserLoginModel> post = userCall.getPostByUserId(ownerOfPost);
        post.enqueue(new Callback<UserLoginModel>() {
            @Override
            public void onResponse(Call<UserLoginModel> call, Response<UserLoginModel> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(PostDetailsActivity.this, "erreur getting post status " + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }
                textView_username_owner.setText(response.body().getUsername());

                if (response.body().getAvatarUrl() != null) {
                    Picasso.get().load(response.body().getAvatarUrl()).placeholder(R.drawable.avatar).into(profile_owner);
                }


            }

            @Override
            public void onFailure(Call<UserLoginModel> call, Throwable t) {

            }
        });

        if (ownerOfPost.equals(userId)) {
            imgButton_menu.setVisibility(View.VISIBLE);
        } else {
            imgButton_menu.setVisibility(View.GONE);
        }

        imgButton_menu.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(PostDetailsActivity.this, imgButton_menu);
            popupMenu.setOnMenuItemClickListener(this);
            popupMenu.inflate(R.menu.action_post_menu);
            popupMenu.show();
        });


        //handle downloading the image
        downloadButtonIcon.setOnClickListener(view -> {
            Toast.makeText(PostDetailsActivity.this , "Started downloading the image" , Toast.LENGTH_SHORT).show();
            downloadImage(getIntent().getStringExtra("media"));
            Toast.makeText(PostDetailsActivity.this , "Image downloaded successfully " , Toast.LENGTH_SHORT).show();
        });


        //handle comments
        commentButtonIcon.setOnClickListener(view -> {

            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                    PostDetailsActivity.this);
            View bottomSheetView = LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.comments_bottom_sheet, null);

            RecyclerView recycler_comments = bottomSheetView.findViewById(R.id.recycler_comments);


             LinearLayoutManager linearLayoutManager = new LinearLayoutManager(bottomSheetDialog.getContext());
             linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
             recycler_comments.setLayoutManager(linearLayoutManager);
             CommentAdapter commentAdapter = new CommentAdapter(PostDetailsActivity.this , listComments);
             recycler_comments.setAdapter(commentAdapter);


            // GET the comments  by id post
            CommentCall commentCall = retrofit.create(CommentCall.class);
            Call<List<Comment>> getComments = commentCall.getPostComments(  getIntent().getStringExtra("id"));

            getComments.enqueue(new Callback<List<Comment>>() {
                @Override
                public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                    if (!response.isSuccessful()) {
                        Toast.makeText(PostDetailsActivity.this, "erreur getting comments  status " + response.code(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    listComments.clear();
                    listComments.addAll(response.body());
                    commentAdapter.notifyDataSetChanged();
                    recyclerEmptyState(listComments);

                }

                @Override
                public void onFailure(Call<List<Comment>> call, Throwable t) {

                }
            });


            bottomSheetDialog.setContentView(bottomSheetView);

            EditText edit_text_comment = bottomSheetView.findViewById(R.id.edit_text_comment);
            ImageView imgSend   = bottomSheetView.findViewById(R.id.imgSend);

            imgSend.setOnClickListener(view1 -> {
                   String comment = edit_text_comment.getText().toString();
                   if(comment.isEmpty()) {
                       Toast.makeText(bottomSheetView.getContext(),"the comment input is empty !!",Toast.LENGTH_SHORT).show();
                   }
                   else {
                       CommentAdd newComment = new CommentAdd();
                       newComment.setPostId(getIntent().getStringExtra("id"));
                       newComment.setContent(comment);
                       newComment.setUserId(userId);

                       CommentCall commentCall1 = retrofit.create(CommentCall.class);
                       Call<Comment> addComment = commentCall1.addComment(newComment);

                       addComment.enqueue(new Callback<Comment>() {
                           @Override
                           public void onResponse(Call<Comment> call, Response<Comment> response) {
                               if (!response.isSuccessful()) {
                                   Toast.makeText(bottomSheetView.getContext(), "Erreur posting comments  status " + response.code(), Toast.LENGTH_SHORT).show();
                                   return;
                               }
                               // Dismiss the keyboard when the button is clicked
                               InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                               imm.hideSoftInputFromWindow(edit_text_comment.getWindowToken(), 0);


                               listComments.add(0 ,response.body());
                               commentAdapter.notifyDataSetChanged();
                               edit_text_comment.setText("");

                           }

                           @Override
                           public void onFailure(Call<Comment> call, Throwable t) {

                           }
                       });


                   }
            });


            BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from((View) bottomSheetView.getParent());
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            CoordinatorLayout coordinatorLayout = bottomSheetDialog.findViewById(R.id.modalBottomSheetContainer);
            assert coordinatorLayout != null ;
            coordinatorLayout.setMinimumHeight(Resources.getSystem().getDisplayMetrics().heightPixels);
            bottomSheetDialog.show();


        });

    }

    private void recyclerEmptyState(List<Comment> list) {
        if (list.isEmpty()) {
          /*  recyclerView_home.setVisibility(View.GONE);
            empty_view.setVisibility(View.VISIBLE);*/
        }
        else {
          /*  recyclerView_home.setVisibility(View.VISIBLE);
            empty_view.setVisibility(View.GONE);*/
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        String id = getIntent().getStringExtra("id");
        String token = getSharedPreferences(KEY_SHARED_JWT, Context.MODE_PRIVATE)
                .getString("jwt", null);
        switch (menuItem.getItemId()) {
            case R.id.update:
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(PostDetailsActivity.this);
                builder.setIcon(R.drawable.edit)
                        .setTitle("Updating the post");

                builder.setBackground(getResources().getDrawable(R.drawable.bg_dialog, null));
                View customView = getLayoutInflater().inflate(R.layout.custom_update_post_dialog, null);
                TextInputLayout textFieldLayout_description = customView.findViewById(R.id.textFieldLayout_description);


                builder.setNegativeButton("No", (dialogInterface, i) -> {

                        })
                        .setPositiveButton("Update", (dialogInterface, i) -> {
                            String description = textFieldLayout_description.getEditText().getText().toString();
                            updatePost(id, description, token);
                        });
                builder.setView(customView);
                builder.show();

                return true;
            case R.id.delete:
                //confirm message 📣 📢 📢 📢 📢   here
                MaterialAlertDialogBuilder builder2 = new MaterialAlertDialogBuilder(PostDetailsActivity.this);
                builder2.setMessage("Waiting for Confirmation")
                        .setIcon(R.drawable.warning)
                        .setTitle("Deleting the post")
                        .setNegativeButton("No", (dialogInterface, i) -> {

                        })
                        .setPositiveButton("Confirm", (dialogInterface, i) -> {
                            deletePost(id, token);
                        })
                        .show();


                return true;

            default:
                return false;


        }
    }

    private void updatePost(String id, String description, String token) {
        Post post = new Post();
        post.set_id(id);
        if (description == null) description = "";
        post.setDescription(description);
        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        PostCall postCall = retrofit.create(PostCall.class);
        Call<Post> updatePost = postCall.updatePost(post, "Bearer " + token);
        updatePost.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if (!response.isSuccessful()) {
                    if (response.code() == 401) {
                        Snackbar.make(textView_likes, "User is not Authenticated", Snackbar.LENGTH_LONG)
                                .show();
                    } else
                        Snackbar.make(textView_likes, "error updating the post description !", Snackbar.LENGTH_LONG)
                                .show();
                } else {
                    Intent i = new Intent(PostDetailsActivity.this, BottomTabActivity.class);
                    i.putExtra("updatedPost", response.body()); // we send this to home to be able to filter the data postList
                    startActivity(i);    // and handle it in onStart on Home class

                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {

            }
        });
    }

    private void deletePost(String id, String token) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        PostCall postCall = retrofit.create(PostCall.class);
        Call<Post> post = postCall.deletePost(id, "Bearer " + token);

        post.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {

                Intent i = new Intent(PostDetailsActivity.this, BottomTabActivity.class);
                i.putExtra("deletedPost", response.body()); // we send this to home to be able to filter the data postList
                startActivity(i);    // and handle it in onStart on Home class

            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {

            }
        });

    }

    private void downloadImage(String url) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DCIM, "image.jpg");

        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);

    }

}