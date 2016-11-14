package com.vn.weather;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.vn.weather.adapter.Forecast3hAdapter;
import com.vn.weather.asyncTask.CurrentWeatherAsyncTask;
import com.vn.weather.entity.currentWeather.WeatherEntity;
import com.vn.weather.entity.forecastWeather10d.WeatherFC10d;
import com.vn.weather.entity.forecastWeather3h.WeatherFC3h;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Scanner;


public class WeatherActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private CurrentWeatherAsyncTask mCurrentWeather;
    private TextView txtCountry;
    private TextView txtStatus;
    private TextView txtTemp;
    private TextView txtHumidity;
    private TextView txtSpeed;
    private TextView txtLastUpdate;
    private RecyclerView mRecyclerView;
    private File file;
    private ImageView imgIcon;
    private String path;
    public static final String STRING_CUT_JSON = "drop";
    private final String urlImage = "http://openweathermap.org/img/w/";
    private static final int REQUEST_ID_ACCESS_COURSE_FINE_LOCATION = 100;
    private static final int REQUEST_WRITE_STORAGE = 112;
    public static final String LOCATION_DATA = "LocationData";
    private Location currentLocation;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getView();

        if (mGoogleApiClient == null) {
            buildGoogleApiClient();
        }


        path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download/current_weather.json";

        //Check self permission WRITE_EXTERNAL_STORAGE với android 6.0 trở lên
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
            }
        }

        file = new File(path);
        Log.e("PATH", path);

    }

    /**
     *
     */
    public void getView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.tlb_weather_main);
        setSupportActionBar(toolbar);

        txtCountry = (TextView) findViewById(R.id.txt_country);
        txtStatus = (TextView) findViewById(R.id.txt_status);
        txtTemp = (TextView) findViewById(R.id.txt_temp);
        txtHumidity = (TextView) findViewById(R.id.txt_humidity);
        txtSpeed = (TextView) findViewById(R.id.txt_speed);
        txtLastUpdate = (TextView) findViewById(R.id.txt_last_update);
        imgIcon = (ImageView) findViewById(R.id.img_icon);
        mRecyclerView = (RecyclerView) findViewById(R.id.list_forecast_3h);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ID_ACCESS_COURSE_FINE_LOCATION:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    getCurrentLocation();
                }else{

                }
                break;
            }
            case REQUEST_WRITE_STORAGE:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    file = new File(path);
                }else{

                }
                break;
            }
        }
    }

    private void getCurrentLocation() {
        try{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
                }
            }
                currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(currentLocation == null){

            }
        }catch (SecurityException e){
            Toast.makeText(this, "Show My Location Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.item_toolbars, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.item_refresh:
                onResume();
                onConnected(new Bundle());
                break;
            case R.id.item_forecast:{
                if(!checkNetworkState()){
                    Toast.makeText(this, getResources().getString(R.string.network_unavailable),
                            Toast.LENGTH_LONG).show();
                }else{
                    Intent intent = new Intent(this, WeatherFC10Activity.class);
                    intent.putExtra(LOCATION_DATA, new double[]{currentLocation.getLatitude(), currentLocation.getLongitude()});
                    startActivity(intent);
                }

                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getCurrentLocation();
        Log.e("Long", currentLocation.getLongitude()+"");
        Log.e("Lat", currentLocation.getLatitude()+"");

        getWeatherData(currentLocation);
    }

    private void getWeatherData(Location currentLocation) {

        mCurrentWeather = new CurrentWeatherAsyncTask(this, currentLocation);
        mCurrentWeather.setCheckNetwork(checkNetworkState());
        Log.e("AsyncTask", "Running");
        mCurrentWeather.execute();
        mCurrentWeather.setCallBack(new CurrentWeatherAsyncTask.CallBack() {
            @Override
            public void onFinish(String body) {
                try {
                    Gson gson = new Gson();
                    Log.e("Callback", body);
                    String[] data = body.split(STRING_CUT_JSON);
                    WeatherEntity weatherEntity;
                    WeatherFC3h weatherFC3h = null;
                    if(!checkNetworkState()){
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_unavailable)
                                , Toast.LENGTH_LONG).show();
                    }else{
                        FileWriter fileWriter = new FileWriter(file);
                        fileWriter.write(body);
                        weatherFC3h = gson.fromJson(data[1], WeatherFC3h.class);
                    }
                    Log.e("1EEEEE", data[1]);
                    weatherEntity = gson.fromJson(data[0], WeatherEntity.class);
                    Log.e("City", weatherEntity.getName());

                    if(weatherFC3h != null){
                        Log.e("NULL", "FC3h not Null");
                    }
                    Log.e("eee", weatherEntity.getName());
                    bindingWeatherData(weatherEntity, weatherFC3h);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void bindingWeatherData(WeatherEntity weatherEntity, WeatherFC3h weatherFC3h) {
        txtCountry.setText(weatherEntity.getName());
        txtStatus.setText(weatherEntity.getWeather().get(0).getDescription());
        String temp = String.format("%.1f", (weatherEntity.getMain().getTemp() - 273));
        txtTemp.setText(temp + " °C");
        txtHumidity.append(" "+weatherEntity.getMain().getHumidity() + " %");
        txtSpeed.append(" "+weatherEntity.getWind().getSpeed() + " m/s");
        txtLastUpdate.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(file.lastModified()));
        Picasso.with(this).load(urlImage+weatherEntity.getWeather().get(0).getIcon()+".png")
                .fit()
                .centerCrop()
                .into(imgIcon);
        Log.e("TIME", weatherFC3h.getList().get(0).getDtTxt()+"");
        Forecast3hAdapter mForecast3hAdapter = new Forecast3hAdapter(weatherFC3h.getList().subList(2, 10), this);
        mRecyclerView.setAdapter(mForecast3hAdapter);
    }


    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        txtHumidity.setText(R.string.humidity_name);
        txtSpeed.setText(R.string.speed_name);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }

    public boolean checkNetworkState(){
        boolean check = true;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED &&
                connectivityManager.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED){
            check = false;
        }
        return check;
    }
}
