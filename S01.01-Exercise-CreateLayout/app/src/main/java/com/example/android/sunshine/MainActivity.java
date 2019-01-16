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

import android.content.Intent;
import android.net.UrlQuerySanitizer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

public class MainActivity extends AppCompatActivity implements ForecastAdapter.ForecastAdapterOnClickHandler {
    private RecyclerView recyclerView;
    private ForecastAdapter adapter;

ProgressBar mProgressBar;
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
    }
    public class FetchWeatherTask extends AsyncTask<String,Void,String[]>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String[] doInBackground(String...locs) {
            String json_resp=null;
            String[] jsonAraay;
            String location=null;
            if(locs.length>0){
                location=locs[0];
            }
            URL weatherURL=NetworkUtils.buildUrl(location);
            try{
                json_resp=NetworkUtils.getResponseFromHttpUrl(weatherURL);
                jsonAraay=OpenWeatherJsonUtils.getSimpleWeatherStringsFromJson(MainActivity.this,json_resp);
                return jsonAraay;
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(String[] s) {
            mProgressBar.setVisibility(View.INVISIBLE);
            if(s!=null&&s.length>0){
                recyclerView.setVisibility(View.VISIBLE);
                adapter.setWeatherData(s);
            }else if(s==null){
                recyclerView.setVisibility(View.INVISIBLE);
            }
        }
    }
    public void LoadWeatherData(){
      //  URL url=NetworkUtils.buildUrl(Double.valueOf(SunshinePreferences.PREF_COORD_LAT),Double.valueOf(SunshinePreferences.PREF_COORD_LONG));
       String location=SunshinePreferences.getPreferredWeatherLocation(this);
        new FetchWeatherTask().execute(location);
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
            default:return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }
}