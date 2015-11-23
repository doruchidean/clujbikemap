package com.doruchidean.clujbikemap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
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

public class MapsActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ApiCallbacks {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ArrayList<StationsModel> mStationsArray = new ArrayList<>();
    GoogleApiClient mGoogleApiClient;
    private double userLatitude = 46.775627, userLongitude = 23.590935;

    private int mColdLimit, mHotLimit;
    private final String COLD_LIMIT = "coldlimit", HOT_LIMIT = "hotlimit", SHARED_PREFS_NAME = "androidCjBike";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_maps);

        mColdLimit = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE).getInt(COLD_LIMIT, 3);
        mHotLimit = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE).getInt(HOT_LIMIT, 3);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!hasGooglePlayServicesAvailable()) return;

        if(!hasNetworkConnection()){
            askForInternetConnection();
            return;
        }

        HttpHandler.getInstance().getStations(MapsActivity.this);

        setUpMapIfNeeded();

        buildGoogleApiClient();

    }


    private void askForInternetConnection(){

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

    private boolean hasGooglePlayServicesAvailable()
    {
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS;

    }

    private void setCoordinatesOfUserLocation(){

        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(lastLocation != null){
            //success getting userLatitude and userLongitude
            userLatitude = lastLocation.getLatitude();
            userLongitude = lastLocation.getLongitude();
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLatitude, userLongitude), 16));
    }

    protected synchronized void buildGoogleApiClient() {

        if(mGoogleApiClient == null) {
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

    private void setUpMap() {

        MarkerOptions markerOptions;

        mMap.clear();

        for(int i = 0; i < mStationsArray.size(); i++) {

            StationsModel station = mStationsArray.get(i);

            markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(station.latitude, station.longitude));
            markerOptions.title(station.stationName);

            if(station.ocuppiedSpots == 0){
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.station_offline));
            }else if(station.ocuppiedSpots < mColdLimit){
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.station_underpopulated));
            }else if(station.ocuppiedSpots < station.maximumNumberOfBikes - mHotLimit){
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.station_online));
            }else{
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.station_overpopulated));
            }

            mMap.addMarker(markerOptions);
        }

        mMap.setMyLocationEnabled(true);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                for (int i = 0; i < mStationsArray.size(); i++) {
                    if (marker.getTitle().equals(mStationsArray.get(i).stationName)) {

                        View v = View.inflate(MapsActivity.this, R.layout.station_dialog, null);
                        TextView tvName = (TextView) v.findViewById(R.id.tvDialogName);
                        TextView tvAddress = (TextView) v.findViewById(R.id.tvDialogAddress);
                        TextView tvEmptySpots = (TextView) v.findViewById(R.id.tvEmptySpots);
                        TextView tvOccupiedSpots = (TextView) v.findViewById(R.id.tvOcuppiedSpots);
                        TextView tvStatus = (TextView) v.findViewById(R.id.tvStatus);

                        StationsModel s = mStationsArray.get(i);

                        Log.v("traces", " name:" + s.stationName +
                                " address:" + s.address +
                                " empty spots:" + s.emptySpots +
                                " occupied spots:" + s.ocuppiedSpots +
                                " status type:" + s.statusType +
                                " custom is valid:" + s.customIsValid +
                                " is valid:" + s.isValid +
                                " station status:" + s.stationStatus +
                                " last sync date:" + s.lastSyncDate+
                                " id:"+s.id+
                                " id status:"+s.idStatus+
                                " maximum nr of bikes: "+ s.maximumNumberOfBikes);

                        tvName.setText(s.stationName);
                        tvAddress.setText(s.address);
                        tvEmptySpots.setText(String.format("Locuri libere: %s", s.emptySpots));
                        tvOccupiedSpots.setText(String.format("Biciclete disponibile: %s", s.ocuppiedSpots));
                        tvStatus.setText(String.format("Status: %s", s.statusType));

                        AlertDialog dialog;
                        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                                .setCancelable(true)
                                .setView(v);
                        dialog = builder.create();
                        dialog.show();
                        break;
                    }
                }

                return false;
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        //        check if location services is turned on
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        if(!gps_enabled){
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

        switch (id){
            case R.id.refresh_rate:
                HttpHandler.getInstance().getStations(MapsActivity.this);
                break;
            case R.id.hot_cold_margins:
                showMarginsDialog();
                break;
            case R.id.contact:
                showContactDialog();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showMarginsDialog(){
        View dialogContainer = View.inflate(MapsActivity.this, R.layout.margings_dialog, null);

        final TextView tvColdMargin = (TextView) dialogContainer.findViewById(R.id.tv_dialog_margins_cold);
        final TextView tvHotMargin = (TextView) dialogContainer.findViewById(R.id.tv_dialog_margins_hot);

        tvColdMargin.setText(String.format("%s %s", getString(R.string.dialog_margins_cold), mColdLimit));
        tvHotMargin.setText(String.format("%s %s", getString(R.string.dialog_margins_hot), mHotLimit));

        final byte hypotheticRange = 30;

        RangeBar rangeBar = (RangeBar) dialogContainer.findViewById(R.id.rangebar);
        rangeBar.setTickCount(hypotheticRange+1); //todo test margins working correctly
        rangeBar.setThumbIndices(mColdLimit, hypotheticRange - mHotLimit);
        rangeBar.setConnectingLineColor(Color.GREEN);
        rangeBar.setThumbColorNormal(Color.GREEN);
        rangeBar.setThumbColorPressed(Color.RED);
        rangeBar.setBackgroundResource(R.drawable.background_rangebar);
        rangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onIndexChangeListener(RangeBar rangeBar, int i, int i1) {
                mColdLimit = i;
                mHotLimit = hypotheticRange - i1;

                tvColdMargin.setText(String.format("%s %s", getString(R.string.dialog_margins_cold), mColdLimit));
                tvHotMargin.setText(String.format("%s %s", getString(R.string.dialog_margins_hot), mHotLimit));
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

                        SharedPreferences.Editor e = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE).edit();
                        e.putInt(COLD_LIMIT, mColdLimit);
                        e.putInt(HOT_LIMIT, mHotLimit);
                        e.apply();

                        setUpMap();

                        dialog.dismiss();
                    }
                });
        dialog = builder.create();
        dialog.show();

    }

    private void showContactDialog(){

        View dialogContainer = View.inflate(MapsActivity.this, R.layout.contact_dialog, null);

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
        Toast.makeText(MapsActivity.this, getString(R.string.toast_up_to_date), Toast.LENGTH_LONG).show();
        setUpMap();
    }

    @Override
    public void onApiCallFail(String error) {
        Toast.makeText(MapsActivity.this, getString(R.string.failure_getting_stations) + error, Toast.LENGTH_LONG).show();
    }

}
