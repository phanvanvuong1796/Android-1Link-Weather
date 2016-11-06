package com.vn.weather;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.vn.weather.asyncTask.CurrentWeatherAsyncTask;
import com.vn.weather.entity.WeatherEntity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;


public class MainActivity extends AppCompatActivity implements LocationListener {

    private CurrentWeatherAsyncTask mCurrentWeather;
    private TextView txtCountry;
    private TextView txtStatus;
    private TextView txtTemp;
    private TextView txtHumidity;
    private TextView txtSpeed;
    private TextView txtLastUpdate;
    private File file;
    private Location currentLocation;
    public static final int REQUEST_ID_ACCESS_COURSE_FINE_LOCATION = 100;
    private final long MIN_TIME_BW_UPDATES = 1000;
    // Met
    private final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getView();

        getCurrentLocation();

        Log.e("Long", String.valueOf(currentLocation.getLongitude()));
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();

        file = new File(path + "/Download/current_weather.json");
        Log.e("PATH", path);
        mCurrentWeather = new CurrentWeatherAsyncTask(this, currentLocation);
        mCurrentWeather.execute();
        mCurrentWeather.setCallBack(new CurrentWeatherAsyncTask.CallBack() {
            @Override
            public void onFinish(String body) {
                try {
                    Gson gson = new Gson();
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(body);
                    WeatherEntity weatherEntity = new WeatherEntity();
                    weatherEntity = gson.fromJson(body, WeatherEntity.class);
                    txtCountry.setText(weatherEntity.getName());
                    txtStatus.setText(weatherEntity.getWeather().get(0).getDescription());
                    String temp = String.format("%.1f", (weatherEntity.getMain().getTemp() - 273));
                    txtTemp.setText(temp + " °C");
                    txtHumidity.setText("Độ ẩm: " + weatherEntity.getMain().getHumidity() + " %");
                    txtSpeed.setText("Tốc độ: " + weatherEntity.getWind().getSpeed() + " m/s");
                    txtLastUpdate.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(file.lastModified()));
                } catch (IOException e) {
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ID_ACCESS_COURSE_FINE_LOCATION: {
                if (grantResults.length > 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "Permission granted!", Toast.LENGTH_LONG).show();

                    // Hiển thị vị trí hiện thời trên bản đồ.
                    this.getCurrentLocation();
                }
                // Hủy bỏ hoặc từ chối.
                else {
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    private void getCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        //String provider = locationManager.getBestProvider(criteria, true);

        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnabled) {
            showSettingsAlert();
        } else {
            try{
                if (Build.VERSION.SDK_INT >= 23) {
                    int accessCoarsePermission
                            = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
                    int accessFinePermission
                            = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);


                    if (accessCoarsePermission != PackageManager.PERMISSION_GRANTED
                            || accessFinePermission != PackageManager.PERMISSION_GRANTED) {

                        // Các quyền cần người dùng cho phép.
                        String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION};

                        // Hiển thị một Dialog hỏi người dùng cho phép các quyền trên.
                        ActivityCompat.requestPermissions(this, permissions,
                                REQUEST_ID_ACCESS_COURSE_FINE_LOCATION);
                        return;
                    }
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }catch (SecurityException e){
                Toast.makeText(this, "Show My Location Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        AlertDialog dialog = alertDialog.create();
        dialog.show();
    }
}
