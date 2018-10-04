package com.example.lively;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.JobIntentService;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    //This is the map activity.By searching a place,sometimes with the help of the city it's in,It creates markers on the results.
    //You can then click the marker to get this places data on the other activity.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //When the maps is ready ,getThe devices location.
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            init();
            mMap.setOnMarkerClickListener(this);
            mMap.setMyLocationEnabled(true);

        }
    }

    private static final String TAG = "MapActivity";

    private RequestQueue requestQueue;
    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private List<Event> elist;
    private EditText editText;
    private Button skip;
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Setting any listeners and Testing the devices internet connectivity
        setContentView(R.layout.activity_map);
        skip = findViewById(R.id.skip_button);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        editText = (EditText) findViewById(R.id.map_search_et);
        elist = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(this);
        ConnectivityManager cm =
                (ConnectivityManager) MapActivity.this
                        .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {


            Toast.makeText(this, "Network connection failed,connect to search place", Toast.LENGTH_LONG).show();
        }
        getLocationPermission();


    }

    private void getDeviceLocation() {

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapActivity.this);

        try {
            if (mLocationPermissionsGranted) {

                final Task location = mFusedLocationProviderClient.getLastLocation();

                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(Task task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Log.d(TAG, "onComplete: found location!");
                            if (task.getResult() == null) {
                                Log.v(TAG, "Check if the app is searching for gps,possible solution restart phone");
                            }
                            //Here the current location is taken ,then it's displayed with a blue dot.
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM, "You're here");

                        } else {
                            Toast.makeText(MapActivity.this, "Device gps error,Try finding location through google maps first", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));


    }

    private void initMap() {
        //here the map is initialised
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapActivity.this);
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    private void init() {
        editText.setMaxLines(1);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                    String searchString = editText.getText().toString();
                    searchString = searchString.replace(' ', '+');
                    fetchPosts(searchString);
//Making the enter key trigger the volley request

                }
                return false;
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                }
            }
        }
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        //Every mapper has an ID.I have made an event list for every possible place found.The number of the marker mathces to the event of the list
        //And so by getting the markerIdToList(marker.getId(),I get the item's position in the list
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("Object", elist.get(markerIdToList(marker.getId())));
        startActivity(intent);
        return false;
    }

    private void fetchPosts(String s) {
        //The query for getting the places id
        StringRequest request = new StringRequest(Request.Method.GET, " https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=" + s + "&inputtype=textquery&key=AIzaSyBA7H3gyHaendvWXEeC-nFkabsnTb-_cP4", onPostsLoaded, onPostsError);
        requestQueue.add(request);

    }

    private void fetchDetails(final String id) {
        //The query for getting the places data and details

        final StringRequest request = new StringRequest(Request.Method.GET, " https://maps.googleapis.com/maps/api/place/details/json?placeid=" + id + "&fields=name,vicinity,geometry,formatted_address,formatted_phone_number&key=AIzaSyBA7H3gyHaendvWXEeC-nFkabsnTb-_cP4", onDetailsLoaded, onPostsError);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                requestQueue.add(request);
            }
        }, 100);

    }

    private final Response.Listener<String> onPostsLoaded = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            //FOR GETTING THE ID
            try {
                JSONObject json = new JSONObject(response);
                JSONArray idarray = json.getJSONArray("candidates");
                if (idarray.length() != 0) {
                    Toast.makeText(MapActivity.this, idarray.length() + " result(s) found", Toast.LENGTH_SHORT).show();


                    for (int i = 0; i < idarray.length(); i++) {
                        JSONObject object = idarray.getJSONObject(i);
                        final String id = object.getString("place_id");
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                fetchDetails(id);
                            }
                        }, 500);

                    }
                } else {

                    Toast.makeText(MapActivity.this, "No results found", Toast.LENGTH_SHORT).show();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };
    private final Response.Listener<String> onDetailsLoaded = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            //FOR GETTING THE DETAILS
            try {
                Event event = new Event();
                JSONObject object = new JSONObject(response);
                JSONObject results = object.getJSONObject("result");
                event.setAddress(results.getString("formatted_address"));
                event.setPhoneNumber(results.getString("formatted_phone_number"));
                event.setHostName(results.getString("name"));
                JSONObject geo = results.getJSONObject("geometry");
                JSONObject latlong = geo.getJSONObject("location");
                event.setLangitude(latlong.getDouble("lat"));
                event.setLongitude(latlong.getDouble("lng"));
                elist.add(event);


                //Every time an item exists ,Im getting the data^ and then I'm creating a marker
                LatLng latLng = new LatLng(event.getLangitude(), event.getLongitude());
                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(event.getHostName());

                mMap.addMarker(options);


                moveCamera(new LatLng(event.getLangitude(), event.getLongitude()), DEFAULT_ZOOM,
                        event.getAddress());


            } catch (JSONException e) {
                e.printStackTrace();
                Event event = new Event();
                JSONObject object = null;
                try {
                    //Usually the only data missing from the place is the phone,So Im redoing the process,just by not getting the phone

                    object = new JSONObject(response);
                    JSONObject results = object.getJSONObject("result");
                    event.setAddress(results.getString("formatted_address"));
                    event.setHostName(results.getString("name"));
                    JSONObject geo = results.getJSONObject("geometry");
                    JSONObject latlong = geo.getJSONObject("location");
                    event.setLangitude(latlong.getDouble("lat"));
                    event.setLongitude(latlong.getDouble("lng"));
                    elist.add(event);


                    LatLng latLng = new LatLng(event.getLangitude(), event.getLongitude());
                    MarkerOptions options = new MarkerOptions()
                            .position(latLng)
                            .title(event.getHostName());


                    mMap.addMarker(options);


                    moveCamera(new LatLng(event.getLangitude(), event.getLongitude()), DEFAULT_ZOOM,
                            event.getAddress());
                } catch (JSONException e1) {
                    Toast.makeText(MapActivity.this, "Data missing from Place,try inserting mannually", Toast.LENGTH_SHORT).show();
                    e1.printStackTrace();
                }

            }


        }
    };

    private final Response.ErrorListener onPostsError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
        }
    };

    //Marker has An m in front of it's id so Im deleting it
    private int markerIdToList(String markerId) {
        StringBuilder sb = new StringBuilder(markerId);
        sb.deleteCharAt(0);
        return Integer.valueOf(sb.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        //initMap(); //Same as before in the discoverEvents,this is for my device
    }
}