package com.example.secquraise;

import static android.text.InputType.TYPE_CLASS_NUMBER;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.Manifest;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    TextView time, capture_val, frequency_val, connectivity_val, charging_val_bool, charge_val, location_val;
    ChargingReceiver chargingReceiver;
    private static final int LOCATION_REQUEST_CODE = 1001;
    int freqency = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        requestLocationPermission();
        connect_values();
        dateTime();

        chargingReceiver = new ChargingReceiver(charging_val_bool, charge_val);
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(chargingReceiver, filter);

        frequency_val.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFrequency();
            }
        });
    }

    private void changeFrequency(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Frequency (minutes)");

        final EditText input = new EditText(this);
        input.setInputType(TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(freqency));
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = input.getText().toString();
                if (!value.isEmpty()){
                    freqency = Integer.parseInt(value);
                    frequency_val.setText(freqency+"");
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void init() {
        time = findViewById(R.id.timestamp);
        capture_val = findViewById(R.id.captureval);
        frequency_val = findViewById(R.id.frequencyval);
        connectivity_val = findViewById(R.id.connectivityval);
        charging_val_bool = findViewById(R.id.batterychargeactionval);
        charge_val = findViewById(R.id.batterychargeperval);
        location_val = findViewById(R.id.locationvalue);
        frequency_val.setText("15");
    }

    public void connect_values() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            connectivity_val.setText("ON");
        } else {
            connectivity_val.setText("OFF");
        }
    }

    public void dateTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String date = simpleDateFormat.format(calendar.getTime());
        time.setText(date);
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, LOCATION_REQUEST_CODE);
        } else {
            setLocation_val();
        }
        Log.d("MainActivity", "Location REQ CALLED");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Location permission granted");
                setLocation_val();
            } else {
                Log.e("MainActivity", "Location permission denied");
            }
        }
    }

    public void setLocation_val() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                try {
                    Log.d("MainActivity", "Location updated: " + location.getLatitude() + ", " + location.getLongitude());
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    location_val.setText(latitude + ", " + longitude);
                } catch (Exception e) {
                    Log.e("MainActivity", "Error updating location: " + e.getMessage());
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(chargingReceiver);
    }
}