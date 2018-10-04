package com.example.lively;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class DetailActivity extends AppCompatActivity {
    Event event;
    TextView artist;
    TextView host;
    TextView datetime;
    TextView entrance;
    TextView genre;
    TextView contact;
    TextView address;
    TextView noteOpen;
    TextView personamMessage;

    //THIS IS THE ACTIVITY WHERE THE USER CAN SEE THE DETAILS OF THE PLACE PICKED FROM THE PLACES AVAILABLE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Intent i = getIntent();
        event = i.getExtras().getParcelable("Object");
        //SETTING THE TOOLBARS FOR THE COLLAPSING EFFECT
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar_layout);
        collapsingToolbarLayout.setTitle(event.getArtistName() + " at " + event.getHostName());
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        collapsingToolbarLayout.setExpandedTitleColor(Color.WHITE);
        //SETTING THE TEXTVIES,ETC
        address = findViewById(R.id.address_detail);
        address.setText(event.getAddress());
        artist = findViewById(R.id.name_details);
        artist.setText(event.getArtistName());
        host = findViewById(R.id.place_details);
        host.setText(event.getHostName());
        datetime = findViewById(R.id.date_details);
        datetime.setText(event.getDateTime());
        entrance = findViewById(R.id.price_details);
        if (event.getPrice().equals("0"))
            entrance.setText("Free");
        else
            entrance.setText(event.getPrice() + " Euro");
        genre = findViewById(R.id.genre_details);
        genre.setText(event.getGenre());
        contact = findViewById(R.id.phone_detail);
        contact.setText(event.getPhoneNumber());
        personamMessage = findViewById(R.id.personalmessagedetail);
        noteOpen = findViewById(R.id.note_opening);
        noteOpen.setText("A message from " + event.getArtistName());
        personamMessage.setText(event.getArtistComment());
        Button button = findViewById(R.id.make_widget);
        //PRESSING THE BUTTON WILL FILL THE WIDGET WITH THE EVENTS INFO
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent wIntent = new Intent(DetailActivity.this, EventService.class);
                wIntent.putExtra("Object", event);
                wIntent.setAction(EventService.ACTION_UPDATE_WIDGET);
                startService(wIntent);
            }
        });


    }
}
