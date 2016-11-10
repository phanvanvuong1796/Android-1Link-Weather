package com.vn.weather.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vn.weather.R;

import java.util.List;

/**
 * Created by phanvuong on 11/9/16.
 */

public class Forecast3hAdapter extends RecyclerView.Adapter<Forecast3hAdapter.ViewHolder> {

    private List<com.vn.weather.entity.forecastWeather3h.List> listWeathers;
    private Context context;
    private final String urlImage = "http://openweathermap.org/img/w/";

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView txtTimeFC; // display time forecast 3 hour
        public ImageView imgFC3h; // display image forecast 3 hour
        public TextView txtSttFC3h; // display status forecast 3 hour
        public TextView txtTempFC3h; // display Temp forecast 3 hour

        public ViewHolder(View itemView) {
            super(itemView);
            txtTimeFC = (TextView) itemView.findViewById(R.id.txt_time_forecast);
            imgFC3h = (ImageView) itemView.findViewById(R.id.img_icon_forecast_3h);
            txtSttFC3h = (TextView) itemView.findViewById(R.id.txt_stt_forecast_3h);
            txtTempFC3h = (TextView) itemView.findViewById(R.id.txt_temp_forecast_3h);
        }
    }

    public Forecast3hAdapter(List<com.vn.weather.entity.forecastWeather3h.List> listWeathers, Context context) {
        this.listWeathers = listWeathers;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_forecast_3h, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        com.vn.weather.entity.forecastWeather3h.List listWeather = listWeathers.get(position);
        String dtTxt = listWeather.getDtTxt();
        Log.e("TIME", dtTxt);
        holder.txtTimeFC.setText(dtTxt.substring(11, 16));
        Picasso.with(context).load(urlImage+listWeather.getWeather().get(0).getIcon()+".png")
                .fit()
                .centerCrop()
                .into(holder.imgFC3h);
        holder.txtSttFC3h.setText(listWeather.getWeather().get(0).getDescription());
        holder.txtTempFC3h.setText((int)(listWeather.getMain().getTemp() - 273)+" °C");
    }

    @Override
    public int getItemCount() {
        return listWeathers.size();
    }
}
