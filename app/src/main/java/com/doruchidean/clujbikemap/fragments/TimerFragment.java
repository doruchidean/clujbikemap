package com.doruchidean.clujbikemap.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import com.doruchidean.clujbikemap.R;
import com.doruchidean.clujbikemap.helpers.GeneralHelper;
import com.doruchidean.clujbikemap.helpers.PersistenceManager;

/**
 * Created by Doru on 03/05/16.
 *
 */
public class TimerFragment extends Fragment {

	@Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		final PersistenceManager persistenceManager = PersistenceManager.getInstance(getContext());

		View fragmentView = inflater.inflate(R.layout.fragment_timer_limit, container, false);

		NumberPicker picker = (NumberPicker) fragmentView.findViewById(R.id.picker_dialog_timer);
		picker.setMinValue(0);
		picker.setMaxValue(GeneralHelper.TIMER_VALUES.length-1);
		picker.setDisplayedValues(GeneralHelper.getTimerPickerDisplayedValues());
		picker.setValue(persistenceManager.getTimerValueIndex());
		picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				persistenceManager.setTimerValueIndex(newVal);
			}
		});

		return fragmentView;
	}
}
