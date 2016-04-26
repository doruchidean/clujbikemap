package com.doruchidean.clujbikemap.helpers;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.doruchidean.clujbikemap.R;
import com.doruchidean.clujbikemap.activities.MapsActivity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Doru on 15/02/16.
 *
 */
public class WidgetProvider extends AppWidgetProvider {

    private RemoteViews mRemoteViews;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        PersistenceManager pm = PersistenceManager.getInstance(context);

        //manually handle the widget id
        pm.setWidgetId(appWidgetIds[0]);
        pm.saveData(context);

        Log.d("traces", "onUpdate");
    }

    @Override
    public void onEnabled(Context context) {

        //start alarm service to update the widget regularly
        SettingsDialogs.getInstance().setAlarmForWidgetUpdate(
                context,
                GeneralHelper.getMillisForWidgetDisplayedValue(
                        PersistenceManager.getInstance(context).getWidgetPickerValue()
                )
        );

        Log.d("traces", "widget onEnabled");
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        //prevents the app to try and update the widget when is gone
        PersistenceManager.getInstance(context).setWidgetId(0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        PersistenceManager persistenceManager = PersistenceManager.getInstance(context);
        int widgetId = persistenceManager.getWidgetId();

        Log.d("traces", "widgetProvider onReceive " + widgetId);

        if(widgetId > 0) {
            mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

            updateTexts(persistenceManager.getBusSchedule(context), persistenceManager.getBusName());

            Intent onClickIntent = new Intent(context, MapsActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, onClickIntent, PendingIntent.FLAG_ONE_SHOT);
            mRemoteViews.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

            AppWidgetManager.getInstance(context).updateAppWidget(
                    widgetId,
                    mRemoteViews
            );

            Log.d("traces", "widget visible: id " + widgetId);
        }
    }

    public void updateTexts(byte[] rawData, String busName) {

        if(rawData == null || busName.length() == 0) return;

        HashMap<String, ArrayList<String>> leavingTimes = Factory.getInstance().readCsv(rawData);

        mRemoteViews.setTextViewText(R.id.tv_widget_bus, GeneralHelper.getBusNumber(busName));
        String text= GeneralHelper.getPlecariAtThisHour(leavingTimes.get(Factory.PLECARI_CAPAT_2));
        mRemoteViews.setTextViewText(R.id.tv_widget_times_capat_2, text);
        text = GeneralHelper.getPlecariAtThisHour(leavingTimes.get(Factory.PLECARI_CAPAT_1));
        mRemoteViews.setTextViewText(R.id.tv_widget_times_capat_1, text);
        text = leavingTimes.get(Factory.NUME_CAPETE).get(0);
        mRemoteViews.setTextViewText(R.id.tv_widget_times_titlu_1, text);
        text = leavingTimes.get(Factory.NUME_CAPETE).get(1);
        mRemoteViews.setTextViewText(R.id.tv_widget_times_titlu_2, text);

    }

}
