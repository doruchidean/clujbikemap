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
public class ClujBikeMapWidgetProvider extends AppWidgetProvider implements Callbacks.ApiCallbacks {

    private RemoteViews mRemoteViews;
    private AppWidgetManager mAppWidgetManager;
    private int mWidgetId;
    private String mBus;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        this.mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        this.mAppWidgetManager = appWidgetManager;
        this.mWidgetId = appWidgetIds[0];
        this.mBus = PersistenceManager.getInstance().getSelectedBus();

        //manually handle the widget id
        PersistenceManager.getInstance().setWidgetId(context, mWidgetId);

        if (mBus!=null && mBus.length()>0) {
            ApiClient.getInstance().getBusSchedule(ClujBikeMapWidgetProvider.this, mBus);
        }

        Intent intent = new Intent(context, MapsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {

        super.onDisabled(context);
    }

    @Override
    public void onApiCallSuccessStations(ArrayList<StationsModel> stationsArray) {

    }

    @Override
    public void onApiCallSuccessBusLeaving(HashMap<String, ArrayList<String>> leavingTimes) {

        Factory factory = Factory.getInstance();

        mRemoteViews.setTextViewText(R.id.tv_widget_bus, factory.getBusNumber(mBus));
        String text= factory.getPlecariAtThisHour(leavingTimes.get(Factory.PLECARI_CAPAT_2));
        mRemoteViews.setTextViewText(R.id.tv_widget_times_capat_2, text);
        text = factory.getPlecariAtThisHour(leavingTimes.get(Factory.PLECARI_CAPAT_1));
        mRemoteViews.setTextViewText(R.id.tv_widget_times_capat_1, text);
        text = leavingTimes.get(Factory.NUME_CAPETE).get(0);
        mRemoteViews.setTextViewText(R.id.tv_widget_times_titlu_1, text);
        text = leavingTimes.get(Factory.NUME_CAPETE).get(1);
        mRemoteViews.setTextViewText(R.id.tv_widget_times_titlu_2, text);

        mAppWidgetManager.updateAppWidget(mWidgetId, mRemoteViews);
        Log.d("traces", "Widget updated: "+ text);
    }

    @Override
    public void onApiCallFail(String error) {
        //todo
    }
}
