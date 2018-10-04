package com.example.lively;

import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
//MEDIUM ARTICLE CODE LINK in the DiscoverEventsActivity

public class ScheduledJobService extends JobService {
    Event event;
    private static final String TAG = ScheduledJobService.class.getSimpleName();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    final DatabaseReference myRef = database.getReference().child("Events");

    @Override
    public boolean onStartJob(final JobParameters params) {
        //Offloading work to a new thread.
        new Thread(new Runnable() {
            @Override
            public void run() {
                codeYouWantToRun(params);
            }
        }).start();

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    public void codeYouWantToRun(final JobParameters parameters) {
        try {

            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    showData(dataSnapshot);

                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w("SchJobError", "Failed to read value.", error.toException());
                }
            });

            Thread.sleep(2000);

            Log.d(TAG, "completeJob: " + "jobFinished");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //Tell the framework that the job has completed and
            jobFinished(parameters, false);
        }
    }

    //Here I', setting the actual job,which is deleting any event,that it's date-time is earlier than the current date.
    public void showData(DataSnapshot dataSnapsh) {
        for (DataSnapshot ds : dataSnapsh.getChildren()) {
            event = ds.getValue(Event.class);
            try {
                if (new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(event.getDateTime()).before(new Date())) {
                    ds.getRef().removeValue();

                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}