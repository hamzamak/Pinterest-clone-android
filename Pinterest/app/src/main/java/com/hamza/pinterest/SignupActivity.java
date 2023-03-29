package com.hamza.pinterest;

import static com.hamza.pinterest.Utils.Constant.BASE_URL;
import static com.hamza.pinterest.Utils.Constant.KEY_SHARED_JWT;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.hamza.pinterest.Calls.UserCall;
import com.hamza.pinterest.Utils.UserLoginModel;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SignupActivity extends AppCompatActivity {
    TextView toSignIn ;
    TextInputLayout textField_RePassword_SignUp ,textField_username ,textField_Email_SignUp ,textField_Password_SignUp ;
    Button signupBtn ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        toSignIn = findViewById(R.id.textView_goToSignin);
        textField_RePassword_SignUp = findViewById(R.id.textField_RePassword_SignUp);
        textField_username = findViewById(R.id.textField_username);
        textField_Email_SignUp = findViewById(R.id.textField_Email_SignUp);
        textField_Password_SignUp = findViewById(R.id.textField_Password_SignUp);
        signupBtn = findViewById(R.id.signupBtn);


        toSignIn.setOnClickListener(view -> {
            Intent i = new Intent(SignupActivity.this , LoginActivity.class);
            startActivity(i);

        });

        signupBtn.setOnClickListener(view -> {
            String email = textField_Email_SignUp.getEditText().getText().toString();
            String username = textField_username.getEditText().getText().toString();
            String password = textField_Password_SignUp.getEditText().getText().toString();
            String re_password = textField_RePassword_SignUp.getEditText().getText().toString();

            if(email.isEmpty() || password.isEmpty() || username.isEmpty() || re_password.isEmpty()){
                Snackbar.make( signupBtn,"make sure to fill all the text inputs given", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.danger_dark))
                        .setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white)).show();
                return;
            } else if (!password.equals(re_password)) {
                Snackbar.make( signupBtn,"password is not matching the re-Password", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.warning))
                        .setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white)).show();
                return;
            }

            signup(username ,email, password);
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
           // Toast.makeText(LoginActivity.this , "expiration :" + expiration + "  true " + second , Toast.LENGTH_LONG).show();
        } else {
            // JWT is still valid
            // Proceed with using the token
          //  Toast.makeText(SignupActivity.this , "from siup expiration :" + expiration + "false " +second, Toast.LENGTH_LONG).show();
            Intent i = new Intent(SignupActivity.this, BottomTabActivity.class);
            startActivity(i);
        }
    }

    private void signup(String username, String email, String password) {

        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        UserCall userCall = retrofit.create(UserCall.class);
        Call<String> getToken = userCall.signUp(new UserLoginModel(email,password,username)) ;
        getToken.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(!response.isSuccessful()){
                     if (response.code() == 500) {
                        Snackbar.make( signupBtn,"try another email or username", Snackbar.LENGTH_LONG).
                                setBackgroundTint(ContextCompat.getColor(getApplicationContext(), R.color.danger_dark))
                                .setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white)).show();
                    }
                }
                else {
                    onSuccessSignUp(response.body());
                    // Snackbar.make( loginBtn,"good  :" + response.body(), Snackbar.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Snackbar.make( signupBtn,"Erreur " +t.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void onSuccessSignUp(String response) {
        SharedPreferences preferences = getSharedPreferences(KEY_SHARED_JWT, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("jwt" , response);
        editor.apply();

        Intent i = new Intent(SignupActivity.this, BottomTabActivity.class);
        startActivity(i);
    }
}