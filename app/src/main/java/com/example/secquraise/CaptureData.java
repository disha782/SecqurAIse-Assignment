package com.example.secquraise;

public class CaptureData {
    private String internetConnectivity;
    private String batteryStatus;
    private String batteryPercentage;

    public CaptureData(String internetConnectivity, String batteryStatus, String batteryPercentage, String location, String timestamp) {
    }

    public String getInternetConnectivity() {
        return internetConnectivity;
    }

    public void setInternetConnectivity(String internetConnectivity) {
        this.internetConnectivity = internetConnectivity;
    }

    public String getBatteryStatus() {
        return batteryStatus;
    }

    public void setBatteryStatus(String batteryStatus) {
        this.batteryStatus = batteryStatus;
    }

    public String getBatteryPercentage() {
        return batteryPercentage;
    }

    public void setBatteryPercentage(String batteryPercentage) {
        this.batteryPercentage = batteryPercentage;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    private String location;
    private String timestamp;


}
