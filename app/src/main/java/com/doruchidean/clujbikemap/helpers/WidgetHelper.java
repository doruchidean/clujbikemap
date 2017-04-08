package com.doruchidean.clujbikemap.helpers;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.RemoteViews;

import com.doruchidean.clujbikemap.R;
import com.doruchidean.clujbikemap.activities.MapsActivity;
import com.doruchidean.clujbikemap.database.DatabaseHandler;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Doru on 08/04/2017.
 *
 */

public class WidgetHelper {

    private Context mContext;
    private String busName;

    public WidgetHelper(Context context) {
        mContext = context;
    }

    public void doUpdate() {
        busName = PersistenceManager.getBusName(mContext);
        new GetBusScheduleTask().execute(busName);
    }

    private void updateWidgetView(HashMap<String, ArrayList<String>> data) {

        int widgetId = PersistenceManager.getWidgetId(mContext);

        if (widgetId > 0) {

            RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget);

            remoteViews.setTextViewText(
                    R.id.tv_widget_bus,
                    busName
            );

            String capat1Title = data.get(Factory.NUME_CAPETE).get(0);
            String capat2Title = data.get(Factory.NUME_CAPETE).get(1);

            ArrayList<String> plecariCapat1 = data.get(Factory.PLECARI_CAPAT_1);
            ArrayList<String> plecariCapat2 = data.get(Factory.PLECARI_CAPAT_2);

            remoteViews.setTextViewText(R.id.tv_widget_times_titlu_1, capat1Title);
            remoteViews.setTextViewText(R.id.tv_widget_times_titlu_2, capat2Title);

            if(plecariCapat1.size() == 0){
                remoteViews.setTextViewText(R.id.tv_widget_times_capat_1, mContext.getString(R.string.bus_schedule_not_found));
            }else{
                String textToBeDisplayed = "";
                for(String s : GeneralHelper.getDeparturesInNextHour(false, plecariCapat1)){
                    textToBeDisplayed += s + " | ";
                }
                remoteViews.setTextViewText(
                        R.id.tv_widget_times_capat_1,
                        textToBeDisplayed
                );
            }
            if (plecariCapat2.size() == 0) {
                remoteViews.setTextViewText(R.id.tv_widget_times_capat_2, mContext.getString(R.string.bus_schedule_not_found));
            } else {
                String textToBeDisplayed = "";
                for(String s : GeneralHelper.getDeparturesInNextHour(false, plecariCapat2)){
                    textToBeDisplayed += s + " | ";
                }
                remoteViews.setTextViewText(
                        R.id.tv_widget_times_capat_2,
                        textToBeDisplayed
                );
            }

            Intent intent = new Intent(mContext, MapsActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            remoteViews.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

            AppWidgetManager.getInstance(mContext).updateAppWidget(
                    widgetId,
                    remoteViews
            );
        }
    }

    class GetBusScheduleTask extends AsyncTask<String, Void, HashMap<String, ArrayList<String>>> {

        @Override
        protected HashMap<String, ArrayList<String>> doInBackground(String... strings) {
            HashMap<String, ArrayList<String>> result = new HashMap<>();

            if (strings.length == 0) {
                return result;
            }

            String busName = strings[0];

            DatabaseHandler db = DatabaseHandler.getInstance(mContext);

            return db.getBusScheduleForTodayByName(busName);
        }

        @Override
        protected void onPostExecute(HashMap<String, ArrayList<String>> data) {
            updateWidgetView(data);
        }
    }
}
