package com.doruchidean.clujbikemap.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.doruchidean.clujbikemap.R;
import com.doruchidean.clujbikemap.helpers.PersistenceManager;

import java.util.HashMap;

/**
 * Created by Doru on 03/05/16.
 *
 */
public class OverallStatsFragment extends Fragment {

	@Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View fragmentView = inflater.inflate(R.layout.fragment_overall_stats, container, false);

		final TextView tvBikes = (TextView) fragmentView.findViewById(R.id.tv_overall_stats_bikes);
		final TextView tvSpots = (TextView) fragmentView.findViewById(R.id.tv_overall_stats_empty_spots);
		final TextView tvTotal = (TextView) fragmentView.findViewById(R.id.tv_overall_stats_total);

		HashMap<String, Integer> stats = PersistenceManager.getOverallStats(getContext());

		tvBikes.setText(String.format(getContext().getString(
			R.string.overall_stats_all_bikes), stats.get(PersistenceManager.OVERALL_BIKES)));
		tvSpots.setText(String.format(getContext().getString(
			R.string.overall_stats_empty_spots), stats.get(PersistenceManager.OVERALL_EMPTY_SPOTS)));
		tvTotal.setText(String.format(getContext().getString(
			R.string.overall_stats_max_nr_bikes), stats.get(PersistenceManager.OVERALL_MAX_NR)));

		return fragmentView;
	}
}
