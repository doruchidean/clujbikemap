package com.doruchidean.clujbikemap.activities;

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
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.doruchidean.clujbikemap.adapters.GoogleMapsInfoWindowAdapter;
import com.doruchidean.clujbikemap.helpers.ApiClient;
import com.doruchidean.clujbikemap.adapters.BusListAdapter;
import com.doruchidean.clujbikemap.helpers.Callbacks;
import com.doruchidean.clujbikemap.helpers.Factory;
import com.doruchidean.clujbikemap.helpers.GeneralHelper;
import com.doruchidean.clujbikemap.helpers.NotificationHandler;
import com.doruchidean.clujbikemap.helpers.PersistenceManager;
import com.doruchidean.clujbikemap.R;
import com.doruchidean.clujbikemap.helpers.SettingsDialogs;
import com.doruchidean.clujbikemap.models.BikeStation;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MapsActivity extends AppCompatActivity
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        Callbacks.SettingsDialogsCallback,
        View.OnClickListener,
        GoogleMap.OnInfoWindowClickListener{

    private ArrayList<MarkerOptions> mapMarkers = new ArrayList<>();
    private ArrayList<BikeStation> mStationsArray = new ArrayList<>();
    private ArrayList<String> plecariCapat1 = new ArrayList<>();
    private ArrayList<String> plecariCapat2 = new ArrayList<>();
    private String capat1Title, capat2Title;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private double userLatitude = 46.775627, userLongitude = 23.590935;

    private ImageButton btnShowFavourites, btnTimer;
    private TextView tvBusCapat1, tvBusCapat2, tvSelectedBus;
    private LinearLayout llTimesLeft, llTimesRight;
    private LinearLayout mBusBar;
    private GoogleMapsInfoWindowAdapter mMapInfoWindowsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

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
        mBusBar = (LinearLayout) findViewById(R.id.bus_bar);
        mBusBar.setVisibility(
                PersistenceManager.getInstance(MapsActivity.this).getShowBusBar() ?
                        View.VISIBLE :
                        View.GONE
        );

        mMapInfoWindowsAdapter = new GoogleMapsInfoWindowAdapter(MapsActivity.this, mStationsArray);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!hasGooglePlayServicesAvailable()) return;

        if (!hasNetworkConnection()) {
            askForInternetConnection();
            return;
        }

        ApiClient.getInstance().getStations(getStationsCallback);

        PersistenceManager pm = PersistenceManager.getInstance(MapsActivity.this);
        if(pm.getBusName().length() > 0){
            ApiClient.getInstance().getBusSchedule(getBusScheduleCallback, pm.getBusName());
        }

        setUpMapIfNeeded();

        buildGoogleApiClient();

        refreshUIButtons();
    }

    private Callback getStationsCallback = new Callback() {
        @Override public void onFailure(Call call, IOException e) {
            trace("fail getStations " + e.getMessage());
        }

        @Override public void onResponse(Call call, Response response) throws IOException {
            String stringResponse = response.body().string();
            trace("response getStations: " + stringResponse);
            try {
                final JSONObject jsonResponse = new JSONObject(stringResponse);
                MapsActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        updateMarkersUI(jsonResponse);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Callback getBusScheduleCallback = new Callback() {
        @Override public void onFailure(Call call, IOException e) {
            trace("fail gettingBus " + e.getMessage());
        }

        @Override public void onResponse(Call call, final Response response) throws IOException {

            final byte[] bytes = response.body().bytes();
            MapsActivity.this.runOnUiThread(
                new Runnable() {
                    public void run() {
                        if (response.code() != 200) {
                            onApiCallFail(response.code());
                            return;
                        }
                        updateBusBarUI(bytes);
                    }
                }
            );
        }
    };

    private void updateMarkersUI(JSONObject jsonResponse){
        mStationsArray = Factory.getInstance().factorizeResponse(jsonResponse);
        Collections.sort(MapsActivity.this.mStationsArray);
        Toast.makeText(MapsActivity.this, getString(R.string.toast_up_to_date), Toast.LENGTH_LONG).show();
        mMapInfoWindowsAdapter.notifyDataSetChanged(mStationsArray);
        setUpMap();
        SettingsDialogs.getInstance().updateOverallStats(MapsActivity.this, mStationsArray);
    }
    
    private void updateBusBarUI(byte[] bytes){
        PersistenceManager pm = PersistenceManager.getInstance(MapsActivity.this);
        pm.setBusSchedule(MapsActivity.this, bytes);
        HashMap<String, ArrayList<String>> leavingTimes = Factory.getInstance().readCsv(bytes);
        setTextsInBusBar(leavingTimes.get(Factory.MINUTES_CAPAT_1), llTimesLeft);
        setTextsInBusBar(leavingTimes.get(Factory.MINUTES_CAPAT_2), llTimesRight);
        tvSelectedBus.setText(GeneralHelper.getBusNumber(pm.getBusName()));

        plecariCapat1 = leavingTimes.get(Factory.PLECARI_CAPAT_1);
        plecariCapat2 = leavingTimes.get(Factory.PLECARI_CAPAT_2);

        capat1Title = leavingTimes.get(Factory.NUME_CAPETE).get(0);
        capat2Title = leavingTimes.get(Factory.NUME_CAPETE).get(1);
        tvBusCapat1.setText(capat1Title);
        tvBusCapat2.setText(capat2Title);
    }

    private void refreshUIButtons(){
        PersistenceManager pm = PersistenceManager.getInstance(MapsActivity.this);
        btnShowFavourites.setBackgroundResource(pm.getShowFavouritesOnly() ?
                R.drawable.ic_favourite : R.drawable.ic_favourites_pressed);
        btnTimer.setBackgroundResource(pm.getIsCountingDown() ?
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
                mMap.setMyLocationEnabled(true);
                mMap.setOnInfoWindowClickListener(this);
                mMap.setInfoWindowAdapter(mMapInfoWindowsAdapter);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLatitude, userLongitude), 16));
                mMap.setOnMarkerClickListener(onMarkerClickListener);

                setUpMap();
            }
        }
    }

    private GoogleMap.OnMarkerClickListener onMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override public boolean onMarkerClick(final Marker marker) {
            ApiClient.getInstance().getDistance(
                    userLatitude + "," + userLongitude,
                    String.valueOf(marker.getPosition().latitude) + ","+
                            String.valueOf(marker.getPosition().longitude),
                    getString(R.string.google_distance_key),
                    new Callback() {
                        @Override public void onFailure(Call call, IOException e) {
                            trace("fail getDistance: " + e.getMessage());
                        }

                        @Override public void onResponse(Call call, Response response) throws IOException {

                            final String[] distance = GeneralHelper.getDistanceFromResponse(response);

                            if (distance[0] != null) {
                                MapsActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        BikeStation station = GeneralHelper.binarySearchStation(marker.getTitle(), mStationsArray);
                                        mStationsArray.get(mStationsArray.indexOf(station)).distanceMinutes = distance[1];
                                        mStationsArray.get(mStationsArray.indexOf(station)).distanceSteps= distance[0];

                                        marker.showInfoWindow();
                                    }
                                });
                            }
                        }
                    }
            );
            return false;
        }
    };

    private void createMarkers(){
        MarkerOptions markerOptions;
        PersistenceManager pm = PersistenceManager.getInstance(MapsActivity.this);

        for (int i = 0; i < mStationsArray.size(); i++) {

            BikeStation station = mStationsArray.get(i);

            markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(station.latitude, station.longitude));
            markerOptions.title(station.stationName);

            if (station.statusType.equalsIgnoreCase("offline")) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.station_offline));
            } else if (station.occupiedSpots < pm.getColdLimit()) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.station_underpopulated));
            } else if (station.occupiedSpots < station.maximumNumberOfBikes - pm.getHotLimit()) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.station_online));
            } else {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.station_overpopulated));
            }

            mapMarkers.add(markerOptions);

        }

        if(pm.getShowFavouritesOnly()){
            hideNonFavouriteMarkers(pm);
        }

    }

    @Override
    public void setUpMap() {

        mMap.clear();
        mapMarkers.clear();
        createMarkers();

        for(MarkerOptions m : mapMarkers){
            mMap.addMarker(m);
        }
    }

    private void hideNonFavouriteMarkers(PersistenceManager pm){
        BikeStation matchingStation;
        for(MarkerOptions m : mapMarkers){
            matchingStation = GeneralHelper.binarySearchStation(m.getTitle(), mStationsArray);
            if(matchingStation != null) {
                m.visible(pm.isFavourite(matchingStation.stationName));
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

        //show_bus_bar button
        menu.getItem(6).setChecked(PersistenceManager.getInstance(MapsActivity.this).getShowBusBar());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.refresh_rate:
                ApiClient.getInstance().getStations(getStationsCallback);
                break;
            case R.id.hot_cold_margins:
                SettingsDialogs.getInstance().showMarginsDialog(MapsActivity.this, this);
                break;
            case R.id.contact:
                SettingsDialogs.getInstance().showContactDialog(MapsActivity.this);
                break;
            case R.id.overall_stats:
                SettingsDialogs.getInstance().showOverallStatsDialog(MapsActivity.this);
                break;
            case R.id.timer_limit:
                SettingsDialogs.getInstance().showTimerLimitDialog(MapsActivity.this);
                break;
            case R.id.widget_refresh_interval:
                SettingsDialogs.getInstance().showWidgetUpdateTimeDialog(MapsActivity.this);
                break;
            case R.id.show_bus_bar:
                onShowBusBarClicked(item);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onShowBusBarClicked(MenuItem item){
        PersistenceManager.getInstance(MapsActivity.this).setShowBusBar(!item.isChecked());

        mBusBar.setVisibility(!item.isChecked() ? View.VISIBLE : View.GONE);

        item.setChecked(!item.isChecked());
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

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
            tvSubtitle.setText(String.format(getString(R.string.bus_bar_subtitle_full), GeneralHelper.maxMinutes));
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

    public void onApiCallFail(int errorCode) {
        trace(errorCode + " error code");
        switch (errorCode){
            case (404):
                Toast.makeText(MapsActivity.this, getString(R.string.failure_page_not_found), Toast.LENGTH_LONG).show();
                tvBusCapat1.setText(getString(R.string.bus_schedule_not_found));
                break;
            default:
                Toast.makeText(MapsActivity.this, getString(R.string.failure_getting_stations), Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override public void onInfoWindowClick(Marker marker) {
        BikeStation station = GeneralHelper.binarySearchStation(marker.getTitle(), mStationsArray);
        if(station == null) return;

        PersistenceManager pm = PersistenceManager.getInstance(this);

        if (station.isFavourite) {
            pm.removeFavouriteStation(station.stationName);
        } else {
            pm.addFavouriteStation(station.stationName);
        }
        updateFavouriteStations(pm);
        mMapInfoWindowsAdapter.notifyDataSetChanged(mStationsArray);
        marker.showInfoWindow();
    }

    private void updateFavouriteStations(PersistenceManager pm){
        ArrayList favouriteStations = pm.getFavouriteStations();
        for (int i = 0; i < mStationsArray.size(); i++) {
            mStationsArray.get(i).isFavourite = favouriteStations.contains(mStationsArray.get(i).stationName);
        }
    }

    @Override
    public void onClick(View v) {

        final PersistenceManager persistenceManager = PersistenceManager.getInstance(MapsActivity.this);

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
                        hideNonFavouriteMarkers(persistenceManager);
                    } else {
                        Toast.makeText(MapsActivity.this, getString(R.string.toast_no_favourites), Toast.LENGTH_LONG).show();
                    }
                }

                setUpMap();
                break;
            case (R.id.btn_timer):

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                Intent notificationHandlerIntent = new Intent(this, NotificationHandler.class);
                PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(this, RESULT_OK, notificationHandlerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                if (!persistenceManager.getIsCountingDown()) {

                    int minutes = GeneralHelper.getMillisForDisplayedValue(persistenceManager.getTimerMinutes());
                    long alarmTime = SystemClock.elapsedRealtime()
                            + 1000 * 60 * minutes;

                    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, alarmPendingIntent);
                    persistenceManager.setIsCountingDown(true);

                    Toast.makeText(MapsActivity.this, getString(R.string.alarm_on), Toast.LENGTH_SHORT).show();
                }else{
                    alarmManager.cancel(alarmPendingIntent);

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
                    if(buses.get(i).equalsIgnoreCase(persistenceManager.getBusName())){
                        selectedBusPosition = i;
                    }
                }
                busListAdapter.setSelectedBus(selectedBusPosition);
                lv.setAdapter(busListAdapter);

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        view.setBackgroundColor(Color.GRAY);
                        persistenceManager.setBusName(busListAdapter.getItem(position));
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
                                ApiClient.getInstance().getBusSchedule(getBusScheduleCallback, persistenceManager.getBusName());
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
                                        GeneralHelper.getBusNumber(
                                                persistenceManager.getBusName()
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
    protected void onStop() {

        PersistenceManager persistenceManager = PersistenceManager.getInstance(MapsActivity.this);
        persistenceManager.saveData(this);

        //update widget if any
        int widgetId = persistenceManager.getWidgetId();
        String busName = persistenceManager.getBusName();
        if(widgetId > 0 && busName.length() >0) {
            RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.widget);

            remoteViews.setTextViewText(
                    R.id.tv_widget_bus,
                    GeneralHelper.getBusNumber(busName)
            );
            remoteViews.setTextViewText(R.id.tv_widget_times_titlu_1, capat1Title);
            remoteViews.setTextViewText(R.id.tv_widget_times_titlu_2, capat2Title);
            remoteViews.setTextViewText(
                    R.id.tv_widget_times_capat_1,
                    GeneralHelper.getPlecariAtThisHour(plecariCapat1)
            );
            remoteViews.setTextViewText(
                    R.id.tv_widget_times_capat_2,
                    GeneralHelper.getPlecariAtThisHour(plecariCapat2)
            );
            Intent intent = new Intent(this, MapsActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            remoteViews.setOnClickPendingIntent(R.id.widget_container, pendingIntent);
            AppWidgetManager.getInstance(MapsActivity.this).updateAppWidget(
                    widgetId,
                    remoteViews
            );

            trace("widget updated in onDestroy maps activity");
        }

        super.onStop();
    }

    public static void trace(String s){
        Log.d("traces", s);
    }
}
