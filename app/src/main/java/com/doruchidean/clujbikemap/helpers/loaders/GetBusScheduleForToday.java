package com.doruchidean.clujbikemap.helpers.loaders;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

import com.doruchidean.clujbikemap.activities.MapsActivity;
import com.doruchidean.clujbikemap.database.DatabaseHandler;
import com.doruchidean.clujbikemap.helpers.Factory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by Doru on 26/06/16.
 *
 */
public class GetBusScheduleForToday extends AsyncTaskLoader<HashMap<String, ArrayList<String>>> {

	private String mBusNumber;
	public GetBusScheduleForToday(Context context, String busNumber) {
		super(context);
		mBusNumber = busNumber;
	}

	@Override protected void onStartLoading() {
		forceLoad();
	}

	@Override public HashMap<String, ArrayList<String>> loadInBackground() {
		return DatabaseHandler.getInstance(getContext()).getBusScheduleForToday(mBusNumber);
	}
}
