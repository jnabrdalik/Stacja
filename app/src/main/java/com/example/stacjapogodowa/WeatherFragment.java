package com.example.stacjapogodowa;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * A simple {@link Fragment} subclass.
 */
public class WeatherFragment extends Fragment {

    private TextView city_tv, temp_tv, pressure_tv, humidity_tv, weather_description;

    public WeatherFragment() {}

    public static WeatherFragment newInstance(String city) {

        Bundle bundle = new Bundle();
        bundle.putString("city", city);

        return newInstance(bundle);
    }

    public static WeatherFragment newInstance(double latitude, double longitude) {
        Bundle bundle = new Bundle();
        bundle.putDouble("latitude", latitude);
        bundle.putDouble("longitude", longitude);

        return newInstance(bundle);
    }

    public static WeatherFragment newInstance(Bundle bundle) {
        WeatherFragment fragment = new WeatherFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);

        city_tv = view.findViewById(R.id.city);
        temp_tv = view.findViewById(R.id.temperature);
        pressure_tv = view.findViewById(R.id.pressure);
        humidity_tv = view.findViewById(R.id.humidity);
        weather_description = view.findViewById(R.id.weather_description);

        download();
        updateDisplayedData();

        return view;
    }

    private void updateDisplayedData() {
        Bundle bundle = getArguments();

        city_tv.setText(bundle.getString("city"));
        temp_tv.setText(bundle.getInt("temperature") + "°");
        pressure_tv.setText(bundle.getInt("pressure") + " hPa");
        humidity_tv.setText(bundle.getInt("humidity") + "%");
        weather_description.setText(bundle.getString("weather_description"));
    }

    public void download() {
        Bundle args = getArguments();
        try {
            String address;
            if (args.containsKey("latitude") && args.containsKey("longitude")) {
                double latitude = args.getDouble("latitude");
                double longitude = args.getDouble("longitude");
                address = String.format("https://api.openweathermap.org/data/2.5/weather?lat=%.2f&lon=%.2f&units=metric&lang=pl&APPID=31c61427016fef5751667871cf897785", latitude, longitude);
            }
            else {
                String city = args.getString("city");
                address = String.format("https://api.openweathermap.org/data/2.5/weather?q=%s&units=metric&lang=pl&APPID=31c61427016fef5751667871cf897785", city);
            }

            URL url = new URL(address);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()));
                JSONObject data = new JSONObject(in.readLine());
                in.close();

                args.putString("city", data.getString("name"));

                JSONObject dataMain = data.getJSONObject("main");
                args.putInt("temperature", (int) Math.round(dataMain.getDouble("temp")));
                args.putInt("pressure", dataMain.getInt("pressure"));
                args.putInt("humidity", dataMain.getInt("humidity"));
                JSONArray weather = data.getJSONArray("weather");
                String descr = "";
                for (int i = 0; i < weather.length(); i++) {
                    JSONObject jo = (JSONObject) weather.get(i);
                    descr += jo.get("description") + "\n";
                }
                args.putString("weather_description", descr);
            }

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    public int getTemperature() {
        return getArguments().getInt("temperature");
    }

    public int getPressure() {
        return getArguments().getInt("pressure");
    }

    public void setCoordinates(double latitude, double longitude) {
        getArguments().putDouble("latitude", latitude);
        getArguments().putDouble("longitude", longitude);
        download();
        updateDisplayedData();
    }
}
