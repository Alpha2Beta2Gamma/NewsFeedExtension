package com.example.newsfeed.newsassignment;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.newsfeed.newsassignment.api.NewsService;
import com.example.newsfeed.newsassignment.app.NewsFeedApplication;
import com.example.newsfeed.newsassignment.model.News;
import com.example.newsfeed.newsassignment.model.Result;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavouriteFragment extends Fragment implements RecyclerViewItemClickListener {


    public static final String FRAGMENT_TAG = "FavouriteFragment";
    private static final String TAG = "MainFragment";

    private FavouritesRecyclerViewAdapter adapter;
    private LinearLayoutManager linearLayoutManager;

    private RecyclerView rv;
    private ProgressBar progressBar;

    private List<Result> displayResults;




    /**
     * This listener is to communicate with the activity.
     */
    private MainFragment.showDetailsListener listener;


    interface showDetailsListener {

        void showDetails(Bundle arguments);

        void showTitle(boolean show);
    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        listener = (MainFragment.showDetailsListener) (context);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new FavouritesRecyclerViewAdapter(getActivity().getApplicationContext(), this);
        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);


        /*
         *Display results are checked here to handle screen rotations. Since the fragment is retained over screen rotation, these
         * variables will also be retained on screen rotation.
         */
        if (displayResults == null || displayResults.size() == 0) {

            displayResults= new ArrayList<>();
            SharedPreferences prefs = getActivity().getSharedPreferences(NewsFeedApplication.FAV_SHARED_PREF, Context.MODE_PRIVATE);
            Map<String,?> keys = prefs.getAll();

            for(Map.Entry<String,?> entry : keys.entrySet()){
                Log.d("map values",entry.getKey() + ": " +
                        entry.getValue().toString());
                Result res = new Gson().fromJson(entry.getValue().toString(), Result.class);
                displayResults.add(res);
            }


        }


    }


    @Override
    public void onResume() {
        super.onResume();


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View thisView = getLayoutInflater().inflate(R.layout.fragment_main, container, false);

        rv = thisView.findViewById(R.id.main_recycler);
        progressBar = thisView.findViewById(R.id.main_progress);

        setRetainInstance(true);

        rv.setLayoutManager(linearLayoutManager);

        rv.setItemAnimator(new DefaultItemAnimator());

        rv.setAdapter(adapter);


        /*
         * Check for existing results because they will be still existing in case of a screen rotation(OnCreateView will be called but
         * OnCreate will not be called)
         *
         */
        if (displayResults == null || displayResults.size() == 0) {

            displayResults =  new ArrayList<>();
            SharedPreferences prefs = getActivity().getSharedPreferences(NewsFeedApplication.FAV_SHARED_PREF, Context.MODE_PRIVATE);
            Map<String,?> keys = prefs.getAll();

            for(Map.Entry<String,?> entry : keys.entrySet()){
                Log.d("map values",entry.getKey() + ": " +
                        entry.getValue().toString());
                Result res = new Gson().fromJson(entry.getValue().toString(), Result.class);
                displayResults.add(res);
            }


        }
        adapter.clear();
        adapter.addAll(displayResults);

        getActivity().setTitle(getString(R.string.all_stories_title));
        listener.showTitle(true);

            progressBar.setVisibility(View.GONE);

        return thisView;
    }





    @Override
    public void onDetach() {

        adapter.clear();
        rv.setLayoutManager(null);

        rv.setItemAnimator(null);

        rv.setAdapter(null);

        super.onDetach();

    }

    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
        adapter.clear();
        rv.setLayoutManager(null);

        rv.setItemAnimator(null);

        rv.setAdapter(null);
        adapter = null;
        rv = null;
    }


    /*
     * This takes care of the click events on recycler view items.
     *
     */
    @Override
    public void onItemClick(View view, int position, boolean mainItem) {

        if(mainItem) {
            Bundle args = new Bundle();
            args.putString(DetailedItemFragment.Url, displayResults.get(position).getContent().getUrl());
            listener.showDetails(args);
        }else{

            SharedPreferences prefs = getActivity().getSharedPreferences(NewsFeedApplication.FAV_SHARED_PREF, Context.MODE_PRIVATE);
            String resultId = prefs.getString(displayResults.get(position).getId(), null);
            if(resultId == null){
                prefs.edit().putString(displayResults.get(position).getId(),new Gson().toJson(displayResults.get(position))).apply();
                view.setBackgroundColor(getActivity().getColor(R.color.colorAccent));
                ((TextView)view.findViewById(R.id.fav_text)).setText("Favourite");


            }else{
                prefs.edit().remove(displayResults.get(position).getId()).apply();
                view.setBackgroundColor(getActivity().getColor(R.color.placeholder));
                ((TextView)view.findViewById(R.id.fav_text)).setText("Favourite");
            }
        }
    }

    /*
     * The below APIs wil help detect network availability changes.
     *
     */
    private boolean isNetworkAvailable() {
        try {

            if(getActivity() == null){

                return  false;
            }
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            if(connectivityManager == null){
                return false;
            }
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }catch (Exception e){
            return false;
        }
    }





}
