package com.doruchidean.clujbikemap;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, Callbacks.ApiCallbacks, Callbacks.SettingsDialogsCallback, View.OnClickListener {

    private ArrayList<MarkerOptions> mapMarkers = new ArrayList<>();
    private ArrayList<StationsModel> mStationsArray = new ArrayList<>();
    private ArrayList<String> plecariCapat1 = new ArrayList<>();
    private ArrayList<String> plecariCapat2 = new ArrayList<>();
    private String capat1Title, capat2Title;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private double userLatitude = 46.775627, userLongitude = 23.590935;

    private ImageButton btnShowFavourites, btnTimer;
    private TextView tvBusCapat1, tvBusCapat2, tvSelectedBus;
    private LinearLayout llTimesLeft, llTimesRight;

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
        tvBusCapat1 = (TextView) findViewById(R.id.tv_bus_capat1);
        tvBusCapat2 = (TextView) findViewById(R.id.tv_bus_capat2);
        tvSelectedBus = (TextView) findViewById(R.id.btn_select_bus);
        llTimesLeft = (LinearLayout) findViewById(R.id.ll_times_left);
        llTimesRight = (LinearLayout) findViewById(R.id.ll_times_right);

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

        if(PersistenceManager.getInstance().getSelectedBus().length() > 0){
            ApiClient.getInstance().getBusSchedule(MapsActivity.this, PersistenceManager.getInstance().getSelectedBus());
        }

        setUpMapIfNeeded();

        buildGoogleApiClient();

        refreshUIButtons();
    }

    private void refreshUIButtons(){
        btnShowFavourites.setBackgroundResource(PersistenceManager.getInstance().getShowFavouritesOnly() ?
                R.drawable.ic_favourite : R.drawable.ic_favourites_pressed);
        btnTimer.setBackgroundResource(PersistenceManager.getInstance().getIsCountingDown() ?
                R.drawable.ic_timer_pressed : R.drawable.ic_timer);
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

    @Override
    public void setUpMap() {

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

        View stationDialog = View.inflate(MapsActivity.this, R.layout.dialog_station, null);
        TextView stationDialogName = (TextView) stationDialog.findViewById(R.id.tvDialogName);
        TextView stationDialogAddress = (TextView) stationDialog.findViewById(R.id.tvDialogAddress);
        TextView stationDialogEmptySpots = (TextView) stationDialog.findViewById(R.id.tvEmptySpots);
        TextView stationDialogOccupiedSpots = (TextView) stationDialog.findViewById(R.id.tvOcuppiedSpots);
        TextView stationDialogStatus = (TextView) stationDialog.findViewById(R.id.tvStatus);
        ImageView stationDialogFavouriteButton = (ImageView) stationDialog.findViewById(R.id.btn_add_favourite);

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
                SettingsDialogs.getInstance().showMarginsDialog(MapsActivity.this, this);
                break;
            case R.id.contact:
                SettingsDialogs.getInstance().showContactDialog(MapsActivity.this);
                break;
            case R.id.overall_stats:
                SettingsDialogs.getInstance().showOverallStatsDialog(this);
                break;
            case R.id.timer_limit:
                SettingsDialogs.getInstance().showTimerLimitDialog(this);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onApiCallSuccessStations(ArrayList<StationsModel> stationsArray) {
        this.mStationsArray = stationsArray;
        Collections.sort(this.mStationsArray);

        Toast.makeText(MapsActivity.this, getString(R.string.toast_up_to_date), Toast.LENGTH_LONG).show();
        createMarkers();
        setUpMap();
        SettingsDialogs.getInstance().updateOverallStats(mStationsArray);
    }

    @Override
    public void onApiCallSuccessBusLeaving(HashMap<String, ArrayList<String>> leavingTimes) {

        setTextsInBusBar(leavingTimes.get(Factory.MINUTES_CAPAT_1), llTimesLeft);
        setTextsInBusBar(leavingTimes.get(Factory.MINUTES_CAPAT_2), llTimesRight);
        tvSelectedBus.setText(Factory.getInstance().getBusNumber(PersistenceManager.getInstance().getSelectedBus()));

        plecariCapat1 = leavingTimes.get(Factory.PLECARI_CAPAT_1);
        plecariCapat2 = leavingTimes.get(Factory.PLECARI_CAPAT_2);

        capat1Title = leavingTimes.get(Factory.NUME_CAPETE).get(0);
        capat2Title = leavingTimes.get(Factory.NUME_CAPETE).get(1);
        tvBusCapat1.setText(capat1Title);
        tvBusCapat2.setText(capat2Title);
    }

    private void setTextsInBusBar(ArrayList<String> minTexts, LinearLayout parent){

        parent.removeAllViews();

        int padding2dp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                2,
                getResources().getDisplayMetrics()
        );

        TextView tvSubtitle = new TextView(MapsActivity.this);
        tvSubtitle.setTextColor(Color.WHITE);
        if (minTexts.size() > 0) {
            tvSubtitle.setText(getString(R.string.bus_bar_subtitle_to_be_filled));
            tvSubtitle.setPadding(0,0,padding2dp,0);
        } else {
            tvSubtitle.setText(String.format(getString(R.string.bus_bar_subtitle_full), Factory.maxMinutes));
        }
        parent.addView(tvSubtitle);

        String minString;
        int minInt;
        for(int i=0; i<minTexts.size(); i++){

            TextView tvMinute = new TextView(MapsActivity.this);
            tvMinute.setPadding(padding2dp,0,0,0);
            tvMinute.setSingleLine();

            minString = minTexts.get(i);
            minInt = Integer.valueOf(minString);

            if(minInt <= 3){
                tvMinute.setTextColor(Color.RED);
            }else if(minInt <= 10){
                tvMinute.setTextColor(Color.YELLOW);
            }else{
                tvMinute.setTextColor(Color.GREEN);
            }

            if (i<minTexts.size()-1) {
                minString += ", ";
            }else{
                minString += " min";
            }

            tvMinute.setText(minString);
            parent.addView(tvMinute);
        }
    }

    @Override
    public void onApiCallFail(String error) {
        Toast.makeText(MapsActivity.this, getString(R.string.failure_getting_stations) + error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {

        final PersistenceManager persistenceManager = PersistenceManager.getInstance();

        switch(v.getId()){
            case(R.id.btn_show_favourites):

                if(persistenceManager.getShowFavouritesOnly()){

                    persistenceManager.setShowFavouritesOnly(false);
                    for(MarkerOptions m : mapMarkers){
                        m.visible(true);
                    }
                }else{
                    if (persistenceManager.getFavouriteStations().size() > 0) {

                        persistenceManager.setShowFavouritesOnly(true);
                        hideNonFavouriteMarkers();
                    } else {
                        Toast.makeText(MapsActivity.this, getString(R.string.toast_no_favourites), Toast.LENGTH_LONG).show();
                    }
                }

                setUpMap();
                break;
            case (R.id.btn_timer):

                if (!persistenceManager.getIsCountingDown()) {
                    alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    long alarmTime = SystemClock.elapsedRealtime() + 1000 * 60 * persistenceManager.getTimerMinutes();
                    Intent notificationHandlerIntent = new Intent(this, NotificationHandler.class);
                    alarmPendingIntent = PendingIntent.getBroadcast(this, RESULT_OK, notificationHandlerIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, alarmPendingIntent);

                    persistenceManager.setIsCountingDown(true);

                    Toast.makeText(MapsActivity.this, getString(R.string.alarm_on), Toast.LENGTH_SHORT).show();
                }else{
                    alarmManager.cancel(alarmPendingIntent);
                    alarmManager = null;
                    alarmPendingIntent = null;

                    persistenceManager.setIsCountingDown(false);

                    Toast.makeText(MapsActivity.this, getString(R.string.alarm_off), Toast.LENGTH_SHORT).show();
                }

                break;
            case (R.id.btn_select_bus):

                View busesContainer = View.inflate(MapsActivity.this, R.layout.dialog_buses, null);

                //prepare the list
                List<String> buses = Arrays.asList(getResources().getStringArray(R.array.available_buses));
                ListView lv = (ListView) busesContainer.findViewById(R.id.lv_buses);
                final BusListAdapter busListAdapter = new BusListAdapter(MapsActivity.this, buses);
                int selectedBusPosition=-1;
                for(int i=0; i<buses.size();i++){
                    if(buses.get(i).equalsIgnoreCase(persistenceManager.getSelectedBus())){
                        selectedBusPosition = i;
                    }
                }
                busListAdapter.setSelectedBus(selectedBusPosition);
                lv.setAdapter(busListAdapter);

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        view.setBackgroundColor(Color.GRAY);
                        persistenceManager.setSelectedBus(busListAdapter.getItem(position));
                        busListAdapter.setSelectedBus(position);
                        busListAdapter.notifyDataSetChanged();
                    }
                });

                AlertDialog dialog = new AlertDialog.Builder(MapsActivity.this)
                        .setView(busesContainer)
                        .setTitle(getString(R.string.dialog_add_buses_title)
                        )
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ApiClient.getInstance().getBusSchedule(MapsActivity.this, persistenceManager.getSelectedBus());
                                tvSelectedBus.setText(Factory.getInstance().getBusNumber(persistenceManager.getSelectedBus()));
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                dialog.show();
                break;
            case(R.id.btn_full_bus_schedule):

                View busTimesContainer = View.inflate(MapsActivity.this, R.layout.dialog_bus_times, null);
                TextView tvCapat1 = (TextView) busTimesContainer.findViewById(R.id.tv_bus_times_title_1);
                TextView tvPlecari1 = (TextView) busTimesContainer.findViewById(R.id.tv_bus_times_1);
                TextView tvCapat2 = (TextView) busTimesContainer.findViewById(R.id.tv_bus_times_title_2);
                TextView tvPlecari2 = (TextView) busTimesContainer.findViewById(R.id.tv_bus_times_2);

                tvCapat1.setText(capat1Title);
                tvCapat2.setText(capat2Title);
                String plecari1="";
                for(String s : plecariCapat1){
                    plecari1 += s+"| ";
                }
                tvPlecari1.setText(plecari1);
                String plecari2="";
                for(String s : plecariCapat2){
                    plecari2 += s+"| ";
                }
                tvPlecari2.setText(plecari2);

                new AlertDialog.Builder(MapsActivity.this)
                        .setTitle(getString(R.string.dialog_orar_complet) + " " +
                                Factory.getInstance().getBusNumber(
                                        PersistenceManager.getInstance().getSelectedBus()
                                )
                        )
                        .setView(busTimesContainer)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();
                break;
        }

        refreshUIButtons();
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
                new Runnable() {
                    @Override
                    public void run() {
                        backPressedCount = 0;
                    }
                },
                3500
        );

        Toast.makeText(this, getString(R.string.confirm_exit_application) + " " + getString(R.string.app_name), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {

        PersistenceManager persistenceManager = PersistenceManager.getInstance();
        persistenceManager.saveData(this);

        int widgetId = persistenceManager.getWidgetId(MapsActivity.this);
        if(widgetId > 0) {
            //update widget

            RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.widget);

            remoteViews.setTextViewText(
                    R.id.tv_widget_bus,
                    Factory.getInstance().getBusNumber(persistenceManager.getSelectedBus())
            );
            remoteViews.setTextViewText(R.id.tv_widget_times_titlu_1, capat1Title);
            remoteViews.setTextViewText(R.id.tv_widget_times_titlu_2, capat2Title);
            remoteViews.setTextViewText(
                    R.id.tv_widget_times_capat_1,
                    Factory.getInstance().getPlecariAtThisHour(plecariCapat1)
            );
            remoteViews.setTextViewText(
                    R.id.tv_widget_times_capat_2,
                    Factory.getInstance().getPlecariAtThisHour(plecariCapat2)
            );

            AppWidgetManager.getInstance(MapsActivity.this).updateAppWidget(
                    widgetId,
                    remoteViews
            );
        }
        super.onDestroy();
    }

}
