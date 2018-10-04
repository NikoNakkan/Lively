package com.example.lively;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Templates;

public class MainActivity extends AppCompatActivity {
    //This is where the Manual event making occurs,and where the button to upload data to firebase is located.
    private static final String TAG = "MainActivity";
    private TextView dateTextView;
    private DatePickerDialog.OnDateSetListener mDataSetListener;
    private TextView timeTextView;
    private Button uploadEvButton;
    private Event event;
    private Spinner genreSpinner;
    private EditText adressEt;
    private EditText phoneEt;
    private EditText artistNameEt;
    private EditText hostNameEt;
    private EditText entranceEt;
    private EditText notesEt;

    private static final int ERROR_DIALOG_REQUEST = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //basic view and spinner setting
        super.onCreate(savedInstanceState);
        event = new Event();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarmainactivity);
        setSupportActionBar(toolbar);
        adressEt = findViewById(R.id.address_name_et);
        artistNameEt = findViewById(R.id.artist_name_et);
        entranceEt = findViewById(R.id.entrance_fee_et);
        phoneEt = findViewById(R.id.phone_et);
        hostNameEt = findViewById(R.id.host_name_et);
        notesEt = findViewById(R.id.notes_et);
        genreSpinner = (Spinner) findViewById(R.id.spinner_genre);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.genres_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genreSpinner.setAdapter(adapter);

        //the time is taken in the following lines,as well as the date
        dateTextView = findViewById(R.id.date_tv);
        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(MainActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth, mDataSetListener,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

            }
        });
        mDataSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month++;
                dateTextView.setText(year + "-" + month + "-" + day);

            }
        };
        timeTextView = findViewById(R.id.time_tv);
        timeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {

                        timeTextView.setText(String.format("%02d:%02d", hour, minute));
                    }
                }, hour, minute, false);
                timePickerDialog.show();
            }
        });
        uploadEvButton = findViewById(R.id.send_data_button);
        uploadEvButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager cm =
                        (ConnectivityManager) MainActivity.this
                                .getSystemService(Context.CONNECTIVITY_SERVICE);
//Before I upload,I have to check if the connection is ok,and also,if all the data is filled with some text.
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();
                if (!isConnected) {


                    Toast.makeText(MainActivity.this, "Network connection failed", Toast.LENGTH_SHORT).show();
                    return;
                }


                if (String.valueOf(hostNameEt.getText()).equals("") || String.valueOf(adressEt.getText()).equals("") ||
                        String.valueOf(artistNameEt.getText()).equals("") || String.valueOf(notesEt.getText()).equals("") ||
                        String.valueOf(genreSpinner.getSelectedItem().toString()).equals("No filter") ||
                        String.valueOf(dateTextView.getText()).equals("") ||
                        String.valueOf(timeTextView.getText()).equals("") ||
                        String.valueOf(entranceEt.getText()).equals("")) {
                    Toast.makeText(MainActivity.this, "Please fill all the info", Toast.LENGTH_SHORT).show();
                    return;
                }

                event.setHostName(String.valueOf(hostNameEt.getText()));
                event.setArtistName(String.valueOf(artistNameEt.getText()));
                event.setArtistComment(String.valueOf(notesEt.getText()));
                event.setAddress(String.valueOf(adressEt.getText()));
                event.setPhoneNumber(String.valueOf(phoneEt.getText()));
                event.setGenre(genreSpinner.getSelectedItem().toString());
                event.setDateTime(String.valueOf(dateTextView.getText() + " " + String.valueOf(timeTextView.getText())));
                event.setPrice(String.valueOf(entranceEt.getText()));
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference();
                Toast.makeText(MainActivity.this, "Wait to upload", Toast.LENGTH_SHORT).show();

                myRef.child("Events").push().setValue(event, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        Intent i = new Intent(MainActivity.this, DiscoverEventsActivity.class);
                        i.putExtra(TAG, TAG);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);


                    }
                });


            }
        });
//Here I get the data from the map activity,so whatever is possible will be autofilled
        Intent i = getIntent();
        if (i.hasExtra("Object")) {
            event = i.getExtras().getParcelable("Object");
            adressEt.setText(event.getAddress());
            hostNameEt.setText(event.getHostName());
            phoneEt.setText(event.getPhoneNumber());


        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.find_lives:
                Intent i = new Intent(this.getApplicationContext(), DiscoverEventsActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);

        }


        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


}
