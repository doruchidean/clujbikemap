package com.doruchidean.clujbikemap;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.edmodo.rangebar.RangeBar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Collections;

public class MapsActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ApiCallbacks, View.OnClickListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ArrayList<MarkerOptions> mapMarkers = new ArrayList<>();

    private ArrayList<StationsModel> mStationsArray = new ArrayList<>();
    private GoogleApiClient mGoogleApiClient;
    private double userLatitude = 46.775627, userLongitude = 23.590935;

    private int overallBikes = 0, overallSpots = 0, overallTotal = 0;

    private ImageButton btnShowFavourites, btnTimer;
    private View stationDialog;
    private TextView stationDialogName, stationDialogAddress, stationDialogEmptySpots,
            stationDialogOccupiedSpots, stationDialogStatus;
    private ImageView stationDialogFavouriteButton;
    private AlarmManager alarmManager;
    private PendingIntent alarmPendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        PersistenceManager.getInstance().loadData(this);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_maps);

        btnShowFavourites = (ImageButton) findViewById(R.id.btn_show_favourites);
        btnTimer = (ImageButton) findViewById(R.id.btn_timer);
        btnShowFavourites.setBackgroundResource(PersistenceManager.getInstance().getShowFavouritesOnly() ?
                R.drawable.ic_favourite : R.drawable.ic_favourites_pressed);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!hasGooglePlayServicesAvailable()) return;

        if (!hasNetworkConnection()) {
            askForInternetConnection();
            return;
        }

        ApiClient.getInstance().getStations(MapsActivity.this);

        setUpMapIfNeeded();

        buildGoogleApiClient();

    }


    private void askForInternetConnection() {

        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle(getString(R.string.dialog_open_settings))
                .setPositiveButton("SETTINGS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_SETTINGS));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        dialog = builder.create();
        dialog.show();

    }

    private boolean hasNetworkConnection() {

        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo[] netInfo = cm.getAllNetworkInfo();

        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }

        return haveConnectedWifi || haveConnectedMobile;
    }

    private boolean hasGooglePlayServicesAvailable() {
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS;

    }

    private void setCoordinatesOfUserLocation() {

        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (lastLocation != null) {
            //success getting userLatitude and userLongitude
            userLatitude = lastLocation.getLatitude();
            userLongitude = lastLocation.getLongitude();
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLatitude, userLongitude), 16));
    }

    protected synchronized void buildGoogleApiClient() {

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLatitude, userLongitude), 16));
                setUpMap();
            }
        }
    }

    private void createMarkers(){
        MarkerOptions markerOptions;
        PersistenceManager values = PersistenceManager.getInstance();

        for (int i = 0; i < mStationsArray.size(); i++) {

            StationsModel station = mStationsArray.get(i);

            markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(station.latitude, station.longitude));
            markerOptions.title(station.stationName);

            if (station.ocuppiedSpots == 0) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.station_offline));
            } else if (station.ocuppiedSpots < values.getColdLimit()) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.station_underpopulated));
            } else if (station.ocuppiedSpots < station.maximumNumberOfBikes - values.getHotLimit()) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.station_online));
            } else {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.station_overpopulated));
            }

            mapMarkers.add(markerOptions);

        }

        if(PersistenceManager.getInstance().getShowFavouritesOnly()){
            hideNonFavouriteMarkers();
        }

    }

    private void setUpMap() {

        mMap.clear();

        for(MarkerOptions m : mapMarkers){
            mMap.addMarker(m);
        }

        mMap.setMyLocationEnabled(true);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                onMarkerClickFunction(marker);

                return false;
            }
        });
    }

    private StationsModel binarySearchStation(String markerTitleToFind){

        int lowEnd = 0;
        int highEnd = mStationsArray.size()-1;
        int middle, compare;

        while (lowEnd <= highEnd){
            middle = (lowEnd+highEnd)/2;

            compare = markerTitleToFind.compareToIgnoreCase(mStationsArray.get(middle).stationName);

            if(compare < 0){
                highEnd = middle-1;
            }else if(compare > 0){
                lowEnd = middle +1;
            }else{
               return mStationsArray.get(middle);
            }

        }

        return null;
    }

    private void onMarkerClickFunction(final Marker marker){

        final StationsModel station = binarySearchStation(marker.getTitle());

        if(station == null){
            return;
        }

        stationDialog = View.inflate(MapsActivity.this, R.layout.dialog_station, null);
        stationDialogName = (TextView) stationDialog.findViewById(R.id.tvDialogName);
        stationDialogAddress = (TextView) stationDialog.findViewById(R.id.tvDialogAddress);
        stationDialogEmptySpots = (TextView) stationDialog.findViewById(R.id.tvEmptySpots);
        stationDialogOccupiedSpots = (TextView) stationDialog.findViewById(R.id.tvOcuppiedSpots);
        stationDialogStatus = (TextView) stationDialog.findViewById(R.id.tvStatus);
        stationDialogFavouriteButton = (ImageView) stationDialog.findViewById(R.id.btn_add_favourite);

        stationDialogName.setText(station.stationName);
        stationDialogAddress.setText(station.address);
        stationDialogEmptySpots.setText(String.format("Locuri libere: %s", station.emptySpots));
        stationDialogOccupiedSpots.setText(String.format("Biciclete disponibile: %s", station.ocuppiedSpots));
        stationDialogStatus.setText(String.format("Status: %s", station.statusType));
        stationDialogFavouriteButton.setBackgroundResource(station.isFavourite ? R.drawable.ic_favourite : R.drawable.ic_add_favourite);

        stationDialogFavouriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (station.isFavourite) {
                    v.setBackgroundResource(R.drawable.ic_add_favourite);
                    station.isFavourite = false;
                    PersistenceManager.getInstance().removeFavouriteStation(station.stationName);
                    marker.setVisible(false);
                } else {
                    v.setBackgroundResource(R.drawable.ic_favourite);
                    station.isFavourite = true;
                    PersistenceManager.getInstance().addFavouriteStation(station.stationName);
                }

            }
        });

        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        })
                .setCancelable(true)
                .setView(stationDialog);
        dialog = builder.create();
        dialog.show();
    }

    private void hideNonFavouriteMarkers(){
        StationsModel matchingStation;
        for(MarkerOptions m : mapMarkers){
            matchingStation = binarySearchStation(m.getTitle());
            if(matchingStation != null) {
                m.visible(PersistenceManager.getInstance().isFavourite(matchingStation.stationName));
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        //check if location services is turned on
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (!gps_enabled) {
            AlertDialog dialog;

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Use GPS location?");
            builder.setPositiveButton("Location", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MapsActivity.this.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog = builder.create();
            dialog.show();
        }

        setCoordinatesOfUserLocation();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.maps_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.refresh_rate:
                ApiClient.getInstance().getStations(MapsActivity.this);
                break;
            case R.id.hot_cold_margins:
                showMarginsDialog();
                break;
            case R.id.contact:
                showContactDialog();
                break;
            case R.id.overall_stats:
                showOverallStatsDialog();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showOverallStatsDialog() {
        View dialogContainer = View.inflate(MapsActivity.this, R.layout.dialog_overall_stats, null);

        final TextView tvBikes = (TextView) dialogContainer.findViewById(R.id.tv_overall_stats_bikes);
        final TextView tvSpots = (TextView) dialogContainer.findViewById(R.id.tv_overall_stats_empty_spots);
        final TextView tvTotal = (TextView) dialogContainer.findViewById(R.id.tv_overall_stats_total);

        tvBikes.setText(String.format("Total Biciclete: %s", overallBikes));
        tvSpots.setText(String.format("Total Locuri Libere: %s", overallSpots));
        tvTotal.setText(String.format("Total Locuri: %s", overallTotal));

        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle(getString(R.string.options_menu_overall_stats))
                .setView(dialogContainer)
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        dialog = builder.create();
        dialog.show();
    }

    private void showMarginsDialog() {

        final PersistenceManager values = PersistenceManager.getInstance();

        final int mColdLimit = values.getColdLimit();
        final int mHotLimit = values.getHotLimit();

        View dialogContainer = View.inflate(MapsActivity.this, R.layout.dialog_margings, null);

        final TextView tvColdMargin = (TextView) dialogContainer.findViewById(R.id.tv_dialog_margins_cold);
        final TextView tvHotMargin = (TextView) dialogContainer.findViewById(R.id.tv_dialog_margins_hot);

        tvColdMargin.setText(String.format("%s %s", getString(R.string.dialog_margins_cold), mColdLimit));
        tvHotMargin.setText(String.format("%s %s", getString(R.string.dialog_margins_hot), mHotLimit));

        final byte hypotheticRange = 30;

        RangeBar rangeBar = (RangeBar) dialogContainer.findViewById(R.id.rangebar);
        rangeBar.setTickCount(hypotheticRange + 1); //todo test margins working correctly
        rangeBar.setThumbIndices(mColdLimit, hypotheticRange - mHotLimit);
        rangeBar.setConnectingLineColor(Color.GREEN);
        rangeBar.setThumbColorNormal(Color.GREEN);
        rangeBar.setThumbColorPressed(Color.RED);
        rangeBar.setBackgroundResource(R.drawable.background_rangebar);
        rangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onIndexChangeListener(RangeBar rangeBar, int i, int i1) {
                values.setColdLimit(i);
                values.setHotLimit(hypotheticRange - i1);

                tvColdMargin.setText(String.format("%s %s", getString(R.string.dialog_margins_cold), i));
                tvHotMargin.setText(String.format("%s %s", getString(R.string.dialog_margins_hot), hypotheticRange-i1));
            }
        });

        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle(getString(R.string.options_menu_hot_cold_margins))
                .setView(dialogContainer)
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        setUpMap();

                        dialog.dismiss();
                    }
                });
        dialog = builder.create();
        dialog.show();

    }

    private void showContactDialog() {

        View dialogContainer = View.inflate(MapsActivity.this, R.layout.dialog_contact, null);

        TextView btnContactDev = (TextView) dialogContainer.findViewById(R.id.btn_contact_developer);
        TextView btnContactCallCenter = (TextView) dialogContainer.findViewById(R.id.btn_contact_call_center);

        btnContactDev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "ClujBike Map - android support");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello, \n\n");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"doru.chidean@gmai.com"});
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }
        });
        btnContactCallCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:0371784172"));
                startActivity(callIntent);
            }
        });

        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle(getString(R.string.options_menu_contact))
                .setView(dialogContainer)
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onApiCallSuccess(ArrayList<StationsModel> stationsArray) {
        this.mStationsArray = stationsArray;
        Collections.sort(this.mStationsArray);

        Toast.makeText(MapsActivity.this, getString(R.string.toast_up_to_date), Toast.LENGTH_LONG).show();
        createMarkers();
        setUpMap();
        for (StationsModel s : mStationsArray) {
            overallBikes += s.ocuppiedSpots;
            overallSpots += s.emptySpots;
            overallTotal += s.maximumNumberOfBikes;
            s.isFavourite = PersistenceManager.getInstance().isFavourite(s.stationName);
        }
    }

    @Override
    public void onApiCallFail(String error) {
        Toast.makeText(MapsActivity.this, getString(R.string.failure_getting_stations) + error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case(R.id.btn_show_favourites):

                if(PersistenceManager.getInstance().getShowFavouritesOnly()){
                    btnShowFavourites.setBackgroundResource(R.drawable.ic_favourites_pressed);
                    PersistenceManager.getInstance().setShowFavouritesOnly(false);
                    for(MarkerOptions m : mapMarkers){
                        m.visible(true);
                    }
                }else{
                    if (PersistenceManager.getInstance().getFavouriteStations().size() > 0) {
                        btnShowFavourites.setBackgroundResource(R.drawable.ic_favourite);
                        PersistenceManager.getInstance().setShowFavouritesOnly(true);
                        hideNonFavouriteMarkers();
                    } else {
                        Toast.makeText(MapsActivity.this, getString(R.string.toast_no_favourites), Toast.LENGTH_LONG).show();
                    }
                }

                setUpMap();
                break;
            case (R.id.btn_timer):

                if (!btnTimer.isSelected()) {
                    alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    long alarmTime = SystemClock.elapsedRealtime() + PersistenceManager.getInstance().getTimerMinutes() * 1000;
                    Intent notificationHandlerIntent = new Intent(this, NotificationHandler.class);
                    alarmPendingIntent = PendingIntent.getBroadcast(this, RESULT_OK, notificationHandlerIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, alarmPendingIntent);

                    btnTimer.setBackgroundResource(R.drawable.ic_timer_pressed);
                    btnTimer.setSelected(true);

                    Toast.makeText(MapsActivity.this, getString(R.string.alarm_on), Toast.LENGTH_SHORT).show();
                }else{
                    alarmManager.cancel(alarmPendingIntent);
                    alarmManager = null;
                    alarmPendingIntent = null;

                    btnTimer.setBackgroundResource(R.drawable.ic_timer);
                    btnTimer.setSelected(false);

                    Toast.makeText(MapsActivity.this, getString(R.string.alarm_off), Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    int backPressedCount = 0;
    @Override
    public void onBackPressed() {

        backPressedCount ++;
        if(backPressedCount == 2){
            super.onBackPressed();
            return;
        }
        Handler h = new Handler();
        h.postDelayed(
            new Runnable(){
                @Override
                public void run() {
                    backPressedCount = 0;
                }
            },
            3500
        );

        Toast.makeText(this, getString(R.string.confirm_exit_application)+" " +getString(R.string.app_name), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        PersistenceManager.getInstance().saveData(this);
        super.onDestroy();
    }

    public static void trace(String s){
        Log.d("traces", s);
    }
}
