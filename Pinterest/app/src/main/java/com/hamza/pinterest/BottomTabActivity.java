package com.hamza.pinterest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.hamza.pinterest.Adapters.HomeAdapter;
import com.hamza.pinterest.Calls.PostCall;
import com.hamza.pinterest.Models.Post;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BottomTabActivity extends AppCompatActivity implements BottomNavigationView.OnItemSelectedListener {
    BottomNavigationView bottomNavigationView ;
    Home homeFragment = new Home();
    AddPin addPinFragment = new AddPin();
    Profil profilFragment = new Profil();

    private int selectedTabIndex = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_tab);
        bottomNavigationView= findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this);
        // Set the selected tab
        bottomNavigationView.setSelectedItemId(R.id.bottom_home);

        //when rotating device , persist the selected tab
        if (savedInstanceState != null) {
            // Restore the selected tab index
            selectedTabIndex = savedInstanceState.getInt("selectedTabIndex");
            bottomNavigationView.setSelectedItemId(selectedTabIndex);
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        selectedTabIndex = item.getItemId();
        Log.e("tagggggggg22222" , String.valueOf(selectedTabIndex));
        switch (item.getItemId()) {
            case R.id.bottom_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayoutContainer,homeFragment).commit();
                return true;
            case R.id.bottom_add:
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayoutContainer,addPinFragment).commit();
                return true;
            case R.id.bottom_profil:
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayoutContainer,profilFragment).commit();
                return true;
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selectedTabIndex", selectedTabIndex);
    }
}