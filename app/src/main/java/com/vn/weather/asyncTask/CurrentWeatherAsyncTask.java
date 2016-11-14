package com.vn.weather.asyncTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.vn.weather.WeatherActivity;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by phanvuong on 11/5/16.
 */

public class CurrentWeatherAsyncTask extends AsyncTask<Void, Void, String> {

    public interface CallBack{
        public void onFinish(String body);
    }


    private CallBack callBack;

    private ProgressDialog mProgressDialog;

    private Location location;
    private String body;

    public void setCheckNetwork(boolean checkNetwork) {
        this.checkNetwork = checkNetwork;
    }

    private boolean checkNetwork;

    public CurrentWeatherAsyncTask(Context context, Location location) {
        this.location = location;
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Loading.....");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... params) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request1 = new Request.Builder()
                .url("http://api.openweathermap.org/data/2.5/weather?lat="+location.getLatitude()+"&lon="+location.getLongitude()+"&APPID=24c4a4e0f6c39150ce79ea184df5ba58")
                .addHeader("Accept", "application/json")
                .build();
        Request request2 = new Request.Builder()
                .url("http://api.openweathermap.org/data/2.5/forecast?lat="+location.getLatitude()+"&lon="+location.getLongitude()+"&appid=24c4a4e0f6c39150ce79ea184df5ba58")
                .addHeader("Accept", "application/json")
                .build();
        try{
            if(checkNetwork){
                Response response1 = okHttpClient.newCall(request1).execute();
                if(response1.isSuccessful()){
                    body = response1.body().string();
                    Log.e("Json", body);
                }
                Response response2 = okHttpClient.newCall(request2).execute();
                if(response2.isSuccessful()){
                    body = body+ WeatherActivity.STRING_CUT_JSON+response2.body().string();
                    Log.e("Json", body);
                    return body;
                }
            }else{
                Scanner scanner = new Scanner(new File(Environment.getExternalStorageDirectory()
                        .getAbsolutePath()+"/Download/current_weather.json"));
                body = scanner.nextLine();
                Log.e("OldJson", body);
                return body;
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String body) {
        super.onPostExecute(body);
        mProgressDialog.dismiss();
        if(callBack != null){
            callBack.onFinish(body);
        }

    }


    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }
}
