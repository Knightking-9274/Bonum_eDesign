package com.example.bonum_edesign;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private Context context;
    private ArrayList<RecycleWeather> weatherArrList;

    public RecyclerAdapter(Context context, ArrayList<RecycleWeather> weatherArrList) {
        this.context = context;
        this.weatherArrList = weatherArrList;
    }


    @NonNull
    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(context).inflate(R.layout.weather,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapter.ViewHolder holder, int position) {
        RecycleWeather weather = weatherArrList.get(position);
        holder.txtTempreture.setText(weather.getTempreture()+"°c");
        holder.txtWindSpeed.setText(weather.getWindspeed()+"km/hr");

        Picasso.get().load("http:".concat(weather.getIcon())).into(holder.condition);
        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        SimpleDateFormat output = new SimpleDateFormat("hh:mm aa");
        try{
            Date date = input.parse(weather.getTime());
            holder.txtTime.setText(output.format(date));
        }
        catch (ParseException e){
            e.printStackTrace();

        }
    }

    @Override
    public int getItemCount() {
       return weatherArrList.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView txtTime, txtTempreture, txtWindSpeed;
        private ImageView condition;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtTempreture = itemView.findViewById(R.id.txtTemp);
            txtWindSpeed = itemView.findViewById(R.id.txtWindSpeed);
            condition = itemView.findViewById(R.id.imgViewCondition);
        }

    }
}
