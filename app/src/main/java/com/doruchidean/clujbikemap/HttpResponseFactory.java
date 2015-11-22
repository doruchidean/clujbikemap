package com.doruchidean.clujbikemap;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Doru on 19/11/15.
 * This class creates models from server json response;
 */
public class HttpResponseFactory {
    private static HttpResponseFactory ourInstance = new HttpResponseFactory();

    public static HttpResponseFactory getInstance() {
        return ourInstance;
    }

    private HttpResponseFactory() {
    }

    public ArrayList<StationsModel> factorizeResponse(JSONObject response){

        JSONArray array;
        ArrayList<StationsModel> stationsArray = new ArrayList<>();
        try {
            array = response.getJSONArray("Data");

            for(int i = 0; i < array.length(); i++){

                JSONObject j = array.optJSONObject(i);

                StationsModel s = new StationsModel();

                s.address = j.getString("Address");
                s.customIsValid = j.getBoolean("CustomIsValid");
                s.emptySpots = j.getInt("EmptySpots");
                s.id = j.getInt("Id");
                s.idStatus = j.getInt("IdStatus");
                s.isValid = j.getBoolean("IsValid");
                s.lastSyncDate = j.getString("LastSyncDate");
                s.latitude = j.getDouble("Latitude");
                s.longitude = j.getDouble("Longitude");
                s.maximumNumberOfBikes = j.getInt("MaximumNumberOfBikes");
                s.ocuppiedSpots = j.getInt("OcuppiedSpots");
                s.stationName = j.getString("StationName");
                s.stationStatus = j.getString("Status");
                s.statusType = j.getString("StatusType");

                stationsArray.add(s);

                Log.d("traces", s.stationName + " " + s.statusType + " biciclete " + s.ocuppiedSpots);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return stationsArray;
    }
}
