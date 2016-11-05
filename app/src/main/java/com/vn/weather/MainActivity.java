package com.vn.weather;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.vn.weather.asyncTask.CurrentWeatherAsyncTask;

public class MainActivity extends AppCompatActivity {

    CurrentWeatherAsyncTask mCurrentWeather;
    TextView txtJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtJson = (TextView) findViewById(R.id.txt_json);
        mCurrentWeather = new CurrentWeatherAsyncTask(this);
        mCurrentWeather.execute();
        mCurrentWeather.setCallBack(new CurrentWeatherAsyncTask.CallBack() {
            @Override
            public void onFinish(String body) {
                txtJson.setText(body);
            }
        });

    }
}
