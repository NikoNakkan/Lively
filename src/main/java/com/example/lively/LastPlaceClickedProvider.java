package com.example.lively;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

//The provider of my widget
public class LastPlaceClickedProvider extends AppWidgetProvider {
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId[], Event event) {


        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.last_place_clicked_widget);
        views.setTextViewText(R.id.widget_art_name_and_place, event.getArtistName() + " at " + event.getHostName());
        views.setTextViewText(R.id.widget_date_time, "Date and time : " + event.getDateTime());
        views.setTextViewText(R.id.widget_cost, "Ticket price : " + event.getPrice() + " Euro");
        views.setTextViewText(R.id.widget_address, "Address : " + event.getAddress());

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);


    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        EventService.startActionUpdateWidgets(context);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

