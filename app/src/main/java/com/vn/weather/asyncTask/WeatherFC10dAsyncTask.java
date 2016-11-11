package com.vn.weather.asyncTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by phanvuong on 11/10/16.
 */

public class WeatherFC10dAsyncTask extends AsyncTask<Void, Void, String> {

    public interface CallBack{
        public void onFinish(String body);
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    private CallBack callBack;

    private ProgressDialog mProgressDialog;

    private double[] locationData;
    private String body;

    public WeatherFC10dAsyncTask(Context context, double[] locationData) {
        this.locationData = locationData;
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Loading.....");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    protected String doInBackground(Void... params) {

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://api.openweathermap.org/data/2.5/forecast/daily?lat="+locationData[0]+"&lon="+locationData[1]+"&cnt=16&mode=json&appid=24c4a4e0f6c39150ce79ea184df5ba58")
                .addHeader("Accept", "application/json")
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            if(response.isSuccessful()){
                body = response.body().string();
                return body;
            }
        } catch (IOException e) {
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
}
