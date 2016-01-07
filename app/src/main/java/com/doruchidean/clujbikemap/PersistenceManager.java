package com.doruchidean.clujbikemap;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Doru on 07/01/16.
 * This class manages values stored locally for persistence purposes.
 * Needs to be initialized once per app, with a context passed, before usage
 */
public class PersistenceManager {

    String LOGTAG = "traces";

    private static PersistenceManager mInstance;

    private static final String FAVOURITE_STATIONS = "favouritesStations";
    private static final String SHOWS_FAVOURITES_ONLY = "showFavourites";

    private ArrayList<String> favouriteStations=new ArrayList<>();

    private boolean showFavouritesOnly = false;

    private PersistenceManager(){
    }

    /**
     * This method must be called once at the start of the app
     * @param context
     */
    public void loadData(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        String raw = sp.getString(FAVOURITE_STATIONS, "");
        String[] rawList = raw.split(",");

        Collections.addAll(favouriteStations, rawList);
        showFavouritesOnly = sp.getBoolean(SHOWS_FAVOURITES_ONLY, false);

    }

    public static PersistenceManager getInstance(){
        if(mInstance == null){
            mInstance = new PersistenceManager();
        }

        return mInstance;
    }

    public void addFavouriteStation(String stationName){
        favouriteStations.add(stationName);
        for(int i = 0; i<favouriteStations.size(); i++){
            Log.d(LOGTAG, favouriteStations.get(i));
        }
    }
    public void removeFavouriteStation(String stationName){
        favouriteStations.remove(stationName);
        for(int i = 0; i<favouriteStations.size(); i++){
            Log.d(LOGTAG, favouriteStations.get(i));
        }
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

        editor.apply();
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
    public boolean getShowFavouritesOnly() {
        return showFavouritesOnly;
    }
}
