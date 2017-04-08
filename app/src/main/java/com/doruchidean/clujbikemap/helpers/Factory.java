package com.doruchidean.clujbikemap.helpers;

import com.doruchidean.clujbikemap.models.BikeStation;
import com.doruchidean.clujbikemap.models.BusSchedule;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Response;

/**
 * Created by Doru on 19/11/15.
 * This class creates models from server json response;
 */
public class Factory {
    private static Factory ourInstance = new Factory();

    public static final String
            PLECARI_CAPAT_1="plecari1",
            PLECARI_CAPAT_2="pelcari2",
            NUME_CAPETE="numecapete";

    public static Factory getInstance() {
        return ourInstance;
    }

    private Factory() {
    }

    public ArrayList<BikeStation> factorizeResponse(JSONObject response){

        JSONArray array;
        ArrayList<BikeStation> stationsArray = new ArrayList<>();
        try {
            array = response.getJSONArray("Data");

            for(int i = 0; i < array.length(); i++){

                JSONObject j = array.optJSONObject(i);

                BikeStation bikeStation = new BikeStation();

                bikeStation.stationName = j.getString("StationName");
                bikeStation.address = j.getString("Address");
                bikeStation.emptySpots = j.getInt("EmptySpots");
                bikeStation.occupiedSpots = j.getInt("OcuppiedSpots");
                bikeStation.statusType = j.getString("StatusType");
                bikeStation.customIsValid = j.getBoolean("CustomIsValid");
                bikeStation.id = j.getInt("Id");
                bikeStation.idStatus = j.getInt("IdStatus");
                bikeStation.isValid = j.getBoolean("IsValid");
                bikeStation.lastSyncDate = j.getString("LastSyncDate");
                bikeStation.latitude = j.getDouble("Latitude");
                bikeStation.longitude = j.getDouble("Longitude");
                bikeStation.maximumNumberOfBikes = j.getInt("MaximumNumberOfBikes");
                bikeStation.stationStatus = j.getString("Status");

                if (!(bikeStation.stationName.equalsIgnoreCase("statie training 6")
                        || bikeStation.stationName.equalsIgnoreCase("statie virtuala"))) {

                    stationsArray.add(bikeStation);
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return stationsArray;
    }

    public List<BusSchedule> parseBusNames(Response response) {
        List<BusSchedule> result = new ArrayList<>();

        try {
            JSONObject raw = new JSONObject(response.body().string());
            JSONArray linii = raw.getJSONArray("linii");
            int size = linii.length();
            for (int i=0; i<size; i++) {
                JSONObject o = linii.getJSONObject(i);
                BusSchedule b = new BusSchedule();

                b.setName(o.getString("nume"));
                b.setBusNumber(o.getString("denumire"));
                b.setNameCapat1(o.getString("plecare1"));
                b.setNameCapat2(o.getString("plecare2"));

                result.add(b);
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    public BusSchedule factorizeBusSchedule(Response response, BusSchedule busSchedule) {

        try {
            JSONObject raw = new JSONObject(response.body().string());
            JSONArray plecari1 = raw.getJSONArray("plecare1");
            JSONArray plecari2 = raw.getJSONArray("plecare2");
            JSONArray plecari1S = raw.getJSONArray("plecare_s1");
            JSONArray plecari2S = raw.getJSONArray("plecare_s2");
            JSONArray plecari1D = raw.getJSONArray("plecare_d1");
            JSONArray plecari2D = raw.getJSONArray("plecare_d2");

            busSchedule.setPlecariCapat1LV(stripString(plecari1.toString()));
            busSchedule.setPlecariCapat2LV(stripString(plecari2.toString()));
            busSchedule.setPlecariCapat1S(stripString(plecari1S.toString()));
            busSchedule.setPlecariCapat2S(stripString(plecari2S.toString()));
            busSchedule.setPlecariCapat1D(stripString(plecari1D.toString()));
            busSchedule.setPlecariCapat2D(stripString(plecari2D.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return busSchedule;
    }

    public String stripString(String s) {
        return s.replaceAll("\\[", "")
                .replaceAll("\\]", "")
                .replaceAll("\"", "");
    }
}
