package com.doruchidean.clujbikemap.helpers;

import com.doruchidean.clujbikemap.models.BikeStation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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

        stationsArray.add(bikeStation);

      }

    } catch (JSONException e) {
      e.printStackTrace();
    }

    return stationsArray;
  }

  public HashMap<String, ArrayList<String>> readCsv(byte[] binaryData){

    InputStream inputStream = new ByteArrayInputStream(binaryData);

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

          minutesRemaining = GeneralHelper.isBusTimeInNextHour(true, row[0], currentHour, currentMinute);
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

          minutesRemaining = GeneralHelper.isBusTimeInNextHour(true, row[1], currentHour, currentMinute);
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
