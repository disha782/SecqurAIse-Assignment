package com.example.secquraise;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView capture_val, frequency_val, connectivity_val, charging_val_bool, charge_val, location_val;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    public void init(){
        capture_val = findViewById(R.id.captureval);
        frequency_val = findViewById(R.id.frequencyval);
        connectivity_val = findViewById(R.id.connectivityval);
        charging_val_bool = findViewById(R.id.batterychargeactionval);
        charge_val = findViewById(R.id.batterychargeperval);
        location_val = findViewById(R.id.locationvalue);
    }
}