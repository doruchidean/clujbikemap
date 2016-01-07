package com.doruchidean.clujbikemap;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

/**
 * Created by Doru on 19/11/15.
 * This class handles all the server api calls
 */
public class ApiClient {

    private static ApiClient instance = new ApiClient();
    private AsyncHttpClient mClient;
    private String
            baseUrl = "http://84.232.185.103",
            login = "/logare", //todo vezi daca il poti folosi
            getStations = "/Station/Read",
            getCardDetails="/CTIInformation/ReadTransactionDetails/"; //todo vezi daca o mai lucrat baietii si functioneaza callu asta

    public ApiClient(){

        mClient = new AsyncHttpClient();
    }

    public static ApiClient getInstance(){
        return instance;
    }

    public void getStations(final ApiCallbacks caller){

        Log.d("traces", "updating info");

        mClient.post(baseUrl+getStations, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                caller.onApiCallSuccess(Factory.getInstance().factorizeResponse(response));

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                caller.onApiCallFail(throwable.getMessage());
            }
        });
    }

}
