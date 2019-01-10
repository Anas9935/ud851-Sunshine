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

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.utilities.NetworkUtils;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
TextView mWeatherTextView;
ProgressBar mProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        mWeatherTextView=(TextView)findViewById(R.id.tv_weather_data);
        mProgressBar=(ProgressBar)findViewById(R.id.progress);
        LoadWeatherData();
    }
    public class FetchWeatherTask extends AsyncTask<URL,Void,String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(URL... urls) {
            String json_resp=null;
            try{
                json_resp=NetworkUtils.getResponseFromHttpUrl(urls[0]);
            }catch (IOException e){
                e.printStackTrace();
            }
            return json_resp;
        }

        @Override
        protected void onPostExecute(String s) {
            mProgressBar.setVisibility(View.INVISIBLE);
            if(s!=null&&s.length()>0){
                mWeatherTextView.setText(s);
            }else if(s==null){
                mWeatherTextView.setText(R.string.error_message);
            }
        }
    }
    public void LoadWeatherData(){
        URL url=NetworkUtils.buildUrl(Double.valueOf(SunshinePreferences.PREF_COORD_LAT),Double.valueOf(SunshinePreferences.PREF_COORD_LONG));
        new FetchWeatherTask().execute(url);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.refresh_action:{
                mWeatherTextView.setText("");
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