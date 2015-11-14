package com.testing.daniel.googlemaps;

import com.google.android.gms.maps.model.LatLng;

public class Stop
{
    private LatLng coords;      //The latitude and longitude of the stop (in that order)
    private String stopName;    //A one sentence description of the stop
    private boolean isActive;   //Whether the stop is the focus of the application
    private String stopNum;

    public Stop(double latitude, double longitude, String stopName, String stopNum)
    {
        this.coords = new LatLng(latitude,longitude);
        this.stopName = stopName;
        //Any created bus stop will be inactive by default
        this.isActive = false;
        this.stopNum = stopNum;
    }

    public String getStopName()
    {
        return stopName;
    }

    public String getStopNum()
    {
        return stopNum;
    }

    public LatLng getCoords()
    {
        return coords;
    }
}
