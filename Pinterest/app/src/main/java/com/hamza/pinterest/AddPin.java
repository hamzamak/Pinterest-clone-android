package com.hamza.pinterest;

import static com.hamza.pinterest.Utils.Constant.BASE_URL;
import static com.hamza.pinterest.Utils.Constant.KEY_SHARED_JWT;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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
import android.widget.TextView;

import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.hamza.pinterest.Calls.PostCall;
import com.hamza.pinterest.Models.Post;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddPin#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddPin extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
   // private static final int PICK_IMAGE_REQUEST = 40 ;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    static  String base64 = "";

    private static boolean submit = false;
    private boolean isLauncherRegistered = false;

    Button pick_submit_post_button ;
    TextInputLayout textField_description ;
    TextView textView_post ;
    ImageView imageView_post ;
    private  ActivityResultLauncher<String> pickImageLauncher ;

    public AddPin() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddPin.
     */
    // TODO: Rename and change types and number of parameters
    public static AddPin newInstance(String param1, String param2) {
        AddPin fragment = new AddPin();
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
        submit = false ;
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    Picasso.get().load(uri).into(imageView_post);
                    imageView_post.setColorFilter(Color.TRANSPARENT);

                    pick_submit_post_button.setText("Submit");
                    submit = true ;
                    isLauncherRegistered = true;
                   base64 = convertUriToBase64(uri);
                });
    }



    private void openImagePicker() {
        if (pickImageLauncher != null) {
            pickImageLauncher.launch("image/*");

        }
     else {
        Log.e("MyFragment", "Attempted to launch an unregistered ActivityResultLauncher.");
    }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_add_pin, container, false);
        pick_submit_post_button = rootView.findViewById(R.id.pick_submit_post_button);
        textField_description = rootView.findViewById(R.id.textField_description);
        imageView_post = rootView.findViewById(R.id.imageView_post);
        textView_post = rootView.findViewById(R.id.textView_post);


       pick_submit_post_button.setOnClickListener(view -> {
          /*  Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);*/
           if (submit == false ){
               openImagePicker();

           }
           else {
               String token = getActivity().getSharedPreferences(KEY_SHARED_JWT, Context.MODE_PRIVATE)
                       .getString("jwt" , null) ;
               if(token == null) return;
               JWT jwt = new JWT(token);
               Map<String, Claim> claims = jwt.getClaims();
           //    String owner = claims.get("id").asString();
               Post post = new Post();
               post.setMedia("data:image/jpeg;base64,"+base64);
               String desc = textField_description.getEditText().getText().toString() ;
               if(desc == null) desc ="";
               post.setDescription(desc);
               //post.setOwner(owner);

               Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
               PostCall postCall = retrofit.create(PostCall.class);

               Call<Post> createPost = postCall.createPost(post , "Bearer "+token);
               createPost.enqueue(new Callback<Post>() {
                   @Override
                   public void onResponse(Call<Post> call, Response<Post> response) {
                       if(!response.isSuccessful()){
                           if(response.code() ==401 ){
                               Snackbar.make( pick_submit_post_button,"User is not Authenticated", Snackbar.LENGTH_LONG)
                                       .setBackgroundTint(ContextCompat.getColor(getContext(), R.color.danger_dark))
                                       .setTextColor(ContextCompat.getColor(getContext(), R.color.white)).show();
                           }
                           Snackbar.make( pick_submit_post_button,"error in creating A post !", Snackbar.LENGTH_LONG)
                                   .setBackgroundTint(ContextCompat.getColor(getContext(), R.color.danger_dark))
                                   .setTextColor(ContextCompat.getColor(getContext(), R.color.white)).show();
                           clear();
                       }

                       Snackbar.make( pick_submit_post_button,"created", Snackbar.LENGTH_LONG)
                               .setBackgroundTint(ContextCompat.getColor(getContext(), R.color.success))
                               .setTextColor(ContextCompat.getColor(getContext(), R.color.white)).show();
                       clear();

                       new Handler().postDelayed(() -> {
                           Intent i = new Intent(getActivity() , BottomTabActivity.class);
                           startActivity(i);
                       }, 1_000);
                   }

                   @Override
                   public void onFailure(Call<Post> call, Throwable t) {
                       Snackbar.make( pick_submit_post_button,"failure "+ t.getMessage(), Snackbar.LENGTH_LONG)
                               .setBackgroundTint(ContextCompat.getColor(getContext(), R.color.danger_dark))
                               .setTextColor(ContextCompat.getColor(getContext(), R.color.white)).show();
                       clear();
                   }
               });
           }


        });

        return rootView ;
    }

    private void clear() {
        pick_submit_post_button.setText("Pick an image");
        textField_description.getEditText().setText("");
        imageView_post.setImageResource(R.drawable.preview);
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isLauncherRegistered) {
            pickImageLauncher.unregister();
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