package com.testing.daniel.googlemaps;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class StopAdapter extends ArrayAdapter<Stop>
{
    public StopAdapter(Context context, int textViewResourceId)
    {
        super(context, textViewResourceId);
    }

    public StopAdapter(Context context, int resourceId, ArrayList<Stop> stops)
    {
        super(context, resourceId, stops);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view = convertView;

        if(view == null)
        {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.row, null);
        }

        Stop s = getItem(position);

        if(s != null)
        {
            TextView tView1 = (TextView)view.findViewById(R.id.stop_text_number);
            TextView tView2 = (TextView)view.findViewById(R.id.stop_text_desc);

            if(tView1 != null)
                tView1.setText("Stop " +s.getStopNum());

            if(tView2 != null)
                tView2.setText("at " +s.getStopName());
        }

        return view;
    }
}
