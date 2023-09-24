package com.example.secquraise;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.widget.TextView;

public class ChargingReceiver extends BroadcastReceiver {
    private TextView battery_charge_status, battery_per;

    @Override
    public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging  = status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL;
        if (isCharging){
            battery_charge_status.setText("ON");
        }else {
            battery_charge_status.setText("OFF");
        }

        int batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int batteryScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        int batteryPercent = (batteryLevel*100)/batteryScale;
        battery_per.setText(batteryPercent+"%");
    }

    public ChargingReceiver(TextView battery_charge_status, TextView battery_per){
        this.battery_charge_status = battery_charge_status;
        this.battery_per = battery_per;
    }
}
