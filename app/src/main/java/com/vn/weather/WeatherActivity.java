package com.vn.weather;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.vn.weather.asyncTask.CurrentWeatherAsyncTask;
import com.vn.weather.entity.Weather;
import com.vn.weather.entity.WeatherEntity;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;


public class WeatherActivity extends AppCompatActivity{

    private CurrentWeatherAsyncTask mCurrentWeather;
    private TextView txtCountry;
    private TextView txtStatus;
    private TextView txtTemp;
    private TextView txtHumidity;
    private TextView txtSpeed;
    private TextView txtLastUpdate;
    private File file;
    private Location currentLocation;
    private LocationListener locationListener;
    public static final int REQUEST_ID_ACCESS_COURSE_FINE_LOCATION = 100;
    private final long MIN_TIME_BW_UPDATES = 1000;
    // Met
    private final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getView();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
                Log.e("Lon", location.getLongitude()+"");
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };

        getCurrentLocation();

        Log.e("Long", String.valueOf(currentLocation.getLongitude()));
        final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        file = new File(path+"/Download/current_weather.json");
        Log.e("PATH", path);
        mCurrentWeather = new CurrentWeatherAsyncTask(this, currentLocation);
        mCurrentWeather.execute();
        mCurrentWeather.setCallBack(new CurrentWeatherAsyncTask.CallBack() {
            @Override
            public void onFinish(String body) {
                try {
                    Log.e("Callback", body);

                    Gson gson = new Gson();
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(body);
                    WeatherEntity weatherEntity;
                    weatherEntity = gson.fromJson(body, WeatherEntity.class);
                    Log.e("eee", weatherEntity.getName());
                    txtCountry.setText(weatherEntity.getName());
                    txtStatus.setText(weatherEntity.getWeather().get(0).getDescription());
                    String temp = String.format("%.1f", (weatherEntity.getMain().getTemp() - 273));
                    txtTemp.setText(temp + " °C");
                    txtHumidity.setText("Độ ẩm: " + weatherEntity.getMain().getHumidity() + " %");
                    txtSpeed.setText("Tốc độ: " + weatherEntity.getWind().getSpeed() + " m/s");
                    txtLastUpdate.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(file.lastModified()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getView() {
        txtCountry = (TextView) findViewById(R.id.txt_country);
        txtStatus = (TextView) findViewById(R.id.txt_status);
        txtTemp = (TextView) findViewById(R.id.txt_temp);
        txtHumidity = (TextView) findViewById(R.id.txt_humidity);
        txtSpeed = (TextView) findViewById(R.id.txt_speed);
        txtLastUpdate = (TextView) findViewById(R.id.txt_last_update);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                getCurrentLocation();
                break;
            default:
                break;
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET}, 10);
            }
            return;
        }
        // this code won't execute IF permissions are not allowed, because in the line above there is return statement.
        //noinspection MissingPermission
        locationManager.requestLocationUpdates("gps", MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
    }
}
