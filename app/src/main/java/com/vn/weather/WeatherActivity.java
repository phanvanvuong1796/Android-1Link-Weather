package com.vn.weather;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
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
import com.vn.weather.entity.forecastWeather3h.WeatherFC3h;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;


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
    public static final int REQUEST_ID_ACCESS_COURSE_FINE_LOCATION = 100;
    private static final int REQUEST_WRITE_STORAGE = 112;

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

    private Location getCurrentLocation() {
        Location currentLocation = null;
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
        return currentLocation;
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

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Location currentLocation = getCurrentLocation();
        Log.e("Long", currentLocation.getLongitude()+"");
        Log.e("Lat", currentLocation.getLatitude()+"");

        getWeatherData(currentLocation);


    }

    private void getWeatherData(Location currentLocation) {
        mCurrentWeather = new CurrentWeatherAsyncTask(this, currentLocation);
        Log.e("AsyncTask", "Running");
        mCurrentWeather.execute();
        mCurrentWeather.setCallBack(new CurrentWeatherAsyncTask.CallBack() {
            @Override
            public void onFinish(String body) {
                try {
                    Log.e("Callback", body);
                    String[] datas = body.split(STRING_CUT_JSON);
                    Gson gson = new Gson();
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(datas[0]);
                    WeatherEntity weatherEntity;
                    WeatherFC3h weatherFC3h;

                    weatherEntity = gson.fromJson(datas[0], WeatherEntity.class);
                    weatherFC3h = gson.fromJson(datas[1], WeatherFC3h.class);
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
}
