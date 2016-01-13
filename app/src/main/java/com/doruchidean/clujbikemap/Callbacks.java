package com.doruchidean.clujbikemap;

import java.util.ArrayList;

/**
 * Created by Doru on 19/11/15.
 * This interface holds the api response callbacks
 */
public interface Callbacks {

    interface ApiCallbacks{
        void onApiCallSuccess(ArrayList<StationsModel> stationsArray);
        void onApiCallFail(String error);
    }

    interface SettingsDialogsCallback{
        void setUpMap();
    }

}
