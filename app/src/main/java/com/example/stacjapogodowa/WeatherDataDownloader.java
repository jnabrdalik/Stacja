package com.example.stacjapogodowa;

import android.os.AsyncTask;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class WeatherDataDownloader {

    private static final String API_KEY = "31c61427016fef5751667871cf897785";

    private OnWeatherDataDownloadedListener listener;

    WeatherDataDownloader(OnWeatherDataDownloadedListener listener) {
        this.listener = listener;
    }


    public void downloadData(double latitude, double longitude) {
        String address = String.format(Locale.US,"https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&units=metric&lang=pl&APPID=%s", latitude, longitude, API_KEY);
        download(address);
    }

    public void downloadData(String cityName) {
        String address = String.format("https://api.openweathermap.org/data/2.5/weather?q=%s&units=metric&lang=pl&APPID=%s", cityName, API_KEY);
        download(address);
    }

    private void download(final String address) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(address);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(
                                        connection.getInputStream()));
                        JSONObject data = new JSONObject(in.readLine());
                        in.close();

                        Bundle args = new Bundle();

                        String cityName = data.getString("name");
                        args.putString("city", cityName);

                        JSONObject weatherDataJson = data.getJSONObject("main");

                        double temperature = weatherDataJson.getDouble("temp");
                        args.putDouble("temp", temperature);

                        int pressure = weatherDataJson.getInt("pressure");
                        args.putInt("press", pressure);

                        int humidity = weatherDataJson.getInt("humidity");
                        args.putInt("hum", humidity);

                        JSONArray weather = data.getJSONArray("weather");
                        StringBuilder description = new StringBuilder();
                        for (int i = 0; i < weather.length(); i++) {
                            JSONObject jo = (JSONObject) weather.get(i);
                            description.append(jo.get("description"));
                            description.append('\n');
                        }
                        String weatherDescription = description.toString();
                        args.putString("desc", weatherDescription);

                        listener.onFinishedDownloading(args);
                    } else
                        listener.onError();

                } catch (JSONException | IOException e) {
                    listener.onError();
                }
            }
        });
    }

    interface OnWeatherDataDownloadedListener {
        void onFinishedDownloading(Bundle bundle);
        void onError();
    }

}
