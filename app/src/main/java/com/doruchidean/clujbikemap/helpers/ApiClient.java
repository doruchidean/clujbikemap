package com.doruchidean.clujbikemap.helpers;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;

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
		HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
		interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
		client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
	}

	public static ApiClient getInstance(){
		return instance;
	}

	public void getStations(Callback callback){
		String baseUrl = "http://84.232.185.103";
		String getStations = "/Station/Read";

		Request request = new Request.Builder()
			.url(baseUrl + getStations)
			.post(new FormBody.Builder().build())
			.build();
		Call call = client.newCall(request);
		call.enqueue(callback);
	}


	public void getBusSchedule(Callback callback, String busNumber){

		String busUrl = "http://ctp.gapwalk.com/?orar=BUS_PERIOD";
		String url = GeneralHelper.resolveDayOfWeekInUrl(busNumber, busUrl);

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

		Request request = new Request.Builder()
			.url(url)
			.build();
		Call response = client.newCall(request);
		response.enqueue(callback);
	}

	public void getAdDetails(Callback callback){
		Request request = new Request.Builder()
			.url("https://clujbikemap.firebaseio.com/.json")
			.build();

		Call response = client.newCall(request);
		response.enqueue(callback);
	}

	public void getAllBuses(Callback callback) {
		client.newCall(new Request.Builder()
				.url("http://ctp.gapwalk.com/?linii=linii")
				.build()
			)
			.enqueue(callback);
	}
}
