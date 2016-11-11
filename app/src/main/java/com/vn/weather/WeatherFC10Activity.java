package com.vn.weather;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.gson.Gson;
import com.vn.weather.adapter.Forecast10dAdapter;
import com.vn.weather.asyncTask.WeatherFC10dAsyncTask;
import com.vn.weather.entity.forecastWeather10d.WeatherFC10d;

public class WeatherFC10Activity extends AppCompatActivity {

    private Location currentLocation;
    private WeatherFC10dAsyncTask weatherFC10dAsyncTask;
    private RecyclerView recyclerView;
    private Forecast10dAdapter forecast10dAdapter;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_fc10);


        context = this;
        Intent intent = getIntent();
        double[] locationData = intent.getExtras().getDoubleArray(WeatherActivity.LOCATION_DATA);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tlb_weather_forecast10d);
        toolbar.setTitle(getResources().getString(R.string.daily_forecast));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //setSupportActionBar(toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.list_forecast_10d);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        weatherFC10dAsyncTask = new WeatherFC10dAsyncTask(this, locationData);
        weatherFC10dAsyncTask.execute();
        weatherFC10dAsyncTask.setCallBack(new WeatherFC10dAsyncTask.CallBack() {
            @Override
            public void onFinish(String body) {
                Gson gson = new Gson();
                WeatherFC10d weatherFC10d = gson.fromJson(body, WeatherFC10d.class);
                forecast10dAdapter = new Forecast10dAdapter(context, weatherFC10d.getList());
                recyclerView.setAdapter(forecast10dAdapter);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.item_toolbars_fc10d, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*switch (item.getItemId()){
            case R.id.home:{
                onBackPressed();
            }
        }*/
        return super.onOptionsItemSelected(item);
    }
}
