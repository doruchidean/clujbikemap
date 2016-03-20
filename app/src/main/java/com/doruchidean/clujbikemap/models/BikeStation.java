package com.doruchidean.clujbikemap.models;

import android.support.annotation.NonNull;

/**
 * Created by Doru on 28/08/15.
 *
 */
public class BikeStation implements Comparable<BikeStation>{

    public String address, lastSyncDate, stationName, stationStatus, statusType;
    public boolean customIsValid, isValid, isFavourite;
    public int emptySpots, id, idStatus, maximumNumberOfBikes, ocuppiedSpots;
    public double latitude, longitude;

    @Override
    public int compareTo(@NonNull BikeStation another) {
        return stationName.compareToIgnoreCase(another.stationName);
    }
}
