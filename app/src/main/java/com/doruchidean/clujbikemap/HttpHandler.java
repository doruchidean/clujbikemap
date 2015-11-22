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
public class HttpHandler {

    private static HttpHandler instance = new HttpHandler();
    private AsyncHttpClient mClient;

    public HttpHandler(){

        mClient = new AsyncHttpClient();
    }

    public static HttpHandler getInstance(){
        return instance;
    }

    public void getStations(final ApiCallbacks caller){

        Log.d("traces", "updating info");

        mClient.post("http://84.232.185.103/Station/Read", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                caller.onApiCallSuccess(HttpResponseFactory.getInstance().factorizeResponse(response));

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                caller.onApiCallFail(throwable.getMessage());
            }
        });
    }

}
