package com.doruchidean.clujbikemap;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Doru on 19/11/15.
 * This interface holds the api response callbacks
 */
public interface Callbacks {

    interface ApiCallbacks{
        void onSuccessBikeStations(ArrayList<StationsModel> stationsArray);
        void onSuccessBusTimes(byte[] rawData);
        void onApiCallFail(String error);
    }

    interface SettingsDialogsCallback{
        void setUpMap();
    }

}
