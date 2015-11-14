package com.testing.daniel.googlemaps;

import android.app.DialogFragment;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends ActionBarActivity implements ConnectionCallbacks,
                                                              OnConnectionFailedListener,
                                                              OnMapReadyCallback,
                                                              LocationListener,
                                                              OnMapClickListener,
                                                              AddStopDialogFragment.AddStopDialogListener,
                                                              AddStopMenuDialogFragment.AddStopMenuDialogListener
{

    //GOOGLE API'S
    private GoogleMap mMap;                             //Google Map object itself
    private GoogleApiClient mGoogleApiClient;           //Allows connection to Google Play Services
    private Location mLastLocation;                     //Location from Google Maps API
    private LocationRequest mLocationRequest;           //LocationRequest from Google Maps API

    //USER VARIABLES
    private LatLng currentTouchCoords = null;           //The last place the user touched on the map
    private Map<String, Stop> busStops;                //List of bus stops, keyed by stop number

    //CONSTANTS
    protected static final String TAG = "Maps-Activity"; //Debug log prepend
    protected static final long UPDATE_INTERVAL_MILLISECONDS = 8000;
    protected static final long UPDATE_INTERVAL_QUICKEST = (UPDATE_INTERVAL_MILLISECONDS)/2;

    /*
     * First method called when an activity starts.
     * @param A mapping of strings to values to allow retrieval of state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //Set the content of the view based off the specified layout (found in /res/layout)
        setContentView(R.layout.activity_maps);

        //Connect to Google Play services
        buildGoogleApiClient();

        //MapFragment represents a wrapper around a view of a map, thus handles it's own lifecycle
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        //getMap is deprecated. Async call must occur in main thread.
        //getMapAsync guarantees a non-null GoogleMap instance.
        mapFragment.getMapAsync(this);

        //Set-up for later requests for location data from Google Play services
        createLocationRequest();

        busStops = new HashMap<String, Stop>();
    }

    /*
     * Used to inflate the menu items viewable within the action bar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activities_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /*
     * Called when an action bar item is clicked
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int action = item.getItemId();

        if(action == R.id.action_add)
        {
            Log.d(TAG, "Add Stop action pressed");
            showAddBusStopMenu();
        }
        else if(action == R.id.action_list)
        {
            Log.d(TAG, "List action pressed");
        }
        else if(action == R.id.action_settings)
        {
            Log.d(TAG, "Settings action pressed");
        }

        return super.onOptionsItemSelected(item);
    }

    private void showAddBusStopMenu()
    {
        //Create the add bus stop menu dialog
        DialogFragment addMenuDialog = new AddStopMenuDialogFragment();
        addMenuDialog.show(getFragmentManager(), "Add Stop Menu");
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(mGoogleApiClient.isConnected())
            startLocationUpdates();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if(mGoogleApiClient.isConnected())
            stopLocationUpdates();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (mGoogleApiClient.isConnected())
        {
            mGoogleApiClient.disconnect();
        }
    }

    /*
     * Used to connect to Google Play services. Configures the
     * API client with the Builder class, specifying which services to
     * connect to and the callbacks required upon success/failure
     */
    protected synchronized void buildGoogleApiClient()
    {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /*
     * Creates a LocationRequest object for later use in acquiring location data from
     * Google Play Services. Specifies the update interval and priority required.
     * For details on each accuracy constant see
     * {@link https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest}
     */
    protected void createLocationRequest()
    {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_MILLISECONDS);
        mLocationRequest.setFastestInterval(UPDATE_INTERVAL_QUICKEST);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /*
     * Method called if connection to Google Play services was successful.
     * This connection is facilitated through GoogleApiClient.connect, called from onStart()
     *
     * @param Bundle of data provided by Google Play services (may be null)
     */
    @Override
    public void onConnected(Bundle connectionHint)
    {
        Log.d(TAG, "Connected to Google Play Services!");

        //Acquire the last known location of the phone
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation != null)
        {
            Log.d(TAG, "Latitude: " +String.valueOf(mLastLocation.getLatitude()));
            Log.d(TAG, "Longitude: " +String.valueOf(mLastLocation.getLongitude()));

            //Move the camera to the users location
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 13));
        }

        //Once the last known location is available, keep probing for location updates
        startLocationUpdates();

        //Set up callback for touch interactions on the map
        mMap.setOnMapClickListener(this);
    }

    /*
     * Requests location updates from the FusedLocationApi
     */
    protected void startLocationUpdates()
    {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    /*
     * Requests location updates CEASE from the FusedLocationApi
     */
    protected void stopLocationUpdates()
    {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    /*
     * Callback from LocationListener interface. Updates location based off of
     * parameters specified in createLocationRequest()
     */
    @Override
    public void onLocationChanged(Location location)
    {
        mLastLocation = location;
        //Log.d(TAG, "Updated Latitude: " +String.valueOf(mLastLocation.getLatitude()));
        //Log.d(TAG, "Updated Longitude: " +String.valueOf(mLastLocation.getLongitude()));
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // This callback is important for handling errors that
        // may occur while attempting to connect with Google.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onMapReady(GoogleMap map)
    {
        //The location for Perth, Western Australia, Australia
        LatLng startLocation = new LatLng(-31.952, 115.859);

        //The little blue dot on the map of the users location
        map.setMyLocationEnabled(true);

        if(mLastLocation != null)
        {
            startLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        }

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 13));

        //Give the rest of the class access to the map
        mMap = map;
    }

    /*
     * Callback from the OnMapClickListener interface.
     */
    @Override
    public void onMapClick(LatLng point)
    {
        Log.i(TAG, "User clicked on Map at point: Lat: " +point.latitude + "    Long: " +point.longitude);

        //Create the dialog to see whether the user wants to add a stop
        DialogFragment onMapDialog = new AddStopDialogFragment();
        onMapDialog.show(getFragmentManager(), "Add Stop");

        //Update the last point touched
        currentTouchCoords = point;
    }

    /*
     * Callback from the AddStopDialogListener interface. This is called only
     * when the positive action is chosen after clicking on on the map.
     * This method makes asynchronous requests to the Transperth API
     */
    @Override
    public void onDialogPositiveClick(DialogFragment dialog)
    {
        /*StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        */
        new AsyncTransperthNearRequest().execute("?near=",
                currentTouchCoords.latitude + "," + currentTouchCoords.longitude);
    }

    @Override
    public void onStopMenuDialogPositiveClick(String editText, int checkedButtonId)
    {
        //If the user wanted to add a stop from the menu at their location
        if(checkedButtonId == R.id.radio_add_closest)
        {
            new AsyncTransperthNearRequest().execute("?near=",
                    mLastLocation.getLatitude() + "," + mLastLocation.getLongitude());
        }
        //User wants to add a stop based on the stop number
        else if(checkedButtonId == R.id.radio_add_number)
        {
            new AsyncTransperthBusstopRequest().execute("/", editText);
        }
    }

    /*
     * This inner class is used to make an asynchronous request to the Transperth API.
     * This sort of procedure is not allowed on the main thread.
     */
    private class AsyncTransperthNearRequest extends AsyncTask<String, Void, String>
    {
        //Function used to do the async work.
        //Function requests data from the API and returns it
        //This return value results in being the parameter of onPostExecute
        protected String doInBackground(String... params)
        {
            String jsonText = null;
            try
            {
                HttpClient defaultHttpClient = new DefaultHttpClient();
                HttpGet httpRequest = new HttpGet("http://api.perthtransit.com/1/bus_stops"
                                                  + params[0] + params[1]);

                HttpResponse httpResponse = defaultHttpClient.execute(httpRequest);

                BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"));

                jsonText = reader.readLine();
                reader.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            return jsonText;
        }

        protected void onPostExecute(String jsonText)
        {
            Stop closestStop = null;

            try
            {
                //Start to parse the JSON object retrieved from Transperth API
                //First the JSON object itself is retrieved
                JSONObject jObject = new JSONObject(jsonText);
                //Then the array of the 5 closest stops within that object
                JSONArray jsonArray = jObject.getJSONArray("response");

                //Iterate through the array to get the closest stop
                if(jsonArray.length() > 0)
                    closestStop = findClosestStopOf5(jsonArray);

                //Add the marker at that stop if there is a stop to add
                if(closestStop != null)
                {
                    mMap.addMarker(new MarkerOptions()
                            .title("Stop Number: " +closestStop.getStopNum())
                            .snippet(closestStop.getStopName())
                            .position(closestStop.getCoords()));
                }
                else
                {
                    //Show a toast to notify user if a stop is not found
                    Util.createToast("No bus stop found nearby", getApplicationContext());
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        private Stop findClosestStopOf5(JSONArray stopArray)
        {
            Stop closestStop = null;

            try
            {
                int minDistanceIndex = 0;
                double stopDistance = 0.0;
                //Get a stop distance to compare to for the minimum distance
                JSONObject firstStop = stopArray.getJSONObject(0);
                double minDistance = Util.getDistanceBetween(firstStop.getDouble("lat"), firstStop.getDouble("lng"),
                        currentTouchCoords.latitude, currentTouchCoords.longitude);


                for (int i = 0; i < stopArray.length(); i++)
                {
                    //Get each individual stop object
                    JSONObject stop = stopArray.getJSONObject(i);

                    //Check the distance between it and the spot the user clicked
                    stopDistance = Util.getDistanceBetween(stop.getDouble("lat"), stop.getDouble("lng"),
                            currentTouchCoords.latitude, currentTouchCoords.longitude);

                    if(stopDistance < minDistance)
                    {
                        minDistance = stopDistance;
                        minDistanceIndex = i;
                    }

                    String stopString = i + stop.getString("stop_number") + "  " + stop.getString("name")
                                        +"  " +stopDistance +"m";
                    Log.d(TAG, stopString);
                }

                //Construct the stop closest to the users touch
                closestStop = new Stop(stopArray.getJSONObject(minDistanceIndex).getDouble("lat"),
                                  stopArray.getJSONObject(minDistanceIndex).getDouble("lng"),
                                  stopArray.getJSONObject(minDistanceIndex).getString("name"),
                                  stopArray.getJSONObject(minDistanceIndex).getString("stop_number"));

                busStops.put(closestStop.getStopNum(), closestStop);
            }
            catch(JSONException e)
            {
                e.printStackTrace();
            }

            return closestStop;

        }
    }

    private class AsyncTransperthBusstopRequest extends AsyncTransperthNearRequest
    {
        protected void onPostExecute(String jsonText)
        {
            Log.d(TAG, jsonText);
            try
            {
                JSONObject jsonObject = new JSONObject(jsonText);
                if(jsonObject.has("error"))
                {
                    //Show a toast to notify user if a stop is not found
                    Util.createToast("Requested Stop Does Not Exist", getApplicationContext());
                }
                else
                {
                    JSONObject stopObject = jsonObject.getJSONObject("response");

                    Stop stop = new Stop(stopObject.getDouble("lat"),
                            stopObject.getDouble("lng"),
                            stopObject.getString("name"),
                            stopObject.getString("stop_number"));

                    busStops.put(stopObject.getString("stop_number"), stop);

                    mMap.addMarker(new MarkerOptions()
                            .title("Stop Number: " +stop.getStopNum())
                            .snippet(stop.getStopName())
                            .position(stop.getCoords()));
                }
            }
            catch(JSONException e)
            {
                e.printStackTrace();
            }
        }
    }
}