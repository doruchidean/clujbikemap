package com.doruchidean.clujbikemap;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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
            IS_COUNTING_DOWN = "iscounting";

    //values that need to be saved and loaded
    private ArrayList<String> favouriteStations=new ArrayList<>();

    private boolean
            showFavouritesOnly,
            mIsCountingDown;

    private int
            mColdLimit,
            mHotLimit,
            mTimerMinutes;

    private PersistenceManager(){
    }

    /**
     * This method must be called once at the start of the app
     * @param context
     */
    public void loadData(Context context){
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
        mTimerMinutes = sp.getInt(TIMER_MINUTES, 69); //todo update default value to 45 after testing

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

        editor.apply();
    }

    public static PersistenceManager getInstance(){
        if(mInstance == null){
            mInstance = new PersistenceManager();
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
}
