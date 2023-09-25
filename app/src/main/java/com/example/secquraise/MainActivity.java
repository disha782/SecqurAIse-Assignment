package com.example.secquraise;

import static android.text.InputType.TYPE_CLASS_NUMBER;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.Manifest;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    TextView time, capture_val, frequency_val, connectivity_val, charging_val_bool, charge_val, location_val;
    Button refresh_btn;
    ChargingReceiver chargingReceiver;
    private static final int LOCATION_REQUEST_CODE = 1001;
    private static final long RETRY_DELAY_MS = 5000;
    int freqency = 15;
    int count = 1;
    private static final int MAX_RETRY_COUNT = 3;
    private int retryCount = 0;
    DatabaseReference dbref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        requestLocationPermission();
        connect_values();
        dateTime();
        setLocation_val();
        chargingReceiver = new ChargingReceiver(charging_val_bool, charge_val);
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(chargingReceiver, filter);
        refreshValues();
        frequency_val.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFrequency();
            }
        });
        refresh_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshValues();
            }
        });

        schedulePeriodicUpdate();
    }
    private void schedulePeriodicUpdate() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshValues();
                handler.postDelayed(this, (long) freqency*60*1000);
            }
        }, (long) freqency*60*1000);
    }

    private void refreshValues() {
        connect_values();
        dateTime();
        setLocation_val();
        count++;
        capture_val.setText(String.valueOf(count));
        chargingReceiver = new ChargingReceiver(charging_val_bool, charge_val);
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(chargingReceiver, filter);
        if (!isNetworkConnected()){
            storeDataLocally();
            if (retryCount < MAX_RETRY_COUNT){
                retryCount++;
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshValues();;
                    }
                }, RETRY_DELAY_MS);
                return;
            }
        }
        retryCount = 0;

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        String internetConnectivity = connectivity_val.getText().toString();
        String batteryStatus = charging_val_bool.getText().toString();
        String batteryPercentage = charge_val.getText().toString();
        String location = location_val.getText().toString();
        String timestamp = time.getText().toString();

        CaptureData capturedData = new CaptureData(internetConnectivity, batteryStatus, batteryPercentage, location, timestamp);

        Call<ApiResponse> call = apiService.captureData(capturedData);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    Log.i("MainActivity", "Data stored successfully");
                } else {
                    Log.e("MainActivity", "Error storing data: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("MainActivity", "API call failed: " + t.getMessage());
            }
        });

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dbref = FirebaseDatabase.getInstance("https://secquraise-ceb6d-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("user_details");
                String userId = dbref.push().getKey();
                DatabaseReference userRef = dbref.child(userId);
                userRef.child("Internet Connectivity Status").setValue(connectivity_val.getText().toString());
                userRef.child("Battery Charging status").setValue(charging_val_bool.getText().toString());
                userRef.child("Battery Charge Percentage").setValue(charge_val.getText().toString());
                userRef.child("Location").setValue(location_val.getText().toString());
                userRef.child("Timestamp").setValue(time.getText().toString());
            }
        }, RETRY_DELAY_MS);

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
                    frequency_val.setText(String.valueOf(freqency));
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
        refresh_btn = findViewById(R.id.refreshbtn);
        frequency_val.setText("15");
        capture_val.setText(String.valueOf(count));
    }

    public void connect_values() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            connectivity_val.setText(R.string.on_status);
        } else {
            connectivity_val.setText(R.string.off_status);
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

    public boolean isNetworkConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    private void storeDataLocally() {
        SharedPreferences sharedPreferences = getSharedPreferences("LocalData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("InternetConnectivity", connectivity_val.getText().toString());
        editor.putString("BatteryChargingStatus", charging_val_bool.getText().toString());
        editor.putString("BatteryChargePercentage", charge_val.getText().toString());
        editor.putString("Location", location_val.getText().toString());
        editor.putString("Timestamp", time.getText().toString());

        editor.apply();
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            restoreLocalData();
        }
    };

    private void restoreLocalData() {
        SharedPreferences sharedPreferences = getSharedPreferences("LocalData", MODE_PRIVATE);
        connectivity_val.setText(sharedPreferences.getString("InternetConnectivity", ""));
        charging_val_bool.setText(sharedPreferences.getString("BatteryChargingStatus", ""));
        charge_val.setText(sharedPreferences.getString("BatteryChargePercentage", ""));
        location_val.setText(sharedPreferences.getString("Location", ""));
        time.setText(sharedPreferences.getString("Timestamp", ""));
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_BOOT_COMPLETED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(chargingReceiver);
        count = 0;
        retryCount = 0;
    }
}