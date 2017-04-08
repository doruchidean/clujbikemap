package com.doruchidean.clujbikemap.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.doruchidean.clujbikemap.R;
import com.doruchidean.clujbikemap.activities.MapsActivity;
import com.doruchidean.clujbikemap.helpers.GeneralHelper;
import com.doruchidean.clujbikemap.helpers.PersistenceManager;
import com.doruchidean.clujbikemap.models.BikeStation;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Doru on 18/04/16.
 *
 */
public class GoogleMapsInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Context mContext;
    private ArrayList<BikeStation> mStationsArray;

    public GoogleMapsInfoWindowAdapter(Context context, ArrayList<BikeStation> list){
        mContext = context;
        mStationsArray = list;
    }

    public void notifyDataSetChanged(ArrayList<BikeStation> newList){
        mStationsArray.clear();
        mStationsArray.addAll(newList);
    }

    @Override public View getInfoWindow(final Marker marker) {


        final BikeStation station = GeneralHelper.findStationInArray(marker.getTitle(), mStationsArray);

        if(station == null){
            return null;
        }

        View rootView = View.inflate(mContext, R.layout.info_window_station, null);

        GradientDrawable bg = (GradientDrawable) rootView.getBackground();
        int strokeSize = GeneralHelper.getPixelsForDP(mContext, 3);
        if (station.statusType.equalsIgnoreCase("offline")) {
            bg.setStroke(strokeSize, ContextCompat.getColor(mContext, R.color.station_gray));
        } else if (station.occupiedSpots < PersistenceManager.getColdLimit(mContext)) {
            bg.setStroke(strokeSize, ContextCompat.getColor(mContext, R.color.station_blue));
        } else if (station.occupiedSpots < station.maximumNumberOfBikes - PersistenceManager.getHotLimit(mContext)) {
            bg.setStroke(strokeSize, ContextCompat.getColor(mContext, R.color.station_green));
        } else {
            bg.setStroke(strokeSize, ContextCompat.getColor(mContext, R.color.station_red));
        }

        if (station.distanceMinutes != null) {
            TextView tvSteps = (TextView) rootView.findViewById(R.id.tv_station_steps);
            tvSteps.setText(station.distanceSteps);
            tvSteps.setVisibility(View.VISIBLE);
            TextView tvMins = (TextView) rootView.findViewById(R.id.tv_station_minutes);
            tvMins.setText(station.distanceMinutes);
            tvMins.setVisibility(View.VISIBLE);
        }

        TextView stationDialogName = (TextView) rootView.findViewById(R.id.tv_station_name);
        TextView stationDialogAddress = (TextView) rootView.findViewById(R.id.tvDialogAddress);
        TextView stationDialogEmptySpots = (TextView) rootView.findViewById(R.id.tvEmptySpots);
        TextView stationDialogOccupiedSpots = (TextView) rootView.findViewById(R.id.tvOcuppiedSpots);
        TextView stationDialogStatus = (TextView) rootView.findViewById(R.id.tvStatus);

        stationDialogName.setText(station.stationName);
        stationDialogAddress.setText(station.address);
        stationDialogEmptySpots.setText(String.format(mContext.getString(R.string.infowindow_free_spots), station.emptySpots));
        stationDialogOccupiedSpots.setText(String.format(mContext.getString(R.string.infowindow_available_bikes), station.occupiedSpots));
        stationDialogStatus.setText(String.format(mContext.getString(R.string.infowindow_status), station.statusType));

        Drawable isFavourite = ContextCompat.getDrawable(mContext,
                station.isFavourite ? R.drawable.ic_favourite_station : R.drawable.ic_not_favourite_station);

        stationDialogName.setCompoundDrawablesWithIntrinsicBounds(null, null, isFavourite, null);

        //limit warning
        if(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == 23){
            TextView warning = (TextView) rootView.findViewById(R.id.tv_time_warning);
            String s = String.format(
                    mContext.getString(R.string.return_time_limit_warning),
                    (60 - Calendar.getInstance().get(Calendar.MINUTE))
            );

            warning.setText(s);
            warning.setVisibility(View.VISIBLE);
        }

        return rootView;
    }

    @Override public View getInfoContents(Marker marker) {
        return null;
    }
}
