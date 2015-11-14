package com.testing.daniel.googlemaps;

import android.content.Context;
import android.location.Location;
import android.view.Gravity;
import android.widget.Toast;

public class Util
{
    public static double getDistanceBetween(double lat1, double lon1, double lat2, double lon2)
    {
        //Distance will be stored in 0, initial bearing in 1, final bearing in 2
        float[] distance = new float[2];

        Location.distanceBetween(lat1, lon1, lat2, lon2, distance);

        return (double)distance[0];
    }

    public static void createToast(String toastMessage, Context context)
    {
        CharSequence toastText = toastMessage;
        Toast toast = Toast.makeText(context, toastText, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }
}
