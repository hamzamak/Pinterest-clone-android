package com.hamza.pinterest;

import static com.hamza.pinterest.Utils.Constant.BASE_URL;

import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Home#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Home extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ImageView empty_view;

    List<Post> postList = new ArrayList<>() ;

    HomeAdapter homeAdapter ;
    RecyclerView recyclerView_home ;

    SearchView seacrhView_home ;
    public Home() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Home.
     */
    // TODO: Rename and change types and number of parameters
    public static Home newInstance(String param1, String param2) {
        Home fragment = new Home();
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
    }

    @Override
    public void onStart() {
        super.onStart();
        Post deletedPost = (Post) getActivity().getIntent().getSerializableExtra("deletedPost");
        Post updatedPost = (Post) getActivity().getIntent().getSerializableExtra("updatedPost");
        if(deletedPost != null){
            homeAdapter.removeItem(deletedPost);
        }

        if(updatedPost != null){
            homeAdapter.updateItem(updatedPost);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView =inflater.inflate(R.layout.fragment_home, container, false);
        empty_view= rootView.findViewById(R.id.empty_view_home);
        //******************************************************************************************************************//
        seacrhView_home= rootView.findViewById(R.id.seacrhView_home);
        seacrhView_home.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                List<Post> filteredListPost = new ArrayList<>();
                for(Post postItem:postList){
                    if(postItem.getDescription().toLowerCase().contains(newText.toLowerCase())  ){
                        filteredListPost.add(postItem);
                    }
                    homeAdapter.filterList(filteredListPost);

                }
                recyclerEmptyState(filteredListPost);

                return true;
            }
        });
        //******************************************************************************************************************//
        recyclerView_home = rootView.findViewById(R.id.recycler_home);
        recyclerView_home.setLayoutManager(new StaggeredGridLayoutManager( 2 ,  LinearLayoutManager.VERTICAL));
        homeAdapter = new HomeAdapter(getContext() , postList);
        recyclerView_home.setAdapter(homeAdapter);
        recyclerEmptyState(postList);

        //******************************************************************************************************************//


        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        PostCall postCall = retrofit.create(PostCall.class);
        Call<List<Post>> posts = postCall.getPosts();

        posts.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {

                if(!response.isSuccessful()){
                    Toast.makeText(getContext() , "erreur fetching posts status "+ response.code() , Toast.LENGTH_SHORT).show();
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
               // return;
            }
        });

        // Inflate the layout for this fragment
        return rootView ;
    }

    private void recyclerEmptyState(List<Post> list) {
        if (list.isEmpty()) {
            recyclerView_home.setVisibility(View.GONE);
            empty_view.setVisibility(View.VISIBLE);
        }
        else {
            recyclerView_home.setVisibility(View.VISIBLE);
            empty_view.setVisibility(View.GONE);
        }
    }


}