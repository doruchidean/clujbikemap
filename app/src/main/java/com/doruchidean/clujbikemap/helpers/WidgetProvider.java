package com.doruchidean.clujbikemap.helpers;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import com.doruchidean.clujbikemap.fragments.WidgetUpdateIntervalFragment;

/**
 * Created by Doru on 15/02/16.
 *
 */
public class WidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //manually handle the widget id
        PersistenceManager.setWidgetId(context, appWidgetIds[0]);
    }

    @Override
    public void onEnabled(Context context) {

        //start alarm service to update the widget regularly
        WidgetUpdateIntervalFragment.setAlarmForWidgetUpdate(
                context,
                GeneralHelper.getMillisForWidgetDisplayedValue(
                        PersistenceManager.getValueIndexForWidgetUpdateInterval(context)
                )
        );
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        //prevents the app to try and update the widget when is gone
        PersistenceManager.setWidgetId(context, 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        new WidgetHelper(context).doUpdate();
    }

}
