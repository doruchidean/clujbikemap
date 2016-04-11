package com.doruchidean.clujbikemap.models;

import android.support.annotation.NonNull;

/**
 * Created by Doru on 28/08/15.
 *
 */
public class BikeStations implements Comparable<BikeStations>{

    public String address, lastSyncDate, stationName, stationStatus, statusType;
    public boolean customIsValid, isValid, isFavourite;
    public int emptySpots, id, idStatus, maximumNumberOfBikes, ocuppiedSpots;
    public double latitude, longitude;

    @Override
    public int compareTo(@NonNull BikeStations another) {
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
                ocuppiedSpots);
    }
}
