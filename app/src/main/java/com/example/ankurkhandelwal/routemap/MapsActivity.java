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
import com.google.android.gms.maps.model.Marker;
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

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMarkerClickListener{
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    Toolbar toolbar;
    String TAG="MapsActivity";
    ProgressBar progressBar;
    ProgressDialog progressDialog;
    TextView pickup_detail,drop_detail;
    PolylineOptions lineOptions = null;
    public static TextView time_view_pick,distance_view_pick,time_view_user,distance_view_user;
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
        time_view_user=(TextView) findViewById(R.id.time_view_user);
        distance_view_user=(TextView) findViewById(R.id.distance_view_user);
        time_view_pick=(TextView) findViewById(R.id.time_view_pick);
        distance_view_pick=(TextView) findViewById(R.id.distance_view_pick);
        pickup_detail=(TextView) findViewById(R.id.pickup_detail);
        drop_detail=(TextView) findViewById(R.id.drop_details);
        pickup_detail.setText("PickUp : "+ Common.pickPoint +" 10:00 ");
        drop_detail.setText("Drop : " + Common.dropPoint + " 01:00");
        Log.d(TAG, "onCreate");
        set_map();
        mMap.setOnMarkerClickListener(this);
    }

    private void reset_location_data() {
        mMap.clear();
        setUpMap();
        set_map();
    }

    public void set_map(){
        Log.d(TAG, "set_map_called");
        setUpMapIfNeeded();
        addMarkers();
        String url = getMapDirection(Common.pickselected,Common.dropselected);
        ReqAsyncTask task = new ReqAsyncTask();
        task.execute(url);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        Log.d(TAG, "onResume");
    }

    private void addMarkers() {
        if (mMap != null) {
            mMap.addMarker(new MarkerOptions().position(Common.pickselected).title("Pick Up Point"));
            mMap.addMarker(new MarkerOptions().position(Common.dropselected).title("Drop Point"));
        }
    }

    private String getMapDirection(LatLng location1,LatLng location2) {
        String url="https://maps.googleapis.com/maps/api/directions/json?"+ "origin=" + location1.latitude + "," + location1.longitude
                + "&destination=" + location2.latitude + "," + location2.longitude
                + "&sensor=false&units=metric&mode=driving";
        Log.d("waypoint_url ", url);
        return url;
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            Log.d(TAG,"map is null");
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            setUpMap();
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
//                Log.d("ReqAsyncTask ",url[0]);
                data = http.readUrl(url[0]);
            }catch(Exception e){
//                Log.d("Background Task", e.toString());
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
            distance_view_pick.setText(Common.distance.get(0));
            time_view_pick.setText(Common.time.get(0));
            set_user_pick_map();
        }
    }

    private void set_user_pick_map() {
        String url_from_user = getMapDirection(Common.userLocation,Common.pickselected);
        ReqTask task_new = new ReqTask();
        task_new.execute(url_from_user);
    }

    private class ReqTask extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try{
                // Fetching the data from web service
                HttpConnection http = new HttpConnection();
//                Log.d("ReqAsyncTask ",url[0]);
                data = http.readUrl(url[0]);
            }catch(Exception e){
//                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParTask parserTask = new ParTask();
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    private class ParTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{
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
                lineOptions.color(Color.RED);
            }
            // Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);
            set_camera_position();
            distance_view_user.setText(Common.distance.get(1));
            time_view_user.setText(Common.time.get(1));
//            set_user_pick_map();
//            distance_between.setText(distance);
//            time_taken.setText(duration);
        }
    }

    private void set_camera_position(){
        Point mappoint = mMap.getProjection().toScreenLocation(Common.userLocation);
        mappoint.set(mappoint.x, mappoint.y - 30);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(mMap.getProjection().fromScreenLocation(mappoint)));
        Marker marker= mMap.addMarker(new MarkerOptions().position(Common.userLocation).title("Your location"));
        onMarkerClick(marker);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.setTitle("Your Location");
        return false;
    }


    @Override
    public void onBackPressed(){
        Common.pickselected=null;
        Common.dropselected=null;
        Common.distance.clear();
        Common.time.clear();
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
//                reset_location_data();
                return false;
            }
        });
        return true;
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
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
