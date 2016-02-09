package com.doruchidean.clujbikemap;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Doru on 19/11/15.
 * This class handles all the server api calls
 */
public class ApiClient {

    private static ApiClient instance = new ApiClient();
    private AsyncHttpClient mClient;
    private String
            baseUrl = "http://84.232.185.103",
            getStations = "/Station/Read",
            busUrl = "http://ctpcj.ro/orare/csv/orar_BUS_PERIOD.csv",
            login = "/logare", //todo vezi daca il poti folosi
            getCardDetails="/CTIInformation/ReadTransactionDetails/"; //todo vezi daca functioneaza callu asta

    public ApiClient(){

        mClient = new AsyncHttpClient();
    }

    public static ApiClient getInstance(){
        return instance;
    }

    public void getStations(final Callbacks.ApiCallbacks caller){

        Log.d("traces", "updating info");

        mClient.post(baseUrl + getStations, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                caller.onApiCallSuccessStations(Factory.getInstance().factorizeResponse(response));

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                caller.onApiCallFail(throwable.getMessage());
            }
        });
    }

    public void getBusSchedule(final Callbacks.ApiCallbacks caller, String bus){

        String url = Factory.getInstance().resolveBusInUrl(bus, busUrl);

        trace("getting bus " + bus + " at" + url);

        String[] contentTypes = {"text/csv"};
        mClient.get(url, new BinaryHttpResponseHandler(contentTypes) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] binaryData) {

                HashMap<String, ArrayList<String>> leavingMinutes = Factory.getInstance().readCsv(new ByteArrayInputStream(binaryData));
                caller.onApiCallSuccessBusLeaving(leavingMinutes);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] binaryData, Throwable error) {
                trace("FAILED GETTING BUS SCHEDULE " + error.getMessage());
                caller.onApiCallFail(error.getMessage());
            }
        });

    }

    private void trace(String s){
        Log.d("traces", s);
    }


}
