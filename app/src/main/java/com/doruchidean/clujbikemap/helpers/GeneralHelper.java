package com.doruchidean.clujbikemap.helpers;

import android.content.Context;
import android.util.DisplayMetrics;

import com.doruchidean.clujbikemap.models.BikeStation;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Doru on 18/04/16.
 *
 */
public class GeneralHelper {

  public static final int minMinutes = 0, maxMinutes=45;

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
      result = String.format(" > %s min", maxMinutes);
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

      if (timeDifference>=minMinutes && timeDifference <= maxMinutes) {

        if (requiresMinutes) {
          return String.valueOf(timeDifference);
        } else {
          return plecare;
        }
      }
    }
    return null;
  }

  public static int getMillisForDisplayedValue(int pickerVal){
    int result;

    if(pickerVal == 2){
      result = 45;
    }else if(pickerVal == 3){
      result = 60;
    }else if(pickerVal == 4){
      result = 4*60;
    }else if(pickerVal == 5){
      result = 8*60;
    }else if(pickerVal == 6){
      result = 12*60;
    }else if(pickerVal == 7){
      result = 24*60;
    }else{
      result = 30;
    }

    return result*60*1000;
  }
}
