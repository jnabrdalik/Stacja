package com.example.stacjapogodowa;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.StrictMode;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.support.v7.widget.Toolbar;
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
    private ViewPager viewPager;
    private double latitude, longitude;
    //private Bundle bundle;
   // private static String key = "31c61427016fef5751667871cf897785";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Toolbar toolbar = findViewById(R.id.tb1);
        setSupportActionBar(toolbar);

        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        updateLocation();
        adapter = new WeatherDataStatePagerAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.container);
        setupViewPager(viewPager);
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
                EditText editText = findViewById(R.id.new_city);
                String city = editText.getText().toString();
                if (!city.matches("")) {
                    addCity(city);
                    Toast.makeText(this, "Dodano: " + city, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.refresh_option:
                updateLocation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        float pressure = event.values[0];
        WeatherFragment wf = (WeatherFragment) adapter.getItem(0);
        double altitude = calculateAltitude(wf.getPressure(), pressure, wf.getTemperature());
        TextView altitude_tv = findViewById(R.id.altitude_tv);
        altitude_tv.setText(String.format("%.2f m", altitude));

    }

    public static double calculateAltitude(double seaLvlPressure, double currentPressure, double temp) {
        //return (Math.pow(seaLvlPressure/currentPressure, 1/5.257) - 1) * (temp + 273.15) / 0.0065;
        return 29.21 * (temp + 273.15) * Math.log(seaLvlPressure/currentPressure);
    }

    private void setupViewPager(ViewPager vp) {
        adapter.addFragment(WeatherFragment.newInstance("Dubai"));
        viewPager.setAdapter(adapter);
    }

    public void addCity(String city) {
        adapter.addFragment(WeatherFragment.newInstance(city));
        adapter.notifyDataSetChanged();
    }

    @SuppressLint("MissingPermission")
    public void updateLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            ((WeatherFragment)adapter.getItem(0)).setCoordinates(latitude, longitude);
                            adapter.notifyDataSetChanged();
                        }
                    }
                });

    }
}
