package com.example.ankurkhandelwal.routemap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<LocationSettingsResult> {
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    Toolbar toolbar;

    protected static final String TAG = "MapActivity";
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000*60;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    protected LocationSettingsRequest mLocationSettingsRequest;
    protected Location mCurrentLocation;
    protected Boolean mRequestingLocationUpdates;
    protected String mLastUpdateTime;

    ProgressBar progressBar;
    ProgressDialog progressDialog;
    TextView pickup_detail,drop_detail;
    PolylineOptions lineOptions = null;
    public static TextView time_view,distance_view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        toolbar=(Toolbar) findViewById(R.id.toolbar_map);
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.drawable.map_logo);
        toolbar.inflateMenu(R.menu.menu_map);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                reset_location_data();
                return true;
            }
        });
        time_view=(TextView) findViewById(R.id.time_view);
        distance_view=(TextView) findViewById(R.id.distance_view);
        pickup_detail=(TextView) findViewById(R.id.pickup_detail);
        drop_detail=(TextView) findViewById(R.id.drop_details);
        pickup_detail.setText("PickUp : "+ Common.pickPoint +" 10:00 ");
        drop_detail.setText("Drop : "+ Common.dropPoint +" 12:00");
        set_map();
        mLastUpdateTime = "";
        mRequestingLocationUpdates=false;
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
    }

    public void set_map(){
        setUpMapIfNeeded();
        addMarkers();
        String url = getMapDirection();
        ReqAsyncTask task = new ReqAsyncTask();
        task.execute(url);

    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(MapsActivity.this)
                .addConnectionCallbacks(MapsActivity.this)
                .addOnConnectionFailedListener(MapsActivity.this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        mLocationSettingsRequest = builder.build();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        if(null!=mGoogleApiClient) {
            if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
//                startLocationUpdates();
            }
        }
    }

    private void addMarkers() {
        if (mMap != null) {
            mMap.addMarker(new MarkerOptions().position(Common.pickselected).title("Pick Up Point"));
            mMap.addMarker(new MarkerOptions().position(Common.dropselected).title("Drop Point"));
        }
    }

    private String getMapDirection() {
        String url="https://maps.googleapis.com/maps/api/directions/json?"+ "origin=" + Common.pickselected.latitude + "," + Common.pickselected.longitude
                + "&destination=" + Common.dropselected.latitude + "," + Common.dropselected.longitude
                + "&sensor=false&units=metric&mode=driving";
        Log.d("waypoint_url ", url);
        return url;
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(Common.pickselected).title("Pick Up point"));
        mMap.addMarker(new MarkerOptions().position(Common.dropselected).title("Drop point"));
        mMap.setTrafficEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Common.pickselected, 13));
    }


    private class ReqAsyncTask extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try{
                // Fetching the data from web service
                HttpConnection http = new HttpConnection();
                Log.d("ReqAsyncTask ",url[0]);
                data = http.readUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{
        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try{
                jObject = new JSONObject(jsonData[0]);
                JSONPathParser parser = new JSONPathParser();
                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();
                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);
                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(15);
                lineOptions.color(Color.BLUE);
            }
            // Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);
            set_camera_position();
            distance_view.setText(Common.distance);
            time_view.setText(Common.time);
//            distance_between.setText(distance);
//            time_taken.setText(duration);
        }
    }

    private void set_camera_position(){
        Projection projection = mMap.getProjection();
        LatLng markerLocation = Common.pickselected;
        Point screenPosition = projection.toScreenLocation(markerLocation);
        Point mappoint = mMap.getProjection().toScreenLocation(Common.pickselected);
        mappoint.set(mappoint.x, mappoint.y-30);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(mMap.getProjection().fromScreenLocation(mappoint)));

    }

    @Override
    public void onBackPressed(){
        Common.pickselected=null;
        Common.dropselected=null;
        mMap.clear();
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);
        MenuItem menu_refresh = menu.findItem(R.id.action_refresh);
        menu_refresh.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                reset_location_data();
                return false;
            }
        });
        return true;
    }

    private void reset_location_data() {
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            checkLocationSettings();
            if (mCurrentLocation != null) {
                LatLng user_location= new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
                Common.pickselected=user_location;
                mMap.clear();
                Toast.makeText(getApplicationContext(), "User location changed", Toast.LENGTH_LONG).show();
                set_map();
            }
        }else {
            LatLng user_location= new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
            Common.pickselected=user_location;
            mMap.clear();
            Toast.makeText(getApplicationContext(), "User location changed", Toast.LENGTH_LONG).show();
            set_map();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.action_refresh:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
       return super.onPrepareOptionsMenu(menu);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient!= null) {
            mGoogleApiClient.connect();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(null!=mGoogleApiClient) {
            if (mGoogleApiClient.isConnected()) {
//                stopLocationUpdates();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != mGoogleApiClient) {
            mGoogleApiClient.disconnect();
        }
    }

    protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );
        result.setResultCallback(this);
    }

    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i(TAG, "All location settings are satisfied.");
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");
                try {
                    status.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i(TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(),"No Date",Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        this.finish();
                        break;
                }
                break;
        }
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                mRequestingLocationUpdates = true;
            }
        });

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            checkLocationSettings();
            if (mCurrentLocation != null) {
                set_user_location(mCurrentLocation);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        set_user_location(mCurrentLocation);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    private void set_user_location(Location location) {
        LatLng user_location= new LatLng(location.getLatitude(),location.getLongitude());
//        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
//        try {
//            List<Address> addresses = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
//            user_location_text.setText(addresses.get(0).getAddressLine(0) + " " + addresses.get(0).getSubLocality());
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
