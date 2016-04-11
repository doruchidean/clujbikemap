package com.doruchidean.clujbikemap.helpers;

import com.doruchidean.clujbikemap.models.BikeStations;

import java.util.ArrayList;

/**
 * Created by Doru on 19/11/15.
 * This interface holds the api response callbacks
 */
public interface Callbacks {

    interface ApiCallbacks{
        void onSuccessBikeStations(ArrayList<BikeStations> stationsArray);
        void onSuccessBusTimes(byte[] rawData);
        void onApiCallFail(int responseCode);
    }

    interface SettingsDialogsCallback{
        void setUpMap();
    }

}
