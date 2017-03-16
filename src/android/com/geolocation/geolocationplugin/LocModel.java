package com.geolocation.geolocationplugin;

public class LocModel {
    private String Longitude, Latitude, Distance;

    LocModel(String Longitude, String Latitude, String Distance){
        this.Longitude = Longitude;
        this.Latitude = Latitude;
        this.Distance = Distance;
    }

    public String getLongitude() {
        return Longitude;
    }

    public String getLatitude() {
        return Latitude;
    }

    public String getDistance() {
        return Distance;
    }
}
