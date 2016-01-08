package com.doruchidean.clujbikemap;

import android.support.annotation.NonNull;

/**
 * Created by Doru on 28/08/15.
 *
 */
public class StationsModel implements Comparable<StationsModel>{

    public String address, lastSyncDate, stationName, stationStatus, statusType;
    public boolean customIsValid, isValid, isFavourite;
    public int emptySpots, id, idStatus, maximumNumberOfBikes, ocuppiedSpots;
    public double latitude, longitude;

    @Override
    public int compareTo(@NonNull StationsModel another) {
        return stationName.compareToIgnoreCase(another.stationName);
    }
}
