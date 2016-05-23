package com.fixmytrip.train.maps;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.fixmytrip.train.MainActivity;
import com.fixmytrip.train.R;
import com.fixmytrip.train.location.LocationHandler;
import com.fixmytrip.train.location.PathDistanceHelper;
import com.fixmytrip.train.trains.Train;
import com.fixmytrip.train.trains.TrainStation;
import com.fixmytrip.train.trains.TrainSystem;
import com.fixmytrip.train.utils.Constants;
import com.fixmytrip.train.utils.FloatingActionButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapFragment extends Fragment implements
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.InfoWindowAdapter,
        GoogleMap.OnMarkerClickListener,
        View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener
{
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION=1;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private TrainSystem trainSystem;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    TrainStation homeStation;
    TrainStation awayStation;

    int mapId;
    FloatingActionButton mFab;
    private Geofence nearestStationGeofence;

    private List<Train> trains;
    Handler mHandler;
    Runnable mHandlerTask;
    private final static String TAG = "MapFragment";
    UpdateStationListener mCallback;
    boolean isNotificationIntent=false;
    Train trainFromNotification;
    private boolean isLocationEnabled=true;

    // The container Activity must implement this interface so the frag can deliver messages
    public interface UpdateStationListener {
        /** Called by HeadlinesFragment when a list item is selected */
        public void onStationSelected(TrainStation trainStation, LatLng currentLocation, List<Train> allTrains);
        public void onTrainSelected(Train train);
        public void updateInformation();
    }

    public MapFragment() {
        // Empty constructor required for fragment subclasses
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_maps_activity, container, false);
         mapId= getArguments().getInt(Constants.systemID);
        int trainNumber = getArguments().getInt(Constants.MAIN_ACTIVITY_TRAIN_EXTRA,-1);
        Train trainFromArgs = MainActivity.trainSystems.get(mapId).getTrainById(trainNumber);
        if(trainFromArgs != null)
        {
            isNotificationIntent = true;
            trainFromNotification = trainFromArgs;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity().getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        ImageButton btn_home = (ImageButton) getActivity().findViewById(R.id.btn_map_home);
        //mFab = (FloatingActionButton) getActivity().findViewById(R.id.fabbutton);
        btn_home.setOnClickListener(this);

    }

    @Override
    public void onResume()
    {
        super.onResume();
        mGoogleApiClient.connect();
        if (isLocationEnabled(this.getActivity()) == true) {
            setUpMapIfNeeded();
            if (MainActivity.trainSystems.get(mapId).trains.isEmpty() == false) {
                trains = MainActivity.trainSystems.get(mapId).trains;
            }
        }
        mHandler = new Handler();
        mHandlerTask = new Runnable()
        {
            @Override
            public void run() {
                Log.d(TAG,"Updating Trains");
                if(trains!=null)
                {
                    for (Train t : trains) {
                        t.updateLocation(mMap);
                    }
                }

                mCallback.updateInformation();
                mHandler.postDelayed(mHandlerTask, Constants.UPDATE_INTERVAL);
            }
        };
        mHandler.postDelayed(mHandlerTask, 1000);
    }

    @Override
    public void onPause()
    {
        // Disconnecting the client invalidates it.
        stopRepeatingTask();
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (UpdateStationListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement UpdateStationListener");
        }
    }



    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link com.google.android.gms.maps.SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            getFragmentManager().findFragmentById(R.id.map);
            mMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap();
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setInfoWindowAdapter(this);
            mMap.setOnMarkerClickListener(this);
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }

        }
    }

    private void setUpMap() {
        trainSystem = MainActivity.trainSystems.get(mapId);
        trainSystem.mMap=mMap;
        trainSystem.AddAllStations();
       if(mapId==Constants.trainSystems.AllAboardFlorida.ordinal())
       {
           trainSystem.generateMainPathFromPoints(Constants.aaf_points);
       }
       else
       {
           trainSystem.generateMainPath();
       }
        if(isNotificationIntent && trainFromNotification!=null && trainFromNotification.isTrainActive() && trainFromNotification.getCurrentLocation()!=null)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(trainFromNotification.getCurrentLocation(), (float) Constants.defaultUserZoom));
        else
            trainSystem.centerAndZoom();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Location mCurrentLocation= LocationServices.FusedLocationApi.getLastLocation( mGoogleApiClient);
        if(mCurrentLocation!=null) {
            getNearestStation(mCurrentLocation);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), (float) Constants.defaultUserZoom));
            return true;
        }
        if(!isLocationEnabled)
            startLocationUpdates();

        //TODO:PUT UP TOAST NOTIFICATION
        return false;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        //TODO: Need to set Layout for Each Station
        View InfoPopupLayout =getActivity().getLayoutInflater().inflate(R.layout.train_station_info_window, null);
        //mFab.hide(true);
        TextView t_lat=(TextView)InfoPopupLayout.findViewById(R.id.txtInfoWindowTitle);
        t_lat.setText(marker.getTitle());
        //InfoPopupLayout.setOnClickListener();
        //TextView t_lng=(TextView)InfoPopupLayout.findViewById(R.id.txtInfoWindowEventType);
        //t_lng.setText("World");
        return InfoPopupLayout;
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        //TODO: NEED WAY TO DIFFERENTIATE TRAINS FROM STATIONS

        if(marker.getTitle().matches(Constants.train_regex)) //is a train
        {
            for(Train train : trains)
            {
                if(train.name.equals(marker.getTitle()))
                {
                    mCallback.onTrainSelected(train);

                    break;
                }
            }
        }
        else //is a station
        {
            marker.getPosition();
            TrainStation selectedStation = null;
            Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation( mGoogleApiClient);
            for (TrainStation trainStation : trainSystem.stations) {
                if ((Math.abs(trainStation.lat - marker.getPosition().latitude) < .0001) && (Math.abs(trainStation.lng - marker.getPosition().longitude) < .0001)) {
                    selectedStation = trainStation;
                    break;
                }
            }
            if(selectedStation!=null) {
                if(mCurrentLocation == null)
                    mCallback.onStationSelected(selectedStation, null, trains);
                else
                    mCallback.onStationSelected(selectedStation, new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), trains);
            }
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(trainSystem.cameraLat, trainSystem.cameraLng), (float) trainSystem.zoom));
    }

    void stopRepeatingTask()
    {
        mHandler.removeCallbacks(mHandlerTask);
    }

    private void getNearestStation(Location mCurrentLocation)
    {
        double neareststraightLineDistance=Double.MAX_VALUE;
        TrainStation nearestStation=null;
        LatLng myCurrentLatLng= new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String homeStationStringPref = sharedPref.getString(getString(R.string.preference_home_station), null);
        String awayStationStringPref = sharedPref.getString(getString(R.string.preference_away_station), null);

        homeStation = trainSystem.getStationByName(homeStationStringPref);
        awayStation = trainSystem.getStationByName(awayStationStringPref);

        if(homeStation!=null && awayStation!=null)
        {
            double home_distance = getDistanceToTrainStation(homeStation, myCurrentLatLng);
            double away_distance = getDistanceToTrainStation(awayStation,myCurrentLatLng);

            if (home_distance <= away_distance) {
                neareststraightLineDistance = home_distance;
                nearestStation = homeStation;
            }
            else
            {
                neareststraightLineDistance = away_distance;
                nearestStation = awayStation;
            }
        }
        else {
            if (homeStation != null || awayStation != null) {
                //TODO: Pop up to have user enter error station preference
                Toast.makeText(getActivity(), "Please correct station preferences", Toast.LENGTH_SHORT);
            }

            for (TrainStation trainStation : trainSystem.stations) {
                double total_distance = getDistanceToTrainStation(trainStation, myCurrentLatLng);
                if (total_distance < neareststraightLineDistance) {
                    neareststraightLineDistance = total_distance;
                    nearestStation = trainStation;
                }
            }
        }

        mCallback.onStationSelected(nearestStation, myCurrentLatLng, trains);
        nearestStationGeofence = new Geofence.Builder()
            // Set the request ID of the geofence. This is a string to identify this
            // geofence.
            .setRequestId(nearestStation.name)

            .setCircularRegion(
                    nearestStation.lat,
                    nearestStation.lng,
                    Constants.GEOFENCE_STATION_RADIUS
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build();
    }


    @Override
    public void onConnected(Bundle bundle) {
        Location mCurrentLocation= LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mCurrentLocation!=null && !isNotificationIntent) {
            getNearestStation(mCurrentLocation);
        }
        if(isLocationEnabled)
            startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public boolean isLocationEnabled(Context context) {
        int locMode = Settings.Secure.LOCATION_MODE_OFF;
        String locProviders;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            try {
                locMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            }
            catch (Settings.SettingNotFoundException snfe) {
                snfe.printStackTrace();
            }

            return locMode != Settings.Secure.LOCATION_MODE_OFF;
        }
        else {
            locProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locProviders);

        }
    }

    protected void startLocationUpdates() {
        if (mLocationRequest == null)
        {
            createLocationRequest();
        }
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }

    }


    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        String mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        handleLocationUpdate();
    }

    private void handleLocationUpdate() {
        if(isNearTrackPath())
        {
            //TODO: Send information to server
        }
    }

    private boolean isNearTrackPath()
    {
        PathDistanceHelper pathDistanceHelper = new PathDistanceHelper();
        double distance = pathDistanceHelper.calculateDistance(trainSystem.routes.get(0),mCurrentLocation);
        return distance <= Constants.GEOFENCE_MAX_DISTANCE_TO_PATH;

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private double getDistanceToTrainStation(TrainStation trainStation, LatLng myCurrentLatLng)
    {
        double lat_dif = Math.abs(trainStation.lat - myCurrentLatLng.latitude);
        double lng_dif = Math.abs(trainStation.lng - myCurrentLatLng.longitude);
        return lat_dif * lat_dif + lng_dif * lng_dif;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isLocationEnabled = true;
                    updateLocationUI(true);
                    mGoogleApiClient.connect();

                } else {
                    isLocationEnabled = false;
                    updateLocationUI(false);

                }
                return;
            }
        }
    }

    public CameraUpdate getCurrentCameraLocation()
    {

        return CameraUpdateFactory.newLatLngZoom(mMap.getCameraPosition().target, mMap.getCameraPosition().zoom);
    }

    public void focusMap(CameraUpdate cameraUpdate)
    {
        mMap.animateCamera(cameraUpdate);
    }

    public void focusMap(LatLng latLng)
    {
        if(latLng == null )
            return;

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude-Constants.defaultZoomverticalOffset,latLng.longitude),(float)Constants.defaultUserZoom));
    }

    private void updateLocationUI(boolean isEnabled) {
        FloatingActionButton directionsFab = (FloatingActionButton) getActivity().findViewById(R.id.trainInfoNavigationFabButton);
        if (directionsFab != null) {
            directionsFab.hide(!isEnabled);
            directionsFab.invalidate();
        }

        TextView directionsName = (TextView) getActivity().findViewById(R.id.directionsName);
        if (directionsName != null) {
            directionsName.setVisibility(isEnabled ? View.VISIBLE : View.GONE);
            directionsName.invalidate();
        }
    }
}
