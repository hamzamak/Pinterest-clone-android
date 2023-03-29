package com.hamza.pinterest;

import static com.hamza.pinterest.Utils.Constant.BASE_URL;
import static com.hamza.pinterest.Utils.Constant.KEY_SHARED_JWT;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.hamza.pinterest.Calls.UserCall;
import com.hamza.pinterest.Utils.UserLoginModel;

import java.util.Calendar;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {
    Button loginBtn;
    TextView toSignUp;
    TextInputLayout textFieldEmailLogin, textFieldPasswordLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        loginBtn = findViewById(R.id.signinBtn);
        toSignUp = findViewById(R.id.textView_goToSignup);
        textFieldEmailLogin = findViewById(R.id.textFieldEmailLogin);
        textFieldPasswordLogin = findViewById(R.id.textFieldPasswordLogin);

        isJwtIsExpired();
        toSignUp.setOnClickListener(view -> {
            Intent i = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(i);

        });
        loginBtn.setOnClickListener(view -> {
            String email = textFieldEmailLogin.getEditText().getText().toString();
            String password = textFieldPasswordLogin.getEditText().getText().toString();
            if(email.isEmpty() || password.isEmpty()){
                Snackbar.make( loginBtn,"Email et Password should not be empty", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.danger_dark))
                        .setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white)).show();
                return;
            }
            login(email, password);
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        isJwtIsExpired(); // if you press the back button to return to  LoginActivity  call this method !!

    }

    private void isJwtIsExpired() {
        String token = getSharedPreferences(KEY_SHARED_JWT, MODE_PRIVATE)
                .getString("jwt" , null) ;
        if(token == null) return;
        JWT jwt = new JWT(token);
        Map<String, Claim> claims = jwt.getClaims();
        int expiration = claims.get("exp").asInt();
        long now = System.currentTimeMillis();
        int second = (int) (now / 1000) ;

        if (second > expiration) {
            // JWT has expired
        //    Toast.makeText(LoginActivity.this , "expiration :" + expiration + "  true " + second , Toast.LENGTH_LONG).show();
        } else {
            // JWT is still valid
            // Proceed with using the token
        //    Toast.makeText(LoginActivity.this , "expiration :" + expiration + "false " +second, Toast.LENGTH_LONG).show();
            Intent i = new Intent(LoginActivity.this, BottomTabActivity.class);
            startActivity(i);
        }


    }

    private void login(String email, String password) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        UserCall userCall = retrofit.create(UserCall.class);
        Call<String> getToken = userCall.signIn(new UserLoginModel(email,password)) ;

        getToken.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(!response.isSuccessful()){
                    if(response.code() == 400){
                        Snackbar.make( loginBtn,"Invalid Credential !", Snackbar.LENGTH_LONG)
                                .setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.danger))
                                .setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white)).show();

                    } else if (response.code() == 500) {
                        Snackbar.make( loginBtn,"No such email exists", Snackbar.LENGTH_LONG).
                                setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.danger))
                                .setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white)).show();
                    }
                }
                else {
                     signIn(response.body());
                   // Snackbar.make( loginBtn,"good  :" + response.body(), Snackbar.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Snackbar.make( loginBtn,"Erreur " +t.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });

    }

    private void signIn(String response) {
        SharedPreferences preferences = getSharedPreferences(KEY_SHARED_JWT, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("jwt" , response);
        editor.apply();

       Intent i = new Intent(LoginActivity.this, BottomTabActivity.class);
        startActivity(i);
    }

}