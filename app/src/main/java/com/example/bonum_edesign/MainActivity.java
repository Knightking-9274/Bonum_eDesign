package com.example.bonum_edesign;


import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.Manifest;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.bonum_edesign.databinding.ActivityMainBinding;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private String nameOfCity;
    private ArrayList<RecycleWeather> weatherArrayList;
    private RecyclerAdapter adpt;
    private int PERMIT_CODE = 1;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        weatherArrayList = new ArrayList<>();
        adpt = new RecyclerAdapter(this, weatherArrayList);

        binding.recyclerView.setAdapter(adpt);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMIT_CODE);
        }else {
            getWeatherData(nameOfCity);
        }
//        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//        nameOfCity = getCityName(location.getLongitude(),location.getLatitude());


        binding.imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = binding.inputEditText.getText().toString();
                if (city.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please Enter City Name!", Toast.LENGTH_SHORT).show();
                } else {
                    binding.txtViewCityName.setText(nameOfCity);
                    getWeatherData(city);
                }
            }
        });

    }

    private void getLocationAndWeatherData() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }

        if (location != null) {
            nameOfCity = getCityName(location.getLongitude(), location.getLatitude());
            getWeatherData(nameOfCity);
        } else {
            nameOfCity="Mumbai";// Default City
            getWeatherData(nameOfCity);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMIT_CODE){
            if(grantResults.length>0 && grantResults[0] ==PackageManager.PERMISSION_GRANTED){
                getLocationAndWeatherData();
                Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void getWeatherData(String cityName) {
        weatherArrayList.clear();
        String url = "http://api.weatherapi.com/v1/forecast.json?key=88777c52fb7a4f718ea54848241903&q=" + cityName + "&days=1&aqi=yes&alerts=yes";
        binding.txtViewCityName.setText(cityName);

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                ArrayList<RecycleWeather> weatherData = parseWeatherData(jsonObject);
                onWeatherDataReceived(weatherData);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(MainActivity.this, "Please enter a valid City name!", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(request);
    }
    private ArrayList<RecycleWeather> parseWeatherData(JSONObject jsonObject) {
        ArrayList<RecycleWeather> weatherData = new ArrayList<>();
        try {
            String temperature = jsonObject.getJSONObject("current").getString("temp_c");
            int isDay = jsonObject.getJSONObject("current").getInt("is_day");
            String condition = jsonObject.getJSONObject("current").getJSONObject("condition").getString("text");
            String icon = jsonObject.getJSONObject("current").getJSONObject("condition").getString("icon");

            String imageUrl = (isDay == 1) ? "https://w0.peakpx.com/wallpaper/779/771/HD-wallpaper-autumn-view-morning-nature.jpg"
                    : "https://w0.peakpx.com/wallpaper/803/717/HD-wallpaper-night-view-edge-milky-night-sky-stars.jpg";

            binding.txtTemp.setText(temperature + "°c");
            binding.txtCondition.setText(condition);
            Picasso.get().load("http:" + icon).into(binding.weatherImgView);
            Picasso.get().load(imageUrl).into(binding.bgImageView);

            JSONObject forecast = jsonObject.getJSONObject("forecast");
            JSONObject forecastObject = forecast.getJSONArray("forecastday").getJSONObject(0);
            JSONArray hour = forecastObject.getJSONArray("hour");

            for (int i = 0; i < hour.length(); i++) {
                JSONObject hourObj = hour.getJSONObject(i);
                String time = hourObj.getString("time");
                String temp = hourObj.getString("temp_c");
                String img = hourObj.getJSONObject("condition").getString("icon");
                String windSpeed = hourObj.getString("wind_kph");
                weatherData.add(new RecycleWeather(time, temp, img, windSpeed));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return weatherData;
    }
    public void onWeatherDataReceived(ArrayList<RecycleWeather> weatherData) {
        weatherArrayList.addAll(weatherData);
        adpt.notifyDataSetChanged();
    }
    private String getCityName(double longi,double lati){
        String cityName="Not Found!";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try{
            List<Address> address = gcd.getFromLocation(longi,lati,10);
                for(Address adr: address){
                    if(adr!=null){
                        String city =adr.getLocality();
                        if(city!= null && !city.equals("")){
                            cityName = city;
                        }
                        else{
                            Log.d("TAG","CITY NOT FOUND");
                            Toast.makeText(this,"User not Found!",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        return cityName;
    }
}