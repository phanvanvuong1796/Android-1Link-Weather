package com.vn.weather.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vn.weather.R;
import com.vn.weather.WeatherFC10Activity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by phanvuong on 11/10/16.
 */

public class Forecast10dAdapter extends RecyclerView.Adapter<Forecast10dAdapter.ViewHolder>{

    private Context context;

    public void setListWeather(List<com.vn.weather.entity.forecastWeather10d.List> listWeather) {
        this.listWeather = listWeather;
    }

    private List<com.vn.weather.entity.forecastWeather10d.List> listWeather;
    private final String urlImage = "http://openweathermap.org/img/w/";

    public Forecast10dAdapter(Context context, List<com.vn.weather.entity.forecastWeather10d.List> listWeather) {
        this.context = context;
        this.listWeather = listWeather;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView txtDate;
        public ImageView imgIcon;
        public TextView txtTempMax;
        public TextView txtTempMin;
        public TextView txtStt;
        public TextView txtSpeed;
        public TextView txtClouds;
        public TextView txtPressure;

        public ViewHolder(View itemView) {
            super(itemView);
            txtDate = (TextView) itemView.findViewById(R.id.txt_date_FC10d);
            imgIcon = (ImageView) itemView.findViewById(R.id.img_icon_forecast_10d);
            txtTempMax = (TextView) itemView.findViewById(R.id.txt_temp_max_FC10d);
            txtTempMin = (TextView) itemView.findViewById(R.id.txt_temp_min_FC10d);
            txtStt = (TextView) itemView.findViewById(R.id.txt_stt_FC10d);
            txtSpeed = (TextView) itemView.findViewById(R.id.txt_speed_FC10d);
            txtClouds = (TextView) itemView.findViewById(R.id.txt_clouds_FC10d);
            txtPressure = (TextView) itemView.findViewById(R.id.txt_pressure_FC10d);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_forecast_10d, parent, false);
        ViewHolder viewHolder = new ViewHolder(inflate);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        com.vn.weather.entity.forecastWeather10d.List weather = listWeather.get(position);
        Log.e("DateTime", weather.getDt()+"");
        Date date = new Date(weather.getDt()*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE dd/MM");
        sdf.setTimeZone(Calendar.getInstance().getTimeZone());
        holder.txtDate.setText(sdf.format(date));
        if(position==0){
            holder.txtDate.append("\n\n"+context.getResources().getString(R.string.today_name));
        }
        Picasso.with(context).load(urlImage+weather.getWeather().get(0).getIcon()+".png")
                .fit()
                .centerCrop()
                .into(holder.imgIcon);
        holder.txtTempMax.setText(String.format("%02d", (int)(weather.getTemp().getMax()-273)) + " °C");
        holder.txtTempMin.setText(String.format("%02d", (int)(weather.getTemp().getMin()-273)) + " °C");
        holder.txtStt.setText(weather.getWeather().get(0).getDescription());
        holder.txtSpeed.setText(weather.getSpeed()+" m/s");
        holder.txtClouds.setText(context.getResources().getString(R.string.clouds_name)+" "+weather.getClouds()+" %");
        holder.txtPressure.setText(weather.getPressure() + " hpa");

    }

    @Override
    public int getItemCount() {
        return listWeather.size();
    }
}
