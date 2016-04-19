package com.doruchidean.clujbikemap.models;

import android.support.annotation.NonNull;

/**
 * Created by Doru on 28/08/15.
 *
 */
public class BikeStation implements Comparable<BikeStation>{

    public String address, lastSyncDate, stationName, stationStatus, statusType;
    public boolean customIsValid, isValid, isFavourite;
    public int emptySpots, id, idStatus, maximumNumberOfBikes, occupiedSpots;
    public double latitude, longitude;

    @Override
    public int compareTo(@NonNull BikeStation another) {
        return stationName.compareToIgnoreCase(another.stationName);
    }

    @Override
    public String toString() {
        return String.format(
                "name: %s, " +
                "address: %s, " +
                "lastSyncDate: %s, " +
                "stationStatus: %s, " +
                "statusType: %s, " +
                "isValid: %s, " +
                "emptySpots: %s, " +
                "id: %s, " +
                "idStatus: %s, " +
                "maxNrOfBikes: %s, " +
                "occupiedSpots: %s, ",
                stationName,
                address,
                lastSyncDate,
                stationStatus,
                statusType,
                isValid,
                emptySpots,
                id,
                idStatus,
                maximumNumberOfBikes,
                occupiedSpots);
    }
}
