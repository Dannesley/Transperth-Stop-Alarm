package com.testing.daniel.googlemaps;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class Stop implements Parcelable
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

    //BELOW HERE ARE PARCEL METHODS
    public int describeContents(){return 0;}

    //Write objects data to the passed in Parcel
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeDouble(coords.latitude);
        out.writeDouble(coords.longitude);
        out.writeString(stopName);
        out.writeString(stopNum);
    }

    //Used to regenerate the object
    public static final Parcelable.Creator<Stop> CREATOR = new Parcelable.Creator<Stop>()
    {
        public Stop createFromParcel(Parcel in)
        {
            return new Stop(in);
        }

        public Stop[] newArray(int size)
        {
            return new Stop[size];
        }
    };

    //Parcel constructor
    private Stop(Parcel in)
    {
        this.coords = new LatLng(in.readDouble(), in.readDouble());
        this.stopName = in.readString();
        this.stopNum = in.readString();
        this.isActive = false;
    }
}
