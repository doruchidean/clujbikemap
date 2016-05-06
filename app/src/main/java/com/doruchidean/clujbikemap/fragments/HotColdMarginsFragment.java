package com.doruchidean.clujbikemap.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.doruchidean.clujbikemap.R;
import com.doruchidean.clujbikemap.helpers.Callbacks;
import com.doruchidean.clujbikemap.helpers.PersistenceManager;
import com.edmodo.rangebar.RangeBar;

/**
 * Created by Doru on 03/05/16.
 *
 */
public class HotColdMarginsFragment extends Fragment {

	private static Callbacks.SettingsDialogsCallback mListener;

	public static HotColdMarginsFragment newInstance(Callbacks.SettingsDialogsCallback listener){
		mListener = listener;
		return new HotColdMarginsFragment();
	}

	@Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		final PersistenceManager pm = PersistenceManager.getInstance(getContext());

		final int mColdLimit = pm.getColdLimit();
		final int mHotLimit = pm.getHotLimit();

		View fragmentView = inflater.inflate(R.layout.fragment_hot_cold_margings, container, false);

		final TextView tvColdMargin = (TextView) fragmentView.findViewById(R.id.tv_dialog_margins_cold);
		final TextView tvHotMargin = (TextView) fragmentView.findViewById(R.id.tv_dialog_margins_hot);
		Button btnSet = (Button) fragmentView.findViewById(R.id.btn_set);

		btnSet.setOnClickListener(onClickSetButton);

		tvColdMargin.setText(String.format("%s %s", getContext().getString(R.string.dialog_margins_cold), mColdLimit));
		tvHotMargin.setText(String.format("%s %s", getContext().getString(R.string.dialog_margins_hot), mHotLimit));

		final byte hypotheticalMaxRange = 30; //increase if necessary

		int lineColor = ContextCompat.getColor(getContext(), R.color.station_green);
		int tickColor = ContextCompat.getColor(getContext(), R.color.color_primary_dark);

		RangeBar rangeBar = (RangeBar) fragmentView.findViewById(R.id.rangebar);
		rangeBar.setTickCount(hypotheticalMaxRange + 1);
		rangeBar.setThumbIndices(mColdLimit, hypotheticalMaxRange - mHotLimit);
		rangeBar.setTickHeight(12);
		rangeBar.setConnectingLineColor(lineColor);
		rangeBar.setBarColor(tickColor);
		rangeBar.setBackgroundResource(R.drawable.background_rangebar);
		rangeBar.setThumbImageNormal(R.drawable.station_online);
		rangeBar.setThumbImagePressed(R.drawable.station_online);
		rangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
			@Override
			public void onIndexChangeListener(RangeBar rangeBar, int i, int i1) {
				pm.setColdLimit(i);
				pm.setHotLimit(hypotheticalMaxRange - i1);

				tvColdMargin.setText(String.format("%s %s", getContext().getString(R.string.dialog_margins_cold), i));
				tvHotMargin.setText(String.format("%s %s", getContext().getString(R.string.dialog_margins_hot), hypotheticalMaxRange - i1));
			}
		});

		return fragmentView;
	}

	private View.OnClickListener onClickSetButton = new View.OnClickListener() {
		@Override public void onClick(View v) {
			mListener.setUpMap();
		}
	};
}
