package com.doruchidean.clujbikemap.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by Doru on 07/01/16.
 *
 */
public class PersistenceManager {

    //constant keys
    public static final String
            FAVOURITE_STATIONS = "favouritesStations",
            SHOWS_FAVOURITES_ONLY = "showFavourites",
            COLD_LIMIT = "coldlimit",
            HOT_LIMIT = "hotlimit",
            TIMER_VALUE_INDEX ="timermin",
            IS_COUNTING_DOWN = "iscounting",
            WIDGET_ID="widgetid",
            WIDGET_UPDATE_INTERVAL="widgetupdateinterval",
            OVERALL_BIKES="overall.bikes",
            OVERALL_EMPTY_SPOTS="overall.empty",
            OVERALL_MAX_NR="overall.max.nr",
            BUS_TABLE_UPDATED_DAY = "bus.table.updated.day",
            BUS_NAME ="buses";

    //// TODO: 19/05/16 TEST ALL FAVOURITE STATIONS FUNCTIONALITIES
    public static void setFavouriteStation(Context context, String stationName){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        String favouriteStationCSV = sp.getString(FAVOURITE_STATIONS, "");
        favouriteStationCSV += ","+stationName;

        sp.edit()
                .putString(FAVOURITE_STATIONS, favouriteStationCSV)
                .apply();
    }

    public static void removeFavouriteStation(Context context, String stationName){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        String favouriteStationsCSV = sp.getString(FAVOURITE_STATIONS, ","+stationName);

        sp.edit()
                .putString(FAVOURITE_STATIONS, favouriteStationsCSV.replace(","+stationName, ""))
                .apply();
    }

    public static ArrayList<String> getFavouriteStations(Context context) {
        ArrayList<String> result = new ArrayList<>();

        String favouriteStations = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(FAVOURITE_STATIONS, "");

        if (favouriteStations.length() > 0) {
            Collections.addAll(
                    result,
                    favouriteStations.split(","));
        }

        return result;
    }
    public static boolean isFavourite(Context context, String stationName){
        ArrayList<String> favouriteStations = new ArrayList<>();
        Collections.addAll(
                favouriteStations,
                PreferenceManager.getDefaultSharedPreferences(context).getString(FAVOURITE_STATIONS, "").split(",")
        );

        for(String s : favouriteStations){
            if (s.equalsIgnoreCase(stationName)) return true;
        }

        return false;
    }

    public static void setShowFavouritesOnly(Context context, boolean showFavouritesOnly) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(SHOWS_FAVOURITES_ONLY, showFavouritesOnly)
                .apply();
    }

    public static boolean getShowFavouritesOnly(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SHOWS_FAVOURITES_ONLY, false);
    }

    public static int getTimerValueIndex(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(TIMER_VALUE_INDEX, 2);
    }

    public static void setTimerValueIndex(Context context, int valueIndex) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(TIMER_VALUE_INDEX, valueIndex)
                .apply();
    }

    public static int getHotLimit(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(HOT_LIMIT, 3);
    }

    public static void setHotLimit(Context context, int hotLimit) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(HOT_LIMIT, hotLimit)
                .apply();
    }

    public static int getColdLimit(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(COLD_LIMIT, 3);
    }

    public static void setColdLimit(Context context, int coldLimit) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(COLD_LIMIT, coldLimit)
                .apply();
    }

    public static void setIsCountingDown(Context context, boolean isCountingDown){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(IS_COUNTING_DOWN, isCountingDown)
                .apply();
    }

    public static boolean getIsCountingDown(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(IS_COUNTING_DOWN, false);
    }
    public static void setBusName(Context context, String busName) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(BUS_NAME, busName)
                .apply();
    }

    public static String getBusName(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(BUS_NAME, "");
    }

    public static int getWidgetId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(WIDGET_ID, 0);
    }

    public static void setWidgetId(Context context, int widgetId) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(WIDGET_ID, widgetId)
                .apply();
    }

    public static void setOverallStats(Context context, int allBikes, int allEmptySpots, int maxNrOfBikes){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(OVERALL_BIKES, allBikes)
                .putInt(OVERALL_EMPTY_SPOTS, allEmptySpots)
                .putInt(OVERALL_MAX_NR, maxNrOfBikes)
                .apply();
    }

    public static HashMap<String, Integer> getOverallStats(Context context){
        HashMap<String, Integer> result = new HashMap<>();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        result.put(OVERALL_BIKES, sp.getInt(OVERALL_BIKES, 0));
        result.put(OVERALL_EMPTY_SPOTS, sp.getInt(OVERALL_EMPTY_SPOTS, 0));
        result.put(OVERALL_MAX_NR, sp.getInt(OVERALL_MAX_NR, 0));

        return result;
    }

    public static void setValueIndexForWidgetUpdateInterval(Context context, int interval){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(WIDGET_UPDATE_INTERVAL, interval)
                .apply();
    }

    public static int getValueIndexForWidgetUpdateInterval(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(WIDGET_UPDATE_INTERVAL, 3);
    }

    public static void setBusTableUpdatedDay(Context context, int busTableCreatedDay) {
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putInt(BUS_TABLE_UPDATED_DAY, busTableCreatedDay)
                .apply();
    }

    public static int getBusTableUpdatedDay(Context context){
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getInt(BUS_TABLE_UPDATED_DAY, 0);
    }
}
