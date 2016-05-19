package com.doruchidean.clujbikemap.helpers;

import android.util.Log;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by Doru on 19/11/15.
 * This class handles all the server api calls
 */
public class ApiClient {

	private static ApiClient instance = new ApiClient();
	//  private String login = "/logare"; //todo vezi daca il poti folosi
//  private String getCardDetails="/CTIInformation/ReadTransactionDetails/"; //todo vezi daca functioneaza callu asta
	private final OkHttpClient client;

	public ApiClient(){
		client = new OkHttpClient();
	}

	public static ApiClient getInstance(){
		return instance;
	}

	public void getStations(Callback callback){
		String baseUrl = "http://84.232.185.103";
		String getStations = "/Station/Read";
		trace("get stations: " + baseUrl + getStations);

		Request request = new Request.Builder()
			.url(baseUrl + getStations)
			.post(new FormBody.Builder().build())
			.build();
		Call call = client.newCall(request);
		call.enqueue(callback);
	}


	public void getBusSchedule(Callback callback, String busNumber){

		String busUrl = "http://ctpcj.ro/orare/csv/orar_BUS_PERIOD.csv";
		String url = GeneralHelper.resolveBusInUrl(busNumber, busUrl);

		trace("getting bus " + busNumber + " at: " + url);

		Request request = new Request.Builder()
			.url(url)
			.build();
		Call response = client.newCall(request);
		response.enqueue(callback);

	}

	public void getDistance(String from, String to, String key, Callback callback){
		HttpUrl url = new HttpUrl.Builder()
			.scheme("https")
			.host("maps.googleapis.com")
			.addPathSegments("maps/api/distancematrix/json")
			.addQueryParameter("origins", from)
			.addQueryParameter("destinations", to)
			.addQueryParameter("mode", "walking")
			.addQueryParameter("key", key)
			.build();

		trace("getDistance:  " + url.toString());

		Request request = new Request.Builder()
			.url(url)
			.build();
		Call response = client.newCall(request);
		response.enqueue(callback);
	}

	private void trace(String s){
		Log.d("traces", s);
	}

}
