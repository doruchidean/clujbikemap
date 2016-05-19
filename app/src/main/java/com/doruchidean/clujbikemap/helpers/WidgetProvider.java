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
import com.doruchidean.clujbikemap.database.DatabaseHandler;
import com.doruchidean.clujbikemap.fragments.WidgetUpdateIntervalFragment;

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

    //manually handle the widget id
    PersistenceManager.setWidgetId(context, appWidgetIds[0]);

    Log.d("traces", "onUpdate");
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

    Log.d("traces", "widget onEnabled");
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

    int widgetId = PersistenceManager.getWidgetId(context);

    Log.d("traces", "widgetProvider onReceive " + widgetId);

    if(widgetId > 0) {
      mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

      updateTexts(context, PersistenceManager.getBusNumber(context));

      Intent onClickIntent = new Intent(context, MapsActivity.class);
      PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, onClickIntent, PendingIntent.FLAG_ONE_SHOT);
      mRemoteViews.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

      AppWidgetManager.getInstance(context).updateAppWidget(
        widgetId,
        mRemoteViews
      );

      Log.d("traces", this.getClass().getSimpleName() + " onReceive(): id " + widgetId);
    }
  }

  public void updateTexts(Context context, String busNumber) {
		HashMap<String, ArrayList<String>> leavingTimes = DatabaseHandler.getInstance(context).getBusScheduleForToday(busNumber);

    if(busNumber.length() == 0 || leavingTimes.get(Factory.NUME_CAPETE).size() == 0) return;

		ArrayList<String> plecariCapat1 = GeneralHelper.getDeparturesInNextHour(false, leavingTimes.get(Factory.PLECARI_CAPAT_1));
		ArrayList<String> plecariCapat2 = GeneralHelper.getDeparturesInNextHour(false, leavingTimes.get(Factory.PLECARI_CAPAT_2));

		String numeCapat1 = leavingTimes.get(Factory.NUME_CAPETE).get(0);
		String numeCapat2 = leavingTimes.get(Factory.NUME_CAPETE).get(1);

		mRemoteViews.setTextViewText(R.id.tv_widget_bus, busNumber);
		if (plecariCapat1.size() == 0) {
			mRemoteViews.setTextViewText(
				R.id.tv_widget_times_capat_1,
				String.format(context.getString(R.string.bus_departing_over_limit), GeneralHelper.busLeavingMaxLimit));
		}else{
			String textToBeDisplayed = "";
			for(String s : plecariCapat1){
				textToBeDisplayed += s + " | ";
			}
			mRemoteViews.setTextViewText(R.id.tv_widget_times_capat_1, textToBeDisplayed);
		}
		if(plecariCapat2.size() == 0){
			mRemoteViews.setTextViewText(
				R.id.tv_widget_times_capat_2,
				String.format(context.getString(R.string.bus_departing_over_limit), GeneralHelper.busLeavingMaxLimit));
		}else{
			String textToBeDisplayed = "";
			for(String s : plecariCapat2){
				textToBeDisplayed += s + " | ";
			}
			mRemoteViews.setTextViewText(R.id.tv_widget_times_capat_2, textToBeDisplayed);
		}
		mRemoteViews.setTextViewText(R.id.tv_widget_times_titlu_1, numeCapat1);
    mRemoteViews.setTextViewText(R.id.tv_widget_times_titlu_2, numeCapat2);

  }

}
