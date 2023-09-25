package com.example.secquraise;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Build;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;

public class ChargingReceiver extends BroadcastReceiver {
    private TextView battery_charge_status, battery_per;

    @Override
    public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging  = status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL;
        if (isCharging){
            battery_charge_status.setText(R.string.on_status);
        }else {
            battery_charge_status.setText(R.string.off_status);
        }

        int batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int batteryScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        int batteryPercent = (batteryLevel*100)/batteryScale;
        battery_per.setText(batteryPercent+"%");
        if(batteryPercent <= 20){
            sendNotification(context, "Low Battery", "Battery is below 20%");
        }
    }

    public ChargingReceiver(TextView battery_charge_status, TextView battery_per){
        this.battery_charge_status = battery_charge_status;
        this.battery_per = battery_per;
    }

    private void sendNotification(Context context, String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("channel_id", "Channel Name", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_id")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(1, builder.build());
    }
}
