package com.doruchidean.clujbikemap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by Doru on 19/11/15.
 * This class creates models from server json response;
 */
public class Factory {
    private static Factory ourInstance = new Factory();

    public static final String
            MINUTES_CAPAT_1 = "capat1",
            MINUTES_CAPAT_2="capat2",
            PLECARI_CAPAT_1="plecari1",
            PLECARI_CAPAT_2="pelcari2",
            NUME_CAPETE="numecapete";

    public static final int minMinutes = 0, maxMinutes=45;

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

    public String getBusNumber(String busName){
        return busName.split(":")[0];
    }
    public String resolveBusInUrl(String busName, String url){
        Calendar calendar = Calendar.getInstance();

        String busExtension = getBusNumber(busName);

        switch (calendar.get(Calendar.DAY_OF_WEEK)){
            case(6): busExtension = busExtension+"_s";
                break;
            case(7): busExtension = busExtension+"_d";
                break;
            default: busExtension = busExtension+"_lv";
        }

        return url.replace("BUS_PERIOD", busExtension);
    }

    public String getPlecariAtThisHour(ArrayList<String> plecariTotale){

        Calendar calendar = Calendar.getInstance();

        String result="|";
        String plecare;

        for(String s : plecariTotale){
            plecare = isBusTimeInNextHour(false, s, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
            if(plecare != null){
                result += plecare + "| ";
            }
        }

        if(result.length() == 1){
            result = String.format(" > %s min", maxMinutes);
        }
        return result;
    }

    private String isBusTimeInNextHour(boolean requiresMinutes, String plecare, int currentHour, int currentMinute){

        String[] busHourAndMin = plecare.split(":");
        int busHour = Integer.valueOf(busHourAndMin[0]);
        int currentTimeSeconds = currentHour*60*60 + currentMinute*60;

        if(currentHour == busHour || currentHour+1==busHour) {

            int busMin = Integer.valueOf(busHourAndMin[1]);
            int busTimeSeconds = busHour * 60 * 60 + busMin * 60;
            int timeDifference = (busTimeSeconds - currentTimeSeconds) / 60;

            if (timeDifference>=minMinutes && timeDifference <= maxMinutes) {

                if (requiresMinutes) {
                    return String.valueOf(timeDifference);
                } else {
                    return plecare;
                }
            }
        }
        return null;
    }

    public HashMap<String, ArrayList<String>> readCsv(InputStream inputStream){

        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);
        String minutesRemaining;

        HashMap<String, ArrayList<String>> resultList = new HashMap<>();
        ArrayList<String> numeCapete = new ArrayList<>();
        ArrayList<String> minutesCapatul1 = new ArrayList<>();
        ArrayList<String> minutesCapatul2 = new ArrayList<>();
        ArrayList<String> plecariCapatul1 = new ArrayList<>();
        ArrayList<String> plecariCapatul2 = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String csvLine;
            while ((csvLine = reader.readLine()) != null) {

                if(csvLine.startsWith(",")){
                    csvLine = " " +csvLine;
                }
                if(csvLine.endsWith(",")){
                    csvLine = csvLine + " ";
                }

                String[] row = csvLine.split(",");

                //row 0 reprezinta orele pt capatul 1 al linii
                if(row[0].contains(":")) {

                    plecariCapatul1.add(row[0]);

                    minutesRemaining = isBusTimeInNextHour(true, row[0], currentHour, currentMinute);
                    if(minutesRemaining != null){
                        minutesCapatul1.add(minutesRemaining);
                    }

                }else if(row[0].contains("route_long_name")){  //if row has the route we show it in the bus bar
                    String[] capete = row[1].split(" - ");
                    numeCapete.add(capete[0]+":");
                    numeCapete.add(capete[1]+":");
                }
                //row 1 reprezinta orele pt capatul 2 al linii
                if(row[1].contains(":")) {

                    plecariCapatul2.add(row[1]);

                    minutesRemaining = isBusTimeInNextHour(true, row[1], currentHour, currentMinute);
                    if(minutesRemaining != null){
                        minutesCapatul2.add(minutesRemaining);
                    }
                }
            }

            resultList.put(NUME_CAPETE, numeCapete);
            resultList.put(MINUTES_CAPAT_1, minutesCapatul1);
            resultList.put(MINUTES_CAPAT_2, minutesCapatul2);
            resultList.put(PLECARI_CAPAT_1, plecariCapatul1);
            resultList.put(PLECARI_CAPAT_2, plecariCapatul2);
        }
        catch (IOException ex) {
            throw new RuntimeException("Error in reading CSV file: "+ex);
        }
        try {
            inputStream.close();
        }
        catch (IOException e) {
            throw new RuntimeException("Error while closing input stream: "+e);
        }
        return resultList;
    }

}
