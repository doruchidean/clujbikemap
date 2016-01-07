package com.doruchidean.clujbikemap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Doru on 19/11/15.
 * This class creates models from server json response;
 */
public class Factory {
    private static Factory ourInstance = new Factory();

    public static Factory getInstance() {
        return ourInstance;
    }

    private Factory() {
    }

    public ArrayList<StationsModel> factorizeResponse(JSONObject response){

        JSONArray array;
        ArrayList<StationsModel> stationsArray = new ArrayList<>();
        try {
            array = response.getJSONArray("Data");

            for(int i = 0; i < array.length(); i++){

                JSONObject j = array.optJSONObject(i);

                StationsModel s = new StationsModel();

                s.stationName = j.getString("StationName");
                s.address = j.getString("Address");
                s.emptySpots = j.getInt("EmptySpots");
                s.ocuppiedSpots = j.getInt("OcuppiedSpots");
                s.statusType = j.getString("StatusType");
                s.customIsValid = j.getBoolean("CustomIsValid");
                s.id = j.getInt("Id");
                s.idStatus = j.getInt("IdStatus");
                s.isValid = j.getBoolean("IsValid");
                s.lastSyncDate = j.getString("LastSyncDate");
                s.latitude = j.getDouble("Latitude");
                s.longitude = j.getDouble("Longitude");
                s.maximumNumberOfBikes = j.getInt("MaximumNumberOfBikes");
                s.stationStatus = j.getString("Status");

                stationsArray.add(s);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return stationsArray;
    }
}
