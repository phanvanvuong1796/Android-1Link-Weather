package com.vn.weather.asyncTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;

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

    public CurrentWeatherAsyncTask(Context context) {
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
                .url("http://api.openweathermap.org/data/2.5/weather?lat=21&lon=106&APPID=24c4a4e0f6c39150ce79ea184df5ba58")
                .addHeader("Accept", "application/json")
                .build();
        try{
            Thread.sleep(3000);
            Response response = okHttpClient.newCall(request).execute();
            if(response.isSuccessful()){
                String body = response.body().string();
                return body;
            }
        }catch (IOException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
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
