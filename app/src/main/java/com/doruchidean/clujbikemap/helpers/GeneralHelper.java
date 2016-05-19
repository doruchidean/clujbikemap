package com.doruchidean.clujbikemap.helpers;

import android.content.Context;
import android.util.DisplayMetrics;

import com.doruchidean.clujbikemap.models.BikeStation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.Response;

/**
 * Created by Doru on 18/04/16.
 *
 */
public class GeneralHelper {

  public static final int
    busLeavingMinOffset = 0,
    busLeavingMaxOffset = 45;
  public static final int[] TIMER_VALUES = {30,35,40,45,50,55};
  public static final int[] WIDGET_UPDATE_HOUR_INTERVALS = {1,2,3,4,5,6,7,8,12,24};

  public static BikeStation binarySearchStation(String markerTitleToFind, ArrayList<BikeStation> list){

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

  public static int getPixelsForDP(Context c, int dpNeeded){
    DisplayMetrics displayMetrics = c.getResources().getDisplayMetrics();
    return Math.round(dpNeeded * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
  }

  public static String getBusNumber(String busName){
    return busName.split(":")[0];
  }

  public static String resolveBusInUrl(String busName, String url){
    Calendar calendar = Calendar.getInstance();

    String busExtension = getBusNumber(busName);

    switch (calendar.get(Calendar.DAY_OF_WEEK)){
      case(Calendar.SATURDAY): busExtension = busExtension+"_s";
        break;
      case(Calendar.SUNDAY): busExtension = busExtension+"_d";
        break;
      default: busExtension = busExtension+"_lv";
    }

    return url.replace("BUS_PERIOD", busExtension);
  }

  public static String getPlecariAtThisHour(ArrayList<String> plecariTotale){

    Calendar calendar = Calendar.getInstance();

    String result="|";
    String plecare;

    for(String s : plecariTotale){
      plecare = isBusTimeInNextHour(false, s, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
      if(plecare != null){
        result += plecare + "| ";
      }
    }

    if(result.length() == 1){
      result = String.format(" > %s min", busLeavingMaxOffset);
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

  public static String isBusTimeInNextHour(boolean requiresMinutes, String plecare, int currentHour, int currentMinute){

    String[] busHourAndMin = plecare.split(":");
    int busHour = Integer.valueOf(busHourAndMin[0]);
    int currentTimeSeconds = currentHour*60*60 + currentMinute*60;

    if(currentHour == busHour || currentHour+1==busHour) {

      int busMin = Integer.valueOf(busHourAndMin[1]);
      int busTimeSeconds = busHour * 60 * 60 + busMin * 60;
      int timeDifference = (busTimeSeconds - currentTimeSeconds) / 60;

      if (timeDifference>= busLeavingMinOffset && timeDifference <= busLeavingMaxOffset) {

        if (requiresMinutes) {
          return String.valueOf(timeDifference);
        } else {
          return plecare;
        }
      }
    }
    return null;
  }

  public static String[] getTimerPickerDisplayedValues(){
    int length = TIMER_VALUES.length;
    String[] result = new String[length];
    for (int i = 0; i < length; i++) {
      result[i] = String.valueOf(TIMER_VALUES[i]) + " min";
    }
    return result;
  }

  public static String[] getWidgetPickerDisplayedValues(){
    int length = WIDGET_UPDATE_HOUR_INTERVALS.length;
    String[] result = new String[length];
    for (int i = 0; i < length; i++) {
      result[i] = String.valueOf(WIDGET_UPDATE_HOUR_INTERVALS[i]) + " hours";
    }
    return result;
  }

  public static int getMinutesForTimerDisplayedValue(int timerPickerValue){
		if(timerPickerValue > 0 && timerPickerValue < TIMER_VALUES.length){
			return TIMER_VALUES[timerPickerValue];
		}else{
			return TIMER_VALUES[2]; //default value in PersistenceManager
		}
  }

  public static int getMillisForWidgetDisplayedValue(int widgetPickerValue){
    return WIDGET_UPDATE_HOUR_INTERVALS[widgetPickerValue]*60*60*1000;
  }
}
