/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.UrlQuerySanitizer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.utilities.NetworkUtils;
import com.example.android.sunshine.utilities.OpenWeatherJsonUtils;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements ForecastAdapter.ForecastAdapterOnClickHandler, LoaderManager.LoaderCallbacks<String[]> , SharedPreferences.OnSharedPreferenceChangeListener {
    private RecyclerView recyclerView;
    private ForecastAdapter adapter;
    public static int LOADER_CONSTANT=22;
    String[] jsonAraay=null;
    String[] mFinalList=null;
    ProgressBar mProgressBar;
    private static  boolean PREF_HAS_BEEN_UPDATED=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        recyclerView=(RecyclerView)findViewById(R.id.recView);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(this);
        ((LinearLayoutManager) layoutManager).setOrientation(LinearLayoutManager.VERTICAL);
        ((LinearLayoutManager) layoutManager).setReverseLayout(false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter=new ForecastAdapter(this,this);
        recyclerView.setAdapter(adapter);
        mProgressBar=(ProgressBar)findViewById(R.id.progress);
        LoadWeatherData();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }
    private void openLocationInMap() {

        // COMPLETED (9) Use preferred location rather than a default location to display in the map
        String addressString = SunshinePreferences.getPreferredWeatherLocation(this);
        Uri geoLocation = Uri.parse("geo:0,0?q=" + addressString);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d("LOG_TAG", "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
        }
    }
    @NonNull
    @Override
    public Loader<String[]> onCreateLoader(int i, @Nullable final Bundle bundle) {
        @SuppressLint("StaticFieldLeak") AsyncTaskLoader<String[]> bundleList = new AsyncTaskLoader<String[]>(this) {
            @Override
            protected void onStartLoading() {
                if (mFinalList != null) {
                        deliverResult(mFinalList);
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }
            @Nullable
            @Override
            public String[] loadInBackground() {
                String json_resp = null;
                String[] jsonAraay;
                String location = SunshinePreferences.getPreferredWeatherLocation(MainActivity.this);
                URL weatherURL = NetworkUtils.buildUrl(location);
                try {
                    json_resp = NetworkUtils.getResponseFromHttpUrl(weatherURL);
                    jsonAraay = OpenWeatherJsonUtils.getSimpleWeatherStringsFromJson(MainActivity.this, json_resp);
                    return jsonAraay;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(@Nullable String[] data) {
                mFinalList=data;
                super.deliverResult(data);
            }
        };
        return bundleList;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String[]> loader, String[] s) {

        mProgressBar.setVisibility(View.INVISIBLE);
        if(s!=null&&s.length>0){
            recyclerView.setVisibility(View.VISIBLE);
            adapter.setWeatherData(s);
        }else if(s==null){
            recyclerView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String[]> loader) {

    }


    public void LoadWeatherData(){
      //  URL url=NetworkUtils.buildUrl(Double.valueOf(SunshinePreferences.PREF_COORD_LAT),Double.valueOf(SunshinePreferences.PREF_COORD_LONG));
       String location=SunshinePreferences.getPreferredWeatherLocation(this);
       // new FetchWeatherTask().execute(location);
        Bundle loaderBundle=null;
       // loaderBundle.putStringArray("BundleList",jsonAraay);
        LoaderManager manager=getSupportLoaderManager();
        Loader<String[]> loader=manager.getLoader(LOADER_CONSTANT);
        if (loader == null) {
        manager.initLoader(LOADER_CONSTANT,loaderBundle,this);
        }
        else{
            manager.restartLoader(LOADER_CONSTANT,loaderBundle,this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(PREF_HAS_BEEN_UPDATED){
            getSupportLoaderManager().restartLoader(LOADER_CONSTANT,null,this);
            PREF_HAS_BEEN_UPDATED=false;
        }

    }

    @Override
    public void onItemclick(String s) {
        Intent intent=new Intent(MainActivity.this,DetailedActivity.class);
        intent.putExtra("String",s);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.refresh_action:{
               adapter.setWeatherData(null);
                LoadWeatherData();
                return true;
            }
            case R.id.settings_action:{
                Intent intent=new Intent(MainActivity.this,Settings_activity.class);
                startActivity(intent);
            }
            default:return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    PREF_HAS_BEEN_UPDATED=true;
    }
}