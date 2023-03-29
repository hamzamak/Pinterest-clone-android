package com.hamza.pinterest;

import static android.content.Context.MODE_PRIVATE;
import static com.hamza.pinterest.Utils.Constant.BASE_URL;
import static com.hamza.pinterest.Utils.Constant.KEY_SHARED_JWT;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.hamza.pinterest.Adapters.HomeAdapter;
import com.hamza.pinterest.Calls.PostCall;
import com.hamza.pinterest.Calls.UserCall;
import com.hamza.pinterest.Models.Post;
import com.hamza.pinterest.Utils.AvatarModel;
import com.hamza.pinterest.Utils.UserLoginModel;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Profil#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Profil extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    TextView textView_followDetails ,textView_username ;
    ImageView empty_view ;
    CircleImageView profile_image ;
    ImageButton imgButton_logout ,imageButton_editAvatar;
    private ActivityResultLauncher<String> pickImageLauncher ;
    RecyclerView recycler_profil ;
    List<Post> postList = new ArrayList<>() ;
    HomeAdapter homeAdapter ; // we can use the adapter also in Profil Fragment

    CircleImageView circleImageView_edit_profil ;
    static  String base64 = "";
    String token = null ;
    private boolean isLauncherRegistered = false;
    public Profil() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Profil.
     */
    // TODO: Rename and change types and number of parameters
    public static Profil newInstance(String param1, String param2) {
        Profil fragment = new Profil();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    Picasso.get().load(uri).into(circleImageView_edit_profil);
                    isLauncherRegistered = true;
                    base64 = convertUriToBase64(uri);
                });
    }

    private void openImagePicker() {
        if (pickImageLauncher != null) {
            pickImageLauncher.launch("image/*");
        }
        else {Log.e("MyFragment", "Attempted to launch an unregistered ActivityResultLauncher.");}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profil, container, false);

        //*******************************************************
         token =getActivity().getSharedPreferences(KEY_SHARED_JWT, MODE_PRIVATE)
                .getString("jwt" , null) ;


        //*****************************************************************************************
        empty_view= rootView.findViewById(R.id.empty_view);
        textView_username= rootView.findViewById(R.id.textView_username);
        profile_image = rootView.findViewById(R.id.profile_image);
        imageButton_editAvatar= rootView.findViewById(R.id.imageButton_editAvatar);

        //*****************************************************************************************
        // the logic of editing the avatar image of the profil
        imageButton_editAvatar.setOnClickListener(view -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
            builder.setTitle("Edit the profile");
            builder.setBackground(getResources().getDrawable(R.drawable.bg_dialog , null));
            View customView = getLayoutInflater().inflate(R.layout.custom_dialog, null);
            // Reference the Views in the custom dialog layout

            Button button_edit_profil = customView.findViewById(R.id.button_edit_profil);
            TextInputLayout textFieldLayout_url = customView.findViewById(R.id.textFieldLayout_url);
           // Button editButton = customView.findViewById(R.id.editButton);
            RadioGroup radioGroup = customView.findViewById(R.id.radioGroup);
            circleImageView_edit_profil = customView.findViewById(R.id.circleImageView_edit_profil);

            radioGroup.setOnCheckedChangeListener((radioGroup1, i) -> {
                if (i == R.id.radioButton1) {
                    textFieldLayout_url.setEnabled(false);
                    button_edit_profil.setEnabled(true);
                } else if (i == R.id.radioButton2){
                    // A different RadioButton is selected
                    button_edit_profil.setEnabled(false);
                    textFieldLayout_url.setEnabled(true);
                }
            });

            button_edit_profil.setOnClickListener(view12 -> openImagePicker());


            builder.setPositiveButton("Edit", (dialogInterface, i) -> {
                int selectedId = radioGroup.getCheckedRadioButtonId();
                AvatarModel avatarModel = new AvatarModel();
                Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
                UserCall userCall = retrofit.create(UserCall.class);
                Call<String> updateAvatar ;

                if (selectedId == R.id.radioButton1) {
                    avatarModel.setBase_64_avatar("data:image/jpeg;base64,"+base64);
                } else if (selectedId == R.id.radioButton2) {
                    avatarModel.setAvatarUrl(textFieldLayout_url.getEditText().getText().toString());
                }

                updateAvatar = userCall.updateUserAvatar(avatarModel , "Bearer "+token);
                updateAvatar.enqueue(new Callback<String>() {
                   @Override
                   public void onResponse(Call<String> call, Response<String> response) {
                       if (!response.isSuccessful()) {
                           if (response.code() == 401) {
                               Snackbar.make(recycler_profil, "User is not Authenticated", Snackbar.LENGTH_LONG)
                                       .setBackgroundTint(ContextCompat.getColor(getContext(), R.color.danger_dark))
                                       .setTextColor(ContextCompat.getColor(getContext(), R.color.white)).show();
                           }
                           else
                               Snackbar.make(recycler_profil, "error updating the profil !", Snackbar.LENGTH_LONG)
                                       .setBackgroundTint(ContextCompat.getColor(getContext(), R.color.danger_dark))
                                       .setTextColor(ContextCompat.getColor(getContext(), R.color.white)).show();
                       }
                       else {
                           SharedPreferences sharedPreferences = getActivity().getSharedPreferences(KEY_SHARED_JWT, MODE_PRIVATE);
                           SharedPreferences.Editor editor = sharedPreferences.edit();
                           editor.putString("jwt", response.body());
                           editor.apply();



                           Snackbar.make( recycler_profil,"profil updated !", Snackbar.LENGTH_LONG)
                                   .setBackgroundTint(ContextCompat.getColor(getContext(), R.color.success))
                                   .setTextColor(ContextCompat.getColor(getContext(), R.color.white)).show();
                       }
                   }

                   @Override
                   public void onFailure(Call<String> call, Throwable t) {

                   }
               });


            });
           builder.setOnDismissListener(dialogInterface -> {

               new Handler().postDelayed(() -> { // some delay added because when updating picture it may take some time (ms) then renew the jwt
                   if (isLauncherRegistered) {
                       pickImageLauncher.unregister();
                   }
                   token = getActivity().getSharedPreferences(KEY_SHARED_JWT, MODE_PRIVATE)
                           .getString("jwt" , null) ;

                   JWT jwt = new JWT(token);
                   Map<String, Claim> claims = jwt.getClaims();
                   String avatar = null;
                   try {
                       avatar = claims.get("avatar").asString();
                   }catch (Exception exception) {

                   }

                   if(avatar != null){
                       Picasso.get().load(avatar).placeholder(R.drawable.avatar).into(profile_image);
                   }

               },3000);
            });

            builder.setView(customView);
            builder.show();

        });


        JWT jwt = new JWT(token);
        Map<String, Claim> claims = jwt.getClaims();
        String username = "";
        String avatar = null;
        try {
            username = claims.get("username").asString();
            avatar = claims.get("avatar").asString(); // sometimes avatarUrl may be null  so claims.get("avatar") return null
        }catch (Exception exception) {

        }

        //*****************************************************************************************
        if(avatar != null){
            Picasso.get().load(avatar).placeholder(R.drawable.avatar).into(profile_image);
        }

        textView_username.setText(username);
        imgButton_logout= rootView.findViewById(R.id.imgButton_logout);
        imgButton_logout.setOnClickListener(view -> {
            SharedPreferences preferences = getActivity().getSharedPreferences(KEY_SHARED_JWT, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove("jwt");
            editor.apply();

            Intent i = new Intent(getActivity() , LoginActivity.class);
            startActivity(i);

        });

        //******************************************************************************************************************//
        recycler_profil = rootView.findViewById(R.id.recycler_profil);
        recycler_profil.setLayoutManager(new StaggeredGridLayoutManager( 2 ,  LinearLayoutManager.VERTICAL));
        homeAdapter = new HomeAdapter(getContext() , postList);
        recycler_profil.setAdapter(homeAdapter);

        recyclerEmptyState(postList);
        //******************************************************************************************************************//


        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        PostCall postCall = retrofit.create(PostCall.class);

        String userId = claims.get("id").asString();

        Call<List<Post>> posts = postCall.getPostsByUser(userId);

        posts.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {

                if(!response.isSuccessful()){
                    Toast.makeText(getContext() , "Erreur fetching posts for profil status "+ response.code() , Toast.LENGTH_SHORT).show();
                 return;
                }
                postList.clear();
                postList.addAll(response.body());
                homeAdapter.notifyDataSetChanged();
                recyclerEmptyState(postList);
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                Toast.makeText(getContext() , "onFailure fetching posts"+ t.getMessage()  , Toast.LENGTH_LONG).show();

            }
        });
        return rootView ;
    }

   /* @Override
    public void onStart() {
        super.onStart();
        token = getActivity().getSharedPreferences(KEY_SHARED_JWT, MODE_PRIVATE)
                .getString("jwt" , null) ;
        //   Log.e("ezadvbbb", token);
        JWT jwt = new JWT(token);
        Map<String, Claim> claims = jwt.getClaims();
        String avatar = null;
        try {
            avatar = claims.get("avatar").asString();
            Log.e("ezadvbbb", avatar);
        }catch (Exception exception) {

        }

        if(avatar != null){
            Picasso.get().load(avatar).placeholder(R.drawable.avatar).into(profile_image);
        }
    }*/

    private void recyclerEmptyState(List<Post> list) {
        if (list.isEmpty()) {
            recycler_profil.setVisibility(View.GONE);
            empty_view.setVisibility(View.VISIBLE);
        }
        else {
            recycler_profil.setVisibility(View.VISIBLE);
            empty_view.setVisibility(View.GONE);
        }
    }

    private String convertUriToBase64(Uri uri) {
        String base64 = "";
        try {
            // Retrieve the bitmap from the URI
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
            // Convert the bitmap to a byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            // Encode the byte array to a base64-encoded string
            base64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return base64;
    }
}