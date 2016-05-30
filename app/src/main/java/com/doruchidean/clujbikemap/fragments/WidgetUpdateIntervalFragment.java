package com.doruchidean.clujbikemap.fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import com.doruchidean.clujbikemap.R;
import com.doruchidean.clujbikemap.helpers.GeneralHelper;
import com.doruchidean.clujbikemap.helpers.PersistenceManager;
import com.doruchidean.clujbikemap.helpers.WidgetProvider;

/**
 * Created by Doru on 03/05/16.
 *
 */
public class WidgetUpdateIntervalFragment extends Fragment {

	@Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View fragmentView = View.inflate(getContext(), R.layout.fragment_widget_update_interval, null);
		NumberPicker picker = (NumberPicker) fragmentView.findViewById(R.id.picker_dialog_widget_time);
		picker.setMinValue(0);
		picker.setMaxValue(GeneralHelper.WIDGET_UPDATE_HOUR_INTERVALS.length-1);
		picker.setValue(PersistenceManager.getValueIndexForWidgetUpdateInterval(getContext()));
		picker.setDisplayedValues(GeneralHelper.getWidgetPickerDisplayedValues(getContext()));
		picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				PersistenceManager.setValueIndexForWidgetUpdateInterval(getContext(), newVal);
				setAlarmForWidgetUpdate(
					getContext(),
					GeneralHelper.getMillisForWidgetDisplayedValue(newVal)
				);
			}
		});

		return fragmentView;
	}

	public static void setAlarmForWidgetUpdate(Context context, int intervalMillis){
		//set an alarm to update the widget regularly
		AlarmManager alarmManager =
			(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, WidgetProvider.class);
		PendingIntent pendingIntent= PendingIntent.getBroadcast(
			context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		alarmManager.setInexactRepeating(
			AlarmManager.ELAPSED_REALTIME,
			SystemClock.elapsedRealtime(),
			intervalMillis,
			pendingIntent
		);

		Log.d("traces", "widget set to update every: " + intervalMillis/60000);
	}
}
