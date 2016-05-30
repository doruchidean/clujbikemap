package com.doruchidean.clujbikemap.activities;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.doruchidean.clujbikemap.adapters.GoogleMapsInfoWindowAdapter;
import com.doruchidean.clujbikemap.fragments.ContactFragment;
import com.doruchidean.clujbikemap.fragments.HotColdMarginsFragment;
import com.doruchidean.clujbikemap.fragments.OverallStatsFragment;
import com.doruchidean.clujbikemap.fragments.TimerFragment;
import com.doruchidean.clujbikemap.fragments.WidgetUpdateIntervalFragment;
import com.doruchidean.clujbikemap.helpers.ApiClient;
import com.doruchidean.clujbikemap.adapters.BusListAdapter;
import com.doruchidean.clujbikemap.helpers.Callbacks;
import com.doruchidean.clujbikemap.helpers.Factory;
import com.doruchidean.clujbikemap.helpers.GeneralHelper;
import com.doruchidean.clujbikemap.helpers.NotificationHandler;
import com.doruchidean.clujbikemap.helpers.PersistenceManager;
import com.doruchidean.clujbikemap.R;
import com.doruchidean.clujbikemap.models.BikeStation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

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

	private final static String
		TAG_CONTACT = "contact.fragment",
		TAG_STATS = "overall.fragment",
		TAG_MARGINS = "margins.fragment",
		TAG_TIMER = "timer.fragment",
		TAG_WIDGET_INTERVAL = "widget.interval";
	public final static int
		PERMISSION_SET_COORDINATES_OF_USER_LOCATION = 0,
		PERMISSION_SETUP_MAP_IF_NEEDED = 1;

	private ArrayList<MarkerOptions> mapMarkers = new ArrayList<>();
	private ArrayList<BikeStation> mStationsArray = new ArrayList<>();
	private ArrayList<String> plecariCapat1 = new ArrayList<>();
	private ArrayList<String> plecariCapat2 = new ArrayList<>();
	private String capat1Title, capat2Title;

	private GoogleMap mMap;
	private GoogleApiClient mGoogleApiClient;
	private double userLatitude = 46.775627, userLongitude = 23.590935;

	private ImageButton btnShowFavourites;
	private TextView
		btnTimer,
		tvBusCapat1,
		tvBusCapat2,
		tvSelectedBus,
		drawerBtnMargins,
		drawerBtnContact,
		drawerBtnTimer,
		drawerBtnStatus,
		drawerBtnWidgetUpdate;
	private LinearLayout llTimesLeft, llTimesRight;
	private LinearLayout mBusBar;
	private GoogleMapsInfoWindowAdapter mMapInfoWindowsAdapter;
	private CountDownTimer mCountDownTimer;
	private ActionBarDrawerToggle drawerToggle;
	private ToggleButton drawerBtnShowBusBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//			WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_maps);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);


		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setHomeButtonEnabled(true);
			actionBar.setDisplayShowHomeEnabled(true);
		}

		setUpDrawer(toolbar);

		btnShowFavourites = (ImageButton) findViewById(R.id.btn_show_favourites);
		btnTimer = (TextView) findViewById(R.id.btn_timer);
		tvBusCapat1 = (TextView) findViewById(R.id.tv_bus_capat1);
		tvBusCapat2 = (TextView) findViewById(R.id.tv_bus_capat2);
		tvSelectedBus = (TextView) findViewById(R.id.btn_select_bus);
		llTimesLeft = (LinearLayout) findViewById(R.id.ll_times_left);
		llTimesRight = (LinearLayout) findViewById(R.id.ll_times_right);

		boolean showBusBar = PersistenceManager.getInstance(MapsActivity.this).getShowBusBar();
		mBusBar = (LinearLayout) findViewById(R.id.bus_bar);
		assert mBusBar != null;
		mBusBar.setVisibility(showBusBar ? View.VISIBLE : View.GONE);
		drawerBtnShowBusBar.setChecked(showBusBar);

		mMapInfoWindowsAdapter = new GoogleMapsInfoWindowAdapter(MapsActivity.this, mStationsArray);

		buildGoogleApiClient();


		//testing

		ApiClient.getInstance().getAdDetails(new Callback() {
			@Override public void onFailure(Call call, IOException e) {
				Log.e("traces", e.getMessage());
			}

			@Override public void onResponse(Call call, Response response) throws IOException {
				try {
					JSONObject adDetails = new JSONObject(response.body().string());
					final String adImage = adDetails.getString("ad_image");
					final String adWebpage = adDetails.getString("ad_webpage");
					MapsActivity.this.runOnUiThread(new Runnable() {
						@Override public void run() {
							setUpAd(adImage, adWebpage);
						}
					});
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void setUpAd(String imageUrl, final String webPageUrl){
		ImageView ivAd = (ImageView) findViewById(R.id.iv_ad);

		if (ivAd == null) return;

		Picasso.with(this)
			.load(imageUrl)
			.into(ivAd);

		ivAd.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(webPageUrl));
				startActivity(intent);
			}
		});
	}

	@Override protected void onPostCreate(@Nullable Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerToggle.syncState();
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshPage();
		mGoogleApiClient.connect();
	}

	@Override protected void onPause() {
		super.onPause();
		mGoogleApiClient.disconnect();
	}

	private void setUpDrawer(Toolbar toolbar){
		DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerToggle = new ActionBarDrawerToggle(
			this, drawerLayout, toolbar, R.string.open_drawer_content_desc, R.string.close_drawer_content_desc);
		drawerToggle.setDrawerIndicatorEnabled(true);
		assert drawerLayout != null;
		drawerLayout.addDrawerListener(drawerToggle); //further testing: assert

		drawerBtnMargins = (TextView) drawerLayout.findViewById(R.id.drawer_btn_margins);
		drawerBtnContact = (TextView) drawerLayout.findViewById(R.id.drawer_btn_contact);
		drawerBtnStatus = (TextView) drawerLayout.findViewById(R.id.drawer_btn_overall_stats);
		drawerBtnTimer = (TextView) drawerLayout.findViewById(R.id.drawer_btn_timer_limit);
		drawerBtnWidgetUpdate = (TextView) drawerLayout.findViewById(R.id.drawer_btn_widget_refresh_interval);
		drawerBtnShowBusBar = (ToggleButton) drawerLayout.findViewById(R.id.show_bus_bar);
		drawerBtnShowBusBar.setOnCheckedChangeListener(onShowBusBarChanged);

		getSupportFragmentManager()
			.beginTransaction()
			.add(R.id.drawer_fragment_placeholder, new ContactFragment(), TAG_CONTACT)
			.commit();
		setDrawerButtonsState(R.id.drawer_btn_contact);
	}

	private void refreshPage() {
		if (!hasGooglePlayServicesAvailable()) return;

		if (!hasNetworkConnection()) {
			askForInternetConnection();
			return;
		}

		ApiClient.getInstance().getStations(getStationsCallback);

		PersistenceManager pm = PersistenceManager.getInstance(MapsActivity.this);
		if (pm.getBusName().length() > 0 && pm.getShowBusBar()) {
			ApiClient.getInstance().getBusSchedule(getBusScheduleCallback, pm.getBusName());
		}

		setUpMapIfNeeded();

		refreshUIButtons();
	}

	private Callback getStationsCallback = new Callback() {
		@Override public void onFailure(Call call, IOException e) {
			MapsActivity.this.runOnUiThread(new Runnable() {
				@Override public void run() {
					Toast.makeText(MapsActivity.this, getString(R.string.failure_getting_stations), Toast.LENGTH_LONG).show();
				}
			});
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

	private void updateMarkersUI(JSONObject jsonResponse) {
		mStationsArray = Factory.getInstance().factorizeResponse(jsonResponse);
		Collections.sort(MapsActivity.this.mStationsArray);
		Toast.makeText(MapsActivity.this, getString(R.string.toast_up_to_date), Toast.LENGTH_LONG).show();
		mMapInfoWindowsAdapter.notifyDataSetChanged(mStationsArray);
		setUpMap();

		updateOverallStats();
	}

	private void updateOverallStats() {
		int allBikes = 0;
		int allEmptySpots = 0;
		int maxNrOfBikes = 0;

		PersistenceManager pm = PersistenceManager.getInstance(this);

		for (BikeStation s : mStationsArray) {
			allBikes += s.occupiedSpots;
			allEmptySpots += s.emptySpots;
			maxNrOfBikes += s.maximumNumberOfBikes;
		}

		pm.setOverallStats(this, allBikes, allEmptySpots, maxNrOfBikes);
	}

	private void updateBusBarUI(byte[] bytes) {
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

	private void refreshUIButtons() {
		PersistenceManager pm = PersistenceManager.getInstance(MapsActivity.this);
		btnShowFavourites.setBackgroundResource(pm.getShowFavouritesOnly() ?
			R.drawable.ic_favourite : R.drawable.ic_favourites_pressed);

		if (pm.getIsCountingDown()) {
			btnTimer.setBackgroundResource(R.drawable.ic_timer_pressed);
		} else {
			btnTimer.setBackgroundResource(R.drawable.ic_timer);
			btnTimer.setText("");
		}
	}

	private void askForInternetConnection() {

		AlertDialog dialog;
		AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
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

		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Network[] networks = cm.getAllNetworks();
			NetworkInfo networkInfo;
			for (Network mNetwork : networks) {
				networkInfo = cm.getNetworkInfo(mNetwork);
				if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
					return true;
				}
			}
		} else {
			if (cm != null) {
				//noinspection deprecation
				NetworkInfo[] info = cm.getAllNetworkInfo();
				if (info != null) {
					for (NetworkInfo anInfo : info) {
						if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	private boolean hasGooglePlayServicesAvailable() {
		GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
		int result = googleAPI.isGooglePlayServicesAvailable(this);
		return (result == ConnectionResult.SUCCESS);
	}

	private void setCoordinatesOfUserLocation() {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (ActivityCompat.checkSelfPermission(
				this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(
					this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

				requestPermissions(new String[]{
					Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
					PERMISSION_SET_COORDINATES_OF_USER_LOCATION
				);
				return;
			}
		}
		Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

		if (lastLocation != null) {
			//success getting userLatitude and userLongitude
			userLatitude = lastLocation.getLatitude();
			userLongitude = lastLocation.getLongitude();
		}

		if(mMap != null) mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLatitude, userLongitude), 16));
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
			((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
				.getMapAsync(new OnMapReadyCallback() {
					@Override public void onMapReady(GoogleMap googleMap) {
						mMap = googleMap;

						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
							if (ActivityCompat.checkSelfPermission(
								MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
								ActivityCompat.checkSelfPermission(
									MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

								requestPermissions(new String[]{
									Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
									PERMISSION_SETUP_MAP_IF_NEEDED
								);
								return;
							}
						}

						mMap.setMyLocationEnabled(true);
						mMap.setOnInfoWindowClickListener(MapsActivity.this);
						mMap.setInfoWindowAdapter(mMapInfoWindowsAdapter);
						mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLatitude, userLongitude), 16));
						mMap.setOnMarkerClickListener(onMarkerClickListener);

						setUpMap();
					}
				});
		}
	}

	@Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode){
			case PERMISSION_SET_COORDINATES_OF_USER_LOCATION:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					setCoordinatesOfUserLocation();
				} else {
					//todo handle case of no permission granted
					Toast.makeText(MapsActivity.this, "No permission granted", Toast.LENGTH_SHORT).show();
				}
				break;
			case PERMISSION_SETUP_MAP_IF_NEEDED:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					setUpMapIfNeeded();
				} else {
					//todo handle case
					Toast.makeText(MapsActivity.this, "No permission granted", Toast.LENGTH_SHORT).show();
				}
				break;
		}

		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
									if (marker.isInfoWindowShown()) {
										BikeStation station = GeneralHelper.binarySearchStation(marker.getTitle(), mStationsArray);
										mStationsArray.get(mStationsArray.indexOf(station)).distanceMinutes = distance[1];
										mStationsArray.get(mStationsArray.indexOf(station)).distanceSteps= distance[0];

										marker.showInfoWindow();
									}
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

		if(mMap == null) return;

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

			AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this, R.style.MyDialogTheme);
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

		if(drawerToggle.onOptionsItemSelected(item)) return true;

		int id = item.getItemId();

		switch (id) {
			case R.id.btn_refresh:
				refreshPage();
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	private CompoundButton.OnCheckedChangeListener onShowBusBarChanged = new CompoundButton.OnCheckedChangeListener() {
		@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			PersistenceManager pm = PersistenceManager.getInstance(MapsActivity.this);
			pm.setShowBusBar(isChecked);

			mBusBar.setVisibility(isChecked ? View.VISIBLE : View.GONE);
		}
	};

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

	}

	private void setTextsInBusBar(ArrayList<String> minTexts, LinearLayout parent){

		parent.removeAllViews();

		int padding2dp = (int) TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP,
			2,
			getResources().getDisplayMetrics()
		);

		TextView tvSubtitle = new TextView(MapsActivity.this);
		tvSubtitle.setSingleLine();
		tvSubtitle.setTextColor(ContextCompat.getColor(this, R.color.color_text_main));
		if (minTexts.size() > 0) {
			tvSubtitle.setText(getString(R.string.bus_bar_subtitle_to_be_filled));
			tvSubtitle.setPadding(0,0,padding2dp,0);
		} else {
			tvSubtitle.setText(String.format(getString(R.string.bus_bar_subtitle_full), GeneralHelper.busLeavingMaxOffset));
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
				tvBusCapat2.setText("");
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

					//test getMinutesForDisplayed is correct method
					int minutes = GeneralHelper.getMinutesForTimerDisplayedValue(persistenceManager.getTimerValueIndex());
					long alarmTime = SystemClock.elapsedRealtime()
						+ 1000 * 60 * minutes;

					alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, alarmPendingIntent);
					persistenceManager.setIsCountingDown(true);

					Toast.makeText(MapsActivity.this, String.format(getString(R.string.alarm_on), minutes), Toast.LENGTH_SHORT).show();

					btnTimer.setText(String.valueOf(minutes));
					mCountDownTimer = new CountDownTimer(minutes*60*1000, 1000) {
						@Override public void onTick(long millisUntilFinished) {
							trace("onTick: " + millisUntilFinished/(1000));
							btnTimer.setText(String.format(
								"%s:%s",
								millisUntilFinished/(60*1000),
								(millisUntilFinished/1000)%60));
						}

						@Override public void onFinish() {
							trace("onFinish");
							refreshUIButtons();
						}
					};
					mCountDownTimer.start();
				}else{
					alarmManager.cancel(alarmPendingIntent);
					persistenceManager.setIsCountingDown(false);
					if(mCountDownTimer != null) mCountDownTimer.cancel();
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
						persistenceManager.setBusName(busListAdapter.getItem(position));
						busListAdapter.setSelectedBus(position);
						busListAdapter.notifyDataSetChanged();
					}
				});

				final AlertDialog dialog;
				AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this, R.style.MyDialogTheme);
					builder.setView(busesContainer)
						.setTitle(getString(R.string.dialog_add_buses_title))
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
				dialog = builder.create();
				dialog.show();
				break;
			case(R.id.btn_full_bus_schedule):

				if(persistenceManager.getBusName().length() == 0) break;

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

				AlertDialog dialog1 = new AlertDialog.Builder(MapsActivity.this, R.style.MyDialogTheme)
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
					.create();
				dialog1.show();
				dialog1.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
				break;
			case R.id.drawer_btn_margins:
				HotColdMarginsFragment marginsFragment = HotColdMarginsFragment.newInstance(this);
				getSupportFragmentManager()
					.beginTransaction()
					.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
					.replace(R.id.drawer_fragment_placeholder, marginsFragment, TAG_MARGINS)
					.commit();
				setDrawerButtonsState(R.id.drawer_btn_margins);
				break;
			case R.id.drawer_btn_contact:
				getSupportFragmentManager()
					.beginTransaction()
					.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
					.replace(R.id.drawer_fragment_placeholder, new ContactFragment(), TAG_CONTACT)
					.commit();
				setDrawerButtonsState(R.id.drawer_btn_contact);
				break;
			case R.id.drawer_btn_overall_stats:
				getSupportFragmentManager()
					.beginTransaction()
					.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
					.replace(R.id.drawer_fragment_placeholder, new OverallStatsFragment(), TAG_STATS)
					.commit();
				setDrawerButtonsState(R.id.drawer_btn_overall_stats);
				break;
			case R.id.drawer_btn_timer_limit:
				getSupportFragmentManager()
					.beginTransaction()
					.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
					.replace(R.id.drawer_fragment_placeholder, new TimerFragment(), TAG_TIMER)
					.commit();
				setDrawerButtonsState(R.id.drawer_btn_timer_limit);
				break;
			case R.id.drawer_btn_widget_refresh_interval:
				getSupportFragmentManager()
					.beginTransaction()
					.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
					.replace(R.id.drawer_fragment_placeholder, new WidgetUpdateIntervalFragment(), TAG_WIDGET_INTERVAL)
					.commit();
				setDrawerButtonsState(R.id.drawer_btn_widget_refresh_interval);
				break;
		}

		refreshUIButtons();
	}

	private void setDrawerButtonsState(int buttonId){
		drawerBtnMargins.setBackgroundColor(ContextCompat.getColor(this, R.color.drawer_button_normal));
		drawerBtnContact.setBackgroundColor(ContextCompat.getColor(this, R.color.drawer_button_normal));
		drawerBtnStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.drawer_button_normal));
		drawerBtnTimer.setBackgroundColor(ContextCompat.getColor(this, R.color.drawer_button_normal));
		drawerBtnWidgetUpdate.setBackgroundColor(ContextCompat.getColor(this, R.color.drawer_button_normal));

		switch (buttonId){
			case R.id.drawer_btn_margins:
				drawerBtnMargins.setBackgroundColor(ContextCompat.getColor(this, R.color.drawer_button_selected));
				break;
			case R.id.drawer_btn_contact:
				drawerBtnContact.setBackgroundColor(ContextCompat.getColor(this, R.color.drawer_button_selected));
				break;
			case R.id.drawer_btn_overall_stats:
				drawerBtnStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.drawer_button_selected));
				break;
			case R.id.drawer_btn_timer_limit:
				drawerBtnTimer.setBackgroundColor(ContextCompat.getColor(this, R.color.drawer_button_selected));
				break;
			case R.id.drawer_btn_widget_refresh_interval:
				drawerBtnWidgetUpdate.setBackgroundColor(ContextCompat.getColor(this, R.color.drawer_button_selected));
				break;
		}
	}

	int backPressedCount = 0;

	@Override
	public void onBackPressed() {

		backPressedCount ++;
		if(backPressedCount == 2){
			if (PersistenceManager.getInstance(MapsActivity.this).getIsCountingDown()) {
				Intent i = new Intent(Intent.ACTION_MAIN);
				i.addCategory(Intent.CATEGORY_HOME);
				startActivity(i);
			} else {
				super.onBackPressed();
			}
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
