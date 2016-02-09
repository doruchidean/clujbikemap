package com.doruchidean.clujbikemap;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Doru on 19/11/15.
 * This interface holds the api response callbacks
 */
public interface Callbacks {

    interface ApiCallbacks{
        void onApiCallSuccessStations(ArrayList<StationsModel> stationsArray);
        void onApiCallSuccessBusLeaving(HashMap<String, ArrayList<String>> leavingTimes);
        void onApiCallFail(String error);
    }

    interface SettingsDialogsCallback{
        void setUpMap();
    }

}
