package com.doruchidean.clujbikemap.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Doru on 07/01/16.
 * This class manages values stored locally for persistence purposes.
 * Needs to be initialized once per app, with a context passed, before usage
 */
public class PersistenceManager {

    private static PersistenceManager mInstance;

    //constant keys
    private static final String
            FAVOURITE_STATIONS = "favouritesStations",
            SHOWS_FAVOURITES_ONLY = "showFavourites",
            COLD_LIMIT = "coldlimit",
            HOT_LIMIT = "hotlimit",
            TIMER_MINUTES="timermin",
            IS_COUNTING_DOWN = "iscounting",
            BUSES="buses",
            WIDGET_ID="widgetid",
            BUS_SCHEDULE = "busschedule",
            WIDGET_UPDATE_INTERVAL="widgetupdateinterval",
            SHOW_BUS_BAR="showbusbar";

    //values that need to be saved and loaded
    private ArrayList<String> favouriteStations=new ArrayList<>();

    private boolean
            showFavouritesOnly,
            mIsCountingDown,
            mShowBusBar;

    private int
            mColdLimit,
            mHotLimit,
            mTimerMinutes,
            mWidgetUpdateInterval,
            mWidgetId;

    private String mBusName;

    private PersistenceManager(Context context){

        //load data
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        String raw = sp.getString(FAVOURITE_STATIONS, "");
        String[] rawList;
        if (raw.length() > 0) {
            rawList = raw.split(",");
            Collections.addAll(favouriteStations, rawList);
        }
        showFavouritesOnly = sp.getBoolean(SHOWS_FAVOURITES_ONLY, false);
        mIsCountingDown = sp.getBoolean(IS_COUNTING_DOWN, false);
        mColdLimit = sp.getInt(COLD_LIMIT, 3);
        mHotLimit = sp.getInt(HOT_LIMIT, 3);
        mTimerMinutes = sp.getInt(TIMER_MINUTES, 45);
        mBusName = sp.getString(BUSES, "");
        mWidgetUpdateInterval = sp.getInt(WIDGET_UPDATE_INTERVAL, 3);
        mShowBusBar = sp.getBoolean(SHOW_BUS_BAR, true);
        mWidgetId = sp.getInt(WIDGET_ID, 0);
    }

    public void saveData(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        String fakeJson = null;
        for(int i = 0; i<favouriteStations.size(); i++){
            fakeJson = fakeJson + "," + favouriteStations.get(i);
        }

        editor.putString(FAVOURITE_STATIONS, fakeJson);
        editor.putBoolean(SHOWS_FAVOURITES_ONLY, showFavouritesOnly);
        editor.putInt(COLD_LIMIT, mColdLimit);
        editor.putInt(HOT_LIMIT, mHotLimit);
        editor.putInt(TIMER_MINUTES, mTimerMinutes);
        editor.putBoolean(IS_COUNTING_DOWN, mIsCountingDown);
        editor.putString(BUSES, mBusName);
        editor.putInt(WIDGET_UPDATE_INTERVAL, mWidgetUpdateInterval);
        editor.putBoolean(SHOW_BUS_BAR, mShowBusBar);
        editor.putInt(WIDGET_ID, mWidgetId);

        editor.apply();
    }

    public static PersistenceManager getInstance(Context context){
        if(mInstance == null){
            mInstance = new PersistenceManager(context);
        }
        return mInstance;
    }

    public void addFavouriteStation(String stationName){
        favouriteStations.add(stationName);
    }

    public void removeFavouriteStation(String stationName){
        favouriteStations.remove(stationName);
    }
    public ArrayList<String> getFavouriteStations() {
        return favouriteStations;
    }

    public boolean isFavourite(String stationName){

        for(String s:favouriteStations){
            if (s.equalsIgnoreCase(stationName)) return true;
        }

        return false;
    }

    public void setShowFavouritesOnly(boolean showFavouritesOnly) {
        this.showFavouritesOnly = showFavouritesOnly;
    }

    public int getTimerMinutes() {
        return mTimerMinutes;
    }

    public void setTimerMinutes(int mTimerMinutes) {
        this.mTimerMinutes = mTimerMinutes;
    }

    public boolean getShowFavouritesOnly() {
        return showFavouritesOnly;
    }

    public int getHotLimit() {
        return mHotLimit;
    }

    public void setHotLimit(int mHotLimit) {
        this.mHotLimit = mHotLimit;
    }

    public int getColdLimit() {
        return mColdLimit;
    }

    public void setColdLimit(int mColdLimit) {
        this.mColdLimit = mColdLimit;
    }

    public void setIsCountingDown(boolean isCountingDown){
        mIsCountingDown = isCountingDown;
    }
    public boolean getIsCountingDown(){
        return mIsCountingDown;
    }

    public String getBusName() {
        return mBusName;
    }

    public void setBusName(String busName) {
        this.mBusName = busName;
    }

    public int getWidgetId() {

        return mWidgetId;
    }

    public void setWidgetId(int widgetId) {
        mWidgetId = widgetId;
    }

    public void setBusSchedule(Context context, byte[] binaryData) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(BUS_SCHEDULE, Base64.encodeToString(binaryData, Base64.NO_WRAP));
        editor.apply();

    }
    public byte[] getBusSchedule(Context context){

        byte[] result=null;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String s = sp.getString(BUS_SCHEDULE, null);
        if(s != null){
            result = Base64.decode(s, Base64.NO_WRAP);
        }
        return result;
    }

    public void setWidgetUpdateInterval(int interval){
        mWidgetUpdateInterval = interval;
    }

    public int getWidgetUpdateInterval() {
        return mWidgetUpdateInterval;
    }

    public boolean getShowBusBar() {
        return mShowBusBar;
    }

    public void setShowBusBar(boolean showBusBar) {
        this.mShowBusBar = showBusBar;
    }
}
