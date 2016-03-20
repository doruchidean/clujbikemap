package com.doruchidean.clujbikemap.activities;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.doruchidean.clujbikemap.helpers.ApiClient;
import com.doruchidean.clujbikemap.adapters.BusListAdapter;
import com.doruchidean.clujbikemap.helpers.Callbacks;
import com.doruchidean.clujbikemap.helpers.Factory;
import com.doruchidean.clujbikemap.helpers.NotificationHandler;
import com.doruchidean.clujbikemap.helpers.PersistenceManager;
import com.doruchidean.clujbikemap.R;
import com.doruchidean.clujbikemap.helpers.SettingsDialogs;
import com.doruchidean.clujbikemap.models.BikeStation;
import com.doruchidean.clujbikemap.models.BusStation;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        Callbacks.ApiCallbacks,
        Callbacks.SettingsDialogsCallback,
        View.OnClickListener {

    private final String TAB_ONE = "Tab 1", TAB_TWO = "Tab 2";

    private ArrayList<MarkerOptions> mBusStationMarkers = new ArrayList<>();
    private ArrayList<BusStation> mBusStationsArray = new ArrayList<>();
    private ArrayList<MarkerOptions> bikeStationMarkers = new ArrayList<>();
    private ArrayList<BikeStation> mBikeStationsArray = new ArrayList<>();
    private ArrayList<String> plecariCapat1 = new ArrayList<>(), plecariCapat2 = new ArrayList<>();

    private String capat1Title, capat2Title;
    private double userLatitude = 46.775627, userLongitude = 23.590935;
    private GoogleMap mBikeMap, mBusMap;

    private GoogleApiClient mGoogleApiClient;
    private ImageButton btnShowFavourites, btnTimer;
    private TextView tvBusCapat1, tvBusCapat2, tvSelectedBus;
    private LinearLayout llTimesLeft, llTimesRight;
    private LinearLayout mBusBar;
    private TabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_maps);
        grabViews();

        mTabHost = (TabHost) findViewById(R.id.tab_host);
        mTabHost.setup();
        mTabHost.setOnTabChangedListener(onTabChangedListener);

        TabHost.TabSpec tab1 = mTabHost.newTabSpec(TAB_ONE);
        tab1.setContent(R.id.bike_map_container);
        tab1.setIndicator("Bike Map");
        mTabHost.addTab(tab1);

        TabHost.TabSpec tab2 = mTabHost.newTabSpec(TAB_TWO);
        tab2.setContent(R.id.bus_map_container);
        tab2.setIndicator("Bus Map");
        mTabHost.addTab(tab2);

        mBusStationsArray = Factory.getInstance().getBusStations(MapsActivity.this);

        searchAdress("str Tulcea nr 20");

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

        PersistenceManager pm = PersistenceManager.getInstance(MapsActivity.this);
        if(pm.getBusName().length() > 0){
            ApiClient.getInstance().getBusSchedule(MapsActivity.this, pm.getBusName());
        }

        setUpMapIfNeeded(mTabHost.getCurrentTabTag());

        buildGoogleApiClient();

        refreshUIButtons();
    }

    private void grabViews() {
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
                        View.INVISIBLE
        );
    }

    private TabHost.OnTabChangeListener onTabChangedListener = new TabHost.OnTabChangeListener() {
        @Override
        public void onTabChanged(String tabTag) {
            setUpMapIfNeeded(tabTag);
        }
    };

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

        mBikeMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLatitude, userLongitude), 16));
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

    private void setUpMapIfNeeded(String tabTag) {

        switch (tabTag){
            case (TAB_ONE):

                if (mBikeMap == null) {
                    mBikeMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.bike_map))
                            .getMap();
                    if (mBikeMap != null) {
                        mBikeMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLatitude, userLongitude), 16));
                        setMapMarkers();
                    }
                }

                break;
            case (TAB_TWO):

                if (mBusMap == null) {
                    mBusMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.bus_map))
                            .getMap();
                    if (mBusMap!= null) {
                        mBusMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLatitude, userLongitude), 16));
                        setBusMapMarkers("101"); //todo replace with selected bus line
                    }
                }

                break;
        }

    }

    private void createMarkers(){
        MarkerOptions markerOptions;
        PersistenceManager pm = PersistenceManager.getInstance(MapsActivity.this);

        //set the bike stations markers
        for (int i = 0; i < mBikeStationsArray.size(); i++) {

            BikeStation station = mBikeStationsArray.get(i);

            markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(station.latitude, station.longitude));
            markerOptions.title(station.stationName);

            if (station.ocuppiedSpots == 0) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.station_offline));
            } else if (station.ocuppiedSpots < pm.getColdLimit()) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.station_underpopulated));
            } else if (station.ocuppiedSpots < station.maximumNumberOfBikes - pm.getHotLimit()) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.station_online));
            } else {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.station_overpopulated));
            }

            bikeStationMarkers.add(markerOptions);

        }

        if(pm.getShowFavouritesOnly()){
            hideNonFavouriteMarkers(pm);
        }

        //set the bus stations markers
        for(BusStation busStation : mBusStationsArray){

            markerOptions = new MarkerOptions();
            markerOptions.position(busStation.getLatLngLocation());
            markerOptions.title(busStation.getName());

            mBusStationMarkers.add(markerOptions);
        }
    }

    @Override
    public void setMapMarkers() {

        mBikeMap.clear();

        for(MarkerOptions m : bikeStationMarkers){
            mBikeMap.addMarker(m);
        }

        mBikeMap.setMyLocationEnabled(true);

        mBikeMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                onBikeMarkerClickFunction(marker);

                return false;
            }
        });
    }

    private void searchAdress(String address){
        Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
        try{
            List<Address> addresses = geocoder.getFromLocationName(address, 5);
            if(addresses.size()>0){
//                Double latitude = addresses.get(0).getLatitude();
//                Double longitude = addresses.get(0).getLongitude();

            }

            for(Address address1 : addresses){
                trace("featureName: " + address1.getFeatureName() +
                        " latitude " + address1.getLatitude() +
                        " longitude " + address1.getLongitude() +
                                " locality " + address1.getLocality()
                );
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void setBusMapMarkers(String busLine) {

        mBusMap.clear();
        BusStation busStation;

        for(int i = 0; i < mBusStationsArray.size(); i++) {
            busStation = mBusStationsArray.get(i);

            if (busStation.getLinii().contains(busLine)) {
                mBusMap.addMarker(mBusStationMarkers.get(i));
            }
        }
        mBusMap.setMyLocationEnabled(true);
    }

    private void onBusMarkerClickFunction(Marker marker) {
        //todo handle on click bus station marker event
    }

    private BikeStation binarySearchBikeStation(String markerTitleToFind){

        int lowEnd = 0;
        int highEnd = mBikeStationsArray.size()-1;
        int middle, compare;

        while (lowEnd <= highEnd){
            middle = (lowEnd+highEnd)/2;

            compare = markerTitleToFind.compareToIgnoreCase(mBikeStationsArray.get(middle).stationName);

            if(compare < 0){
                highEnd = middle-1;
            }else if(compare > 0){
                lowEnd = middle +1;
            }else{
               return mBikeStationsArray.get(middle);
            }

        }

        return null;
    }

    private void onBikeMarkerClickFunction(final Marker marker){

        final BikeStation station = binarySearchBikeStation(marker.getTitle());
        final PersistenceManager pm = PersistenceManager.getInstance(MapsActivity.this);

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
                    pm.removeFavouriteStation(station.stationName);
                    marker.setVisible(false);
                } else {
                    v.setBackgroundResource(R.drawable.ic_favourite);
                    station.isFavourite = true;
                    pm.addFavouriteStation(station.stationName);
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

    private void hideNonFavouriteMarkers(PersistenceManager pm){
        BikeStation matchingStation;
        for(MarkerOptions m : bikeStationMarkers){
            matchingStation = binarySearchBikeStation(m.getTitle());
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
                ApiClient.getInstance().getStations(MapsActivity.this);
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

        mBusBar.setVisibility(!item.isChecked() ? View.VISIBLE : View.INVISIBLE);

        item.setChecked(!item.isChecked());
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onSuccessBikeStations(ArrayList<BikeStation> stationsArray) {
        this.mBikeStationsArray = stationsArray;
        Collections.sort(this.mBikeStationsArray);

        Toast.makeText(MapsActivity.this, getString(R.string.toast_up_to_date), Toast.LENGTH_LONG).show();
        createMarkers();
        setMapMarkers();
        SettingsDialogs.getInstance().updateOverallStats(MapsActivity.this, mBikeStationsArray);
    }

    @Override
    public void onSuccessBusTimes(byte[] binaryData) {

        HashMap<String, ArrayList<String>> leavingTimes = Factory.getInstance().readCsv(binaryData);
        PersistenceManager pm = PersistenceManager.getInstance(MapsActivity.this);

        pm.setBusSchedule(this, binaryData);

        setTextsInBusBar(leavingTimes.get(Factory.MINUTES_CAPAT_1), llTimesLeft);
        setTextsInBusBar(leavingTimes.get(Factory.MINUTES_CAPAT_2), llTimesRight);
        tvSelectedBus.setText(Factory.getInstance().getBusNumber(pm.getBusName()));

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

        final PersistenceManager persistenceManager = PersistenceManager.getInstance(MapsActivity.this);

        switch(v.getId()){
            case(R.id.btn_show_favourites):

                if(persistenceManager.getShowFavouritesOnly()){

                    persistenceManager.setShowFavouritesOnly(false);
                    for(MarkerOptions m : bikeStationMarkers){
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

                setMapMarkers();
                break;
            case (R.id.btn_timer):

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                Intent notificationHandlerIntent = new Intent(this, NotificationHandler.class);
                PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(this, RESULT_OK, notificationHandlerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                if (!persistenceManager.getIsCountingDown()) {

                    int minutes = Factory.getInstance().getMillisForDisplayedValue(persistenceManager.getTimerMinutes());
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
                                ApiClient.getInstance().getBusSchedule(MapsActivity.this, persistenceManager.getBusName());
                                tvSelectedBus.setText(Factory.getInstance().getBusNumber(persistenceManager.getBusName()));
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
    protected void onDestroy() {

        PersistenceManager persistenceManager = PersistenceManager.getInstance(MapsActivity.this);
        persistenceManager.saveData(this);

        //update widget if any
        int widgetId = persistenceManager.getWidgetId();
        String busName = persistenceManager.getBusName();
        if(widgetId > 0 && busName.length() >0) {
            RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.widget);

            remoteViews.setTextViewText(
                    R.id.tv_widget_bus,
                    Factory.getInstance().getBusNumber(busName)
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
            Intent intent = new Intent(this, MapsActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            remoteViews.setOnClickPendingIntent(R.id.widget_container, pendingIntent);
            AppWidgetManager.getInstance(MapsActivity.this).updateAppWidget(
                    widgetId,
                    remoteViews
            );

            trace("widget updated in onDestroy maps activity");
        }
        super.onDestroy();
    }

    public static void trace(String s){
        Log.d("traces", s);
    }

}
