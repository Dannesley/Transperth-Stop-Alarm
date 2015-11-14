package com.testing.daniel.googlemaps;

import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;


public class ViewStopsListActivity extends ActionBarActivity
{
    private ArrayList<Stop> stops;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_stops_list);

        getDataFromParent();
        populateListView();

    }

    private void getDataFromParent()
    {
        if(getIntent().getParcelableArrayListExtra("k") != null)
        {
            stops = getIntent().getParcelableArrayListExtra("k");
            for (Stop s : stops)
            {
                Log.d("View-Stops", s.getStopName() + " " + s.getStopNum());
            }
        }
    }

    private void populateListView()
    {
        ListView listView = (ListView) findViewById(R.id.stopListView);
        ArrayAdapter<Stop> arrayAdapter = new ArrayAdapter<Stop>(
                this,
                android.R.layout.simple_list_item_1,
                stops);

        listView.setAdapter(arrayAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_stops_list, menu);

        //Hide the "up" menu. I only want the users to be able to go "back" on the stack.
        //This allows the maintenance of the parent activity state
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
        {
            actionBar.setHomeButtonEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
