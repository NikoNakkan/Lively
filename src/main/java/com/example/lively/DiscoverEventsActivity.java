package com.example.lively;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


//HERE IS THE THE ACTIVITY WHERE THE FIREBASE DATA IS LOADED AND DISPLAYED IN A LIST WITH A RECYCLER VIEW.THE DATA CAN BE
//FILTERED BASED ON GENRE OR LOCATION.*ONLY THE DATA ENTERED THROUGH MAP WILL HAVE THE LOCATION FILTER OPTION.IF MANUALLY ENTERED
//THEY WILL ONLY BE VISIBLE IN NO DISTANCE FITLER OPTION*.I ALSO UPDATE THE DATA WITH A FIREBASE JOB_DISPATCHER AS THE EVENT SHOULD
//NOT BE DISPLAYED IF FINISHED.
//I will use lower from now on,caps seem too much.The algorithm of the filters is not the best but I believe is functional,as you will see ,I'm using
//three lists to make it happen,thus different listeners.I could save any list finally in a single list as the final list and have one listener
// but I thought later of that.I have comments in code as well.
//https://medium.com/wiselteach/firebase-jobdispatcher-androidmonk-3e6d729ed9ce
//^link for the article I got the dispatcher from


public class DiscoverEventsActivity extends AppCompatActivity implements EventsAdapter.EventsClickListener {
    EventsAdapter adapter;
    private static final String TAG = "DiscoverAct";
    RecyclerView recyclerView;
    List<Event> customList;
    List<Event> helperCustomList;
    LinearLayoutManager linearLayoutManager;
    private List<Event> basicList = new ArrayList<>();
    Event event;
    static double latitude;
    static double longitude;
    Spinner genreSpinner;
    static DatabaseReference myR;
    ProgressDialog progress;
    Spinner distanceSpinner;
    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Boolean mLocationPermissionsGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover_events);
        getLocationPermission();

        //Here I make sure that there is internet connection.If not I display a Toast.
        ConnectivityManager cm =
                (ConnectivityManager) DiscoverEventsActivity.this
                        .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {


            Toast.makeText(this, "Network connection failed", Toast.LENGTH_SHORT).show();
        }
        //Here I schedule the job(deleting already finished events.
        scheduleJob(this);
        //A progress bar,it will only stop showing after the list is displayed
        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
        helperCustomList = new ArrayList<>();
        basicList = new ArrayList<>();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbaractdisc);
        setSupportActionBar(toolbar);
        customList = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView = findViewById(R.id.rv_events);
        recyclerView.setLayoutManager(linearLayoutManager);
        genreSpinner = (Spinner) findViewById(R.id.genner_spinner_discover_act);
        final ArrayAdapter<CharSequence> spinneradapter = ArrayAdapter.createFromResource(this,
                R.array.genres_array, android.R.layout.simple_spinner_item);
        spinneradapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genreSpinner.setAdapter(spinneradapter);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference().child("Events");
        myR = myRef;

        genreSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //When the spinner is called.call this method.
                BasicGenreFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        distanceSpinner = (Spinner) findViewById(R.id.distance_spinner);
        ArrayAdapter<CharSequence> distanceAdapter = ArrayAdapter.createFromResource(this,
                R.array.distance_array, android.R.layout.simple_spinner_item);
        distanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distanceSpinner.setAdapter(distanceAdapter);

        distanceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (mLocationPermissionsGranted) {
                    //Call method on filter status change
                    BasicLocationFilter();

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (mLocationPermissionsGranted)
            //Here the making of the list from the firebase data occurs
            firebaseBasicListMake(myRef);
    }


    private double distance(double lat1, double lon1, double lat2, double lon2) {
        //calculates the distance based on two points lats and longs
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private void getDeviceLocation() {

        //Need it for the distance filter
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(DiscoverEventsActivity.this);

        try {
            if (mLocationPermissionsGranted) {

                final Task location = mFusedLocationProviderClient.getLastLocation();

                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(Task task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Log.d(TAG, "onComplete: found location!");
                            if (task.getResult() == null) {
                            }
                            Location currentLocation = (Location) task.getResult();
                            //Here I take the lat and long for the distance filter
                            latitude = currentLocation.getLatitude();
                            longitude = currentLocation.getLongitude();


                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(DiscoverEventsActivity.this, "Device gps error,Try finding location through google maps first", Toast.LENGTH_LONG).show();
                            //^This helps some devices with problematic gps,like my xiaomi device.
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void getLocationPermission() {
        //getting the permissions
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                mLocationPermissionsGranted = true;
                getDeviceLocation();
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Toast.makeText(this, "You need to allow it to see the event list", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    //if permission is granted,get the current position info.
                    getDeviceLocation();
                    mLocationPermissionsGranted = true;
                    firebaseBasicListMake(myR);
                    BasicLocationFilter();
                }
            }
        }
    }

//The two menu options

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.create_live:
                Intent i = new Intent(this.getApplicationContext(), MapActivity.class);
                startActivity(i);
                finish();

        }


        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    @Override
//One of the three list click listeners.The other ones are for the basic and the helping,as I Use the custom most of the three
    //I choose to do all the custom adapter listeners here.
    public void OnListItemClick(int clickedItemPosition) {
        Intent i = new Intent(DiscoverEventsActivity.this, DetailActivity.class);
        i.putExtra("Object", customList.get(clickedItemPosition));
        startActivity(i);


    }

    @Override
    protected void onResume() {
        super.onResume();
        //   getDeviceLocation(); //My own device based problem ,solved in this line.If you have any gps issues try this.
    }

    //Code for the jobScheduler
    public static void scheduleJob(Context context) {
        //creating new firebase job dispatcher
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        Job job = createJob(dispatcher);
        dispatcher.mustSchedule(job);
    }

    public static Job createJob(FirebaseJobDispatcher dispatcher) {

        Job job = dispatcher.newJobBuilder()
                .setLifetime(Lifetime.FOREVER)
                .setService(ScheduledJobService.class)
                .setTag("UniqueTagForYourJob")
                .setReplaceCurrent(false)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(30, 60))
                .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                .setConstraints(Constraint.ON_ANY_NETWORK, Constraint.DEVICE_CHARGING)
                .build();
        return job;
    }

    public static Job updateJob(FirebaseJobDispatcher dispatcher) {
        Job newJob = dispatcher.newJobBuilder()
                .setReplaceCurrent(true)
                .setService(ScheduledJobService.class)
                .setTag("UniqueTagForYourJob")
                .setTrigger(Trigger.executionWindow(30, 60))
                .build();
        return newJob;
    }

    public void cancelJob(Context context) {

        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        dispatcher.cancelAll();
        dispatcher.cancel("UniqueTagForYourJob");

    }
//********************DOWN ARE THE FILTER ALGORITHM AND GETTING FIREBASE DATA********************************************************//


    public void populateBasicList(Boolean stoppingLoading) {
        //The basic list is populated .
        if (stoppingLoading)
            progress.dismiss();
        adapter = new EventsAdapter(getApplicationContext(), basicList, new EventsAdapter.EventsClickListener() {
            @Override
            public void OnListItemClick(int clickedItemPosition) {
                Intent i = new Intent(DiscoverEventsActivity.this, DetailActivity.class);
                i.putExtra("Object", basicList.get(clickedItemPosition));
                startActivity(i);
            }
        });
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }


    private void firebaseBasicListMake(DatabaseReference myRef) {
        //Here i take the firebase data,
        //lines414-419 have to do with the data uploaded from MainActivity AKA UPLOADDATA TO FIREBASE ACTIVITY.
        Intent i = getIntent();
        if (i.hasExtra("MainActivity")) {
            if (i.getStringExtra("MainActivity").equals("MainActivity")) {
                Toast.makeText(this, "Uploaded", Toast.LENGTH_SHORT).show();
            }
        }

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                showData(dataSnapshot);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public void showData(DataSnapshot dataSnapsh) {
        basicList.clear();
        //here all the database events are taken
        for (DataSnapshot ds : dataSnapsh.getChildren()) {
            event = ds.getValue(Event.class);
            basicList.add(event);
        }
        //Here the stoppingLoading is good to be set true.
        populateBasicList(true);
    }


    public void BasicGenreFilter() {
        //Here I say,if genre filter is set at no filter and the other one is not,call the basic location filter
        //which will just filter the locations correctly.Then filteredListLocationGenreFinisher where the correct list will be set to the
        //adapter.
        if (genreSpinner.getSelectedItem().toString().equals("No filter") && !distanceSpinner.getSelectedItem().toString().equals("No filter")) {
            BasicLocationFilter();
            return;
        }

//typical stuff
        customList.clear();
        for (Event item : basicList) {
            if (item.getGenre().equals(genreSpinner.getSelectedItem().toString())) {
                customList.add(item);
            }
        }
        adapter = new EventsAdapter(getApplicationContext(), customList, this);

        //The point of the filtered finisher options,is to apply the second filter ,after the first filter is triggered.
        filteredListGenreLocFinisher();
    }

    public void BasicLocationFilter() {

        //Same as the BasicGenreFilter.
        if (distanceSpinner.getSelectedItem().toString().equals("No filter") && !genreSpinner.getSelectedItem().toString().equals("No filter")) {
            BasicGenreFilter();
            return;
        }
        //The rest ,will decide what the contents of the list will be.
        customList.clear();

        if (distanceSpinner.getSelectedItem().toString().equals("Near events")) {
            for (Event item : basicList) {

                if (20.0 > distance(latitude, longitude, item.getLangitude(), item.getLongitude()) && distance(latitude, longitude, item.getLangitude(), item.getLongitude()) > 0.0) {
                    customList.add(item);

                }
            }
            //Opposite to the locat.Finisher,the genrefinisher will double filter the content.
            filteredListLocationGenreFinisher();

        } else if (distanceSpinner.getSelectedItem().toString().equals("Medium distance events")) {
            for (Event item : basicList) {


                if (500.0 > distance(latitude, longitude, item.getLangitude(), item.getLongitude()) && distance(latitude, longitude, item.getLangitude(), item.getLongitude()) > 20.001) {
                    customList.add(item);

                }
            }
            filteredListLocationGenreFinisher();
        } else if (distanceSpinner.getSelectedItem().toString().equals("Global Events")) {
            for (Event item : basicList) {


                if (100000000000.00 > distance(latitude, longitude, item.getLangitude(), item.getLongitude()) && distance(latitude, longitude, item.getLangitude(), item.getLongitude()) > 500.00001) {
                    if (item.getLangitude() != 0 && item.getLongitude() != 0)  //IF SOMEONE ADDS MANUALLY A PLACES THAT IS NOT ON MAPS YET,LAT AND LONG WILL BE 0,
                        customList.add(item);                       //THOSE PLACES WILL ONLY BE FOUND IN THE CASE WHERE LOCATION FILTER IS SET IN NO FILTER
                    //Extra info,the location where 0,0 occurs is in the sea,so for the next loooots years,probably nothing will be there :)
                    // This location is in the tropical waters of the eastern Atlantic Ocean, specifically, the Gulf of Guinea.


                }
            }


            filteredListLocationGenreFinisher();

        }
        //The last possible if would be both no filter.I have a taken care of that in genrefinisher.
        else filteredListLocationGenreFinisher();
    }

    void filteredListLocationGenreFinisher() {

//If both are no filter,Just do the populatebasiclist,where it just sets the adapter to the data from the firebase unchanged.
        //If i hadn't done it,It would crash for me.
        if (genreSpinner.getSelectedItem().toString().equals("No filter")) {
            if (distanceSpinner.getSelectedItem().toString().equals("No filter")) {
                populateBasicList(false);       //also,I have set a flag so it would only stopped loading if it was called
                return;                                      //from the method where the firebase data are taken.Else this would be called
                //first thing in onCreate

            }
            //If only the genre is set to no filter,the list is ready from the previous method and good to go.
            adapter = new EventsAdapter(getApplicationContext(), customList, this);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            return;
        }
        //If not ,do the the genre filtering.I could't reuse the code as some things are different here.
        helperCustomList.clear();
        for (Event item : customList) {
            if (item.getGenre().equals(genreSpinner.getSelectedItem().toString())) {
                helperCustomList.add(item);
            }
        }
        adapter = new EventsAdapter(getApplicationContext(), helperCustomList, new EventsAdapter.EventsClickListener() {
            @Override
            public void OnListItemClick(int clickedItemPosition) {
                Intent i = new Intent(DiscoverEventsActivity.this, DetailActivity.class);
                i.putExtra("Object", helperCustomList.get(clickedItemPosition));
                startActivity(i);
            }
        });
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void filteredListGenreLocFinisher() {
        //Same as the  genre finisher
        if (distanceSpinner.getSelectedItem().toString().equals("No filter")) {
            if (genreSpinner.getSelectedItem().toString().equals("No filter")) {
                populateBasicList(false);
                return;
            }
            adapter = new EventsAdapter(getApplicationContext(), customList, this);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            return;
        }
        helperCustomList.clear();
        if (distanceSpinner.getSelectedItem().toString().equals("Near events")) {
            for (Event item : customList) {
                Log.v("SOOOONA", String.valueOf(distance(latitude, longitude, item.getLangitude(), item.getLongitude())));

                if (20.0 > distance(latitude, longitude, item.getLangitude(), item.getLongitude()) && distance(latitude, longitude, item.getLangitude(), item.getLongitude()) > 0.0) {
                    helperCustomList.add(item);
                }
            }
        }
        if (distanceSpinner.getSelectedItem().toString().equals("Medium distance events")) {
            for (Event item : customList) {


                if (500.0 > distance(latitude, longitude, item.getLangitude(), item.getLongitude()) && distance(latitude, longitude, item.getLangitude(), item.getLongitude()) > 20.00001) {
                    helperCustomList.add(item);
                }
            }
        }
        if (distanceSpinner.getSelectedItem().toString().equals("Global Events")) {
            for (Event item : customList) {
                if (100000000000.00 > distance(latitude, longitude, item.getLangitude(), item.getLongitude()) && distance(latitude, longitude, item.getLangitude(), item.getLongitude()) > 500.00001) {
                    helperCustomList.add(item);
                }
            }
        }
        adapter = new EventsAdapter(getApplicationContext(), helperCustomList, new EventsAdapter.EventsClickListener() {
            @Override
            public void OnListItemClick(int clickedItemPosition) {
                Intent i = new Intent(DiscoverEventsActivity.this, DetailActivity.class);
                i.putExtra("Object", helperCustomList.get(clickedItemPosition));
                startActivity(i);
            }
        });
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }


}