package com.example.lively;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

//THE WIDGET Service with a normal implementation
public class EventService extends IntentService {

    public static final String ACTION_UPDATE_WIDGET = "update_widget";

    public EventService(String name) {
        super(name);
    }

    public EventService() {
        super("Baking Service");

    }

    public static void startActionUpdateWidgets(Context context) {
        Intent intent = new Intent(context, EventService.class);
        intent.setAction(ACTION_UPDATE_WIDGET);
        context.startService(intent);

    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {

            final String action = intent.getAction();
            if (ACTION_UPDATE_WIDGET.equals(action)) {

                if (intent.hasExtra("Object")) {


                    Event event = intent.getExtras().getParcelable("Object");

                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, LastPlaceClickedProvider.class));
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.layout.last_place_clicked_widget);
                    LastPlaceClickedProvider.updateAppWidget(this, appWidgetManager, appWidgetIds, event);
                }
            }
        }
    }


}