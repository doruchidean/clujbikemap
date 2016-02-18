package com.doruchidean.clujbikemap;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Doru on 15/02/16.
 *
 */
public class ClujBikeMapWidgetProvider extends AppWidgetProvider {

    private RemoteViews mRemoteViews;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
//
        PersistenceManager pm = PersistenceManager.getInstance(context);
//        this.mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
//
        //manually handle the widget id
        pm.setWidgetId(context, appWidgetIds[0]);
//
        Log.d("traces", "onUpdate");
//        if (pm.getBusName()!=null && pm.getBusName().length()>0) {
//            updateTexts(pm.getBusSchedule(context));
//        }
//
//        Intent intent = new Intent(context, MapsActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
//        mRemoteViews.setOnClickPendingIntent(R.id.widget_container, pendingIntent);
//
//        appWidgetManager.updateAppWidget(appWidgetIds[0], mRemoteViews);
//
//        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        PersistenceManager persistenceManager = PersistenceManager.getInstance(context);
        int widgetId = persistenceManager.getWidgetId(context);

        Log.d("traces", "widgetProvider onReceive " + widgetId);

        if(widgetId>0) {
            mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

            updateTexts(persistenceManager.getBusSchedule(context), persistenceManager.getBusName());

            Intent onClickIntent = new Intent(context, MapsActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, onClickIntent, PendingIntent.FLAG_ONE_SHOT);
            mRemoteViews.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

            AppWidgetManager.getInstance(context).updateAppWidget(
                    widgetId,
                    mRemoteViews
            );

            Log.d("traces", "widget updated " + widgetId);
        }
    }

    public void updateTexts(byte[] rawData, String busName) {

        if(rawData == null) return;

        Factory factory = Factory.getInstance();

        HashMap<String, ArrayList<String>> leavingTimes = factory.readCsv(rawData);

        mRemoteViews.setTextViewText(R.id.tv_widget_bus, factory.getBusNumber(busName));
        String text= factory.getPlecariAtThisHour(leavingTimes.get(Factory.PLECARI_CAPAT_2));
        mRemoteViews.setTextViewText(R.id.tv_widget_times_capat_2, text);
        text = factory.getPlecariAtThisHour(leavingTimes.get(Factory.PLECARI_CAPAT_1));
        mRemoteViews.setTextViewText(R.id.tv_widget_times_capat_1, text);
        text = leavingTimes.get(Factory.NUME_CAPETE).get(0);
        mRemoteViews.setTextViewText(R.id.tv_widget_times_titlu_1, text);
        text = leavingTimes.get(Factory.NUME_CAPETE).get(1);
        mRemoteViews.setTextViewText(R.id.tv_widget_times_titlu_2, text);

    }

}
