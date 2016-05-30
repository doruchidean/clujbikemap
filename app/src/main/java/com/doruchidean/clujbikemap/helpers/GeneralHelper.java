package com.doruchidean.clujbikemap.helpers;

import android.content.Context;
import android.util.DisplayMetrics;

import com.doruchidean.clujbikemap.R;
import com.doruchidean.clujbikemap.database.DatabaseHandler;
import com.doruchidean.clujbikemap.models.BikeStation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import okhttp3.Response;

/**
 * Created by Doru on 18/04/16.
 *
 */
public class GeneralHelper {

  public static final int
    busLeavingMinOffset = 0,
    busLeavingMaxLimit = 45;

  public static final int[]
		TIMER_VALUES = {30,35,40,45,50,55},
		WIDGET_UPDATE_HOUR_INTERVALS = {15, 30, 1*60, 2*60, 3*60, 4*60, 5*60, 6*60, 7*60, 8*60, 12*60, 24*60};

	public static final String
		ORAR_SAMBATA="_s",
		ORAR_DUMINICA="_d",
		ORAR_LUNI_VINERI="_lv";

  public static BikeStation findStationInArray(String markerTitleToFind, ArrayList<BikeStation> list){

    BikeStation result = null;

    int lowEnd = 0;
    int highEnd = list.size()-1;
    int middle, compare;

    while (lowEnd <= highEnd){
      middle = (lowEnd+highEnd)/2;

      compare = markerTitleToFind.compareToIgnoreCase(list.get(middle).stationName);

      if(compare < 0){
        highEnd = middle-1;
      }else if(compare > 0){
        lowEnd = middle +1;
      }else{
        result = list.get(middle);
        break;
      }

    }

    return result;
  }

	/**
	 * This method adds a new row into the bus table
	 * @param context needed to handle the database
	 * @param rawData holding a binary .csv file, or null if there is no data available (ex: orar 7 samabta, duminica)
	 */
	public static void updateDatabase(Context context, byte[] rawData){

		DatabaseHandler databaseHandler = DatabaseHandler.getInstance(context);

		if(rawData != null) {
			HashMap<String, ArrayList<String>> scheduleMap = Factory.getInstance().readCsv(rawData);

			databaseHandler.insertBusScheduleForToday(
				PersistenceManager.getBusNumber(context),
				scheduleMap.get(Factory.NUME_CAPETE).get(0),
				scheduleMap.get(Factory.NUME_CAPETE).get(1),
				scheduleMap.get(Factory.PLECARI_CAPAT_1).toString().replace("[", "").replace("]", ""),
				scheduleMap.get(Factory.PLECARI_CAPAT_2).toString().replace("[", "").replace("]", "")
			);
		}else{
			databaseHandler.insertBusScheduleNotExistent(PersistenceManager.getBusNumber(context));
		}

		PersistenceManager.setBusTableUpdatedDay(context, Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
	}

  public static int getPixelsForDP(Context c, int dpNeeded){
    DisplayMetrics displayMetrics = c.getResources().getDisplayMetrics();
    return Math.round(dpNeeded * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
  }

  public static String resolveBusInUrl(String busNumber, String url){
    Calendar calendar = Calendar.getInstance();

    switch (calendar.get(Calendar.DAY_OF_WEEK)){
      case(Calendar.SATURDAY): busNumber = busNumber+ORAR_SAMBATA;
        break;
      case(Calendar.SUNDAY): busNumber = busNumber+ORAR_DUMINICA;
        break;
      default: busNumber = busNumber+ORAR_LUNI_VINERI;
    }

    return url.replace("BUS_PERIOD", busNumber);
  }

	/**
	 * This method returns a string containing departures in next hour
	 * @param inMinutes result needed to be in minutes remaining or actual time
	 * @param plecariTotale orar
	 * @return minutes remaining or departure times if any, otherwise the result is an empty string
	 */
  public static ArrayList<String> getDeparturesInNextHour(boolean inMinutes, ArrayList<String> plecariTotale){

    ArrayList<String> result= new ArrayList<>();
    String plecare;

		Calendar calendar = Calendar.getInstance();
		int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
		int currentMinute = calendar.get(Calendar.MINUTE);

    for(String s : plecariTotale){
      plecare = isBusTimeInNextHour(inMinutes, s, currentHour, currentMinute);
      if(plecare != null){
        result.add(plecare);
      }
    }

    return result;
  }

  public static String[] getDistanceFromResponse(Response response){
    String[] result = new String[2];

    try {
      JSONObject j = new JSONObject(response.body().string());
      JSONArray rows = j.getJSONArray("rows");
      JSONObject route1 = rows.getJSONObject(0);
      JSONArray elements = route1.getJSONArray("elements");
      JSONObject element1 = elements.getJSONObject(0);
      JSONObject distance = element1.getJSONObject("distance");
      result[0] = String.valueOf((int)(distance.getLong("value")*1.31));
      JSONObject duration = element1.getJSONObject("duration");
      result[1] = duration.getString("text");
    } catch (JSONException | IOException e) {
      e.printStackTrace();
    }

    return result;
  }

  public static String isBusTimeInNextHour(boolean inMinutes, String plecare, int currentHour, int currentMinute){

    String[] busHourAndMin = plecare.split(":");
    int busHour = Integer.valueOf(busHourAndMin[0]);
    int currentTimeSeconds = currentHour*60*60 + currentMinute*60;

    if(currentHour == busHour || currentHour+1==busHour) {

      int busMin = Integer.valueOf(busHourAndMin[1]);
      int busTimeSeconds = busHour * 60 * 60 + busMin * 60;
      int timeDifference = (busTimeSeconds - currentTimeSeconds) / 60;

      if (timeDifference>= busLeavingMinOffset && timeDifference <= busLeavingMaxLimit) {

        if (inMinutes) {
          return String.valueOf(timeDifference);
        } else {
          return plecare;
        }
      }
    }
    return null;
  }

  public static String[] getTimerPickerDisplayedValues(Context context){
    int length = TIMER_VALUES.length;
    String[] result = new String[length];
		String unit = context.getString(R.string.picker_minutes);
    for (int i = 0; i < length; i++) {
      result[i] = String.valueOf(TIMER_VALUES[i]) + unit;
    }
    return result;
  }

  public static String[] getWidgetPickerDisplayedValues(Context context){
    int length = WIDGET_UPDATE_HOUR_INTERVALS.length;
    String[] result = new String[length];
		int value;
    for (int i = 0; i < length; i++) {
			value = WIDGET_UPDATE_HOUR_INTERVALS[i];
			if (value == 60) {
				result[i] = value/60 + context.getString(R.string.picker_hour);
			} else if (value > 60){
				result[i] = value/60 + context.getString(R.string.picker_hours);
			} else {
				result[i] = value + context.getString(R.string.picker_minutes);
			}
		}
    return result;
  }

  public static int getMinutesForTimerDisplayedValue(int timerPickerValue){
    return TIMER_VALUES[timerPickerValue];
  }

  public static int getMillisForWidgetDisplayedValue(int widgetPickerValue){
    return WIDGET_UPDATE_HOUR_INTERVALS[widgetPickerValue]*60*1000;
  }
}
