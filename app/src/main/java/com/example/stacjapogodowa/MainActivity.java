package com.example.stacjapogodowa;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor pressureSensor;
    private FusedLocationProviderClient fusedLocationClient;
    private WeatherDataStatePagerAdapter adapter;
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Toolbar toolbar = findViewById(R.id.tb1);
        setSupportActionBar(toolbar);

        adapter = new WeatherDataStatePagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.container);
        setupViewPager(viewPager);
        updateLocation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_option:
                EditText editText = findViewById(R.id.new_city_et);
                String cityName = editText
                        .getText()
                        .toString()
                        .trim();

                if (isACorrectName(cityName)) {
                    addCity(cityName);
                    Toast.makeText(this, "Dodano: " + cityName, Toast.LENGTH_SHORT).show();
                }
                else {
                    String errorMessage = "Niepoprawna nazwa miasta!";
                    editText.setError(errorMessage);
                }

                return true;
            case R.id.refresh_option:
                updateData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateData() {
        adapter.updateAll();
    }

    private boolean isACorrectName(String cityName) {
        if (cityName.equals("") || adapter.contains(cityName))
            return false;

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();

        sensorManager.unregisterListener(this);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (adapter.getCount() > 0) {
            float measuredPressure = event.values[0];
            WeatherFragment wf = (WeatherFragment) adapter.getItem(0);
            Bundle args = wf.getArguments();
            double temperature = args.getDouble("temp");
            int pressure = args.getInt("press");
            double altitude = calculateAltitude(pressure, measuredPressure, temperature);
            TextView altitude_tv = findViewById(R.id.altitude_tv);
            altitude_tv.setText(String.format("%.2f m npm", altitude));
        }
    }

    public static double calculateAltitude(double seaLvlPressure, double currentPressure, double temp) {
        //return (Math.pow(seaLvlPressure/currentPressure, 1/5.257) - 1) * (temp + 273.15) / 0.0065;
        return 29.21 * (temp + 273.15) * Math.log(seaLvlPressure/currentPressure);
    }

    private void setupViewPager(ViewPager vp) {
        Bundle args = new Bundle();
        WeatherFragment fragment = WeatherFragment.newInstance(args);
        adapter.addFragment(fragment);
        vp.setAdapter(adapter);
    }

    public void addCity(String city) {
        Bundle args = new Bundle();
        args.putString("city", city);
        WeatherFragment fragment = WeatherFragment.newInstance(args);

        adapter.addFragment(fragment);
        adapter.notifyDataSetChanged();
    }

    public void updateLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null && adapter.getCount() > 0) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();

                            WeatherFragment fragment;
                            Bundle args = new Bundle();
                            args.putDouble("lat", latitude);
                            args.putDouble("lon", longitude);

                            fragment = (WeatherFragment) adapter.getItem(0);
                            Bundle fragmentArgs = fragment.getArguments();
                            fragmentArgs.putAll(args);
                            fragment.updateData();
                        }
                    }
                });

    }
}
