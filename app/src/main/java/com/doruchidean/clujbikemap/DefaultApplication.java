package com.doruchidean.clujbikemap;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by Doru on 06/06/16.
 *
 */
public class DefaultApplication extends Application {
	private Tracker mTracker;

	synchronized public Tracker getDefaultAnalyticsTracker(){
		if(mTracker == null){
			GoogleAnalytics googleAnalytics = GoogleAnalytics.getInstance(this);
			mTracker = googleAnalytics.newTracker(R.xml.global_tracker);
		}
		return mTracker;
	}
}
