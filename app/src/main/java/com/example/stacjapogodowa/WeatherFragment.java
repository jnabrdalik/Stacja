package com.example.stacjapogodowa;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class WeatherFragment extends Fragment implements WeatherDataDownloader.OnWeatherDataDownloadedListener {

    private TextView city_tv, temp_tv, pressure_tv, humidity_tv, weather_description;
    private WeatherDataDownloader downloader;

    public WeatherFragment() {}

    public static WeatherFragment newInstance(Bundle bundle) {
        WeatherFragment fragment = new WeatherFragment();
        fragment.downloader = new WeatherDataDownloader(fragment);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_weather, container, false);
        city_tv = view.findViewById(R.id.city);
        if (isLocalWeather())
            city_tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_location_on_black_24dp,
                    0, 0, 0);
        temp_tv = view.findViewById(R.id.temperature);
        pressure_tv = view.findViewById(R.id.pressure);
        humidity_tv = view.findViewById(R.id.humidity);
        weather_description = view.findViewById(R.id.weather_description);

        updateData();

        return view;
    }

    private boolean isLocalWeather() {
        Bundle args = getArguments();
        return args.containsKey("lat");
    }

    private void updateView() {
        Bundle args = getArguments();
        String cityName = args.getString("city");
        city_tv.setText(cityName);

        double temperature = args.getDouble("temp");
        int roundedTemp = (int) Math.round(temperature);
        temp_tv.setText(roundedTemp + "°");
        int humidity = args.getInt("hum");
        humidity_tv.setText(humidity + "%");
        int pressure = args.getInt("press");
        pressure_tv.setText(String.format("%d hPa", pressure));
        String weatherDescription = args.getString("desc");
        weather_description.setText(weatherDescription);

    }

    public void updateData() {
        Bundle args = getArguments();
        if (args.containsKey("lat") && args.containsKey("lon")) {
            double latitude = args.getDouble("lat");
            double longitude = args.getDouble("lon");
            downloader.downloadData(latitude, longitude);
        }
        else if (args.containsKey("city")) {
            String cityName = args.getString("city");
            downloader.downloadData(cityName);
        }
    }

    @Override
    public void onFinishedDownloading(Bundle bundle) {
        Bundle args = getArguments();
        args.putAll(bundle);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateView();
            }
        });
    }

    @Override
    public void onError() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), "Błąd pobierania!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
