package com.fixmytrip.train;

import android.support.v4.app.Fragment;
import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fixmytrip.train.UI.TrainInfoPanelSlideListener;
import com.fixmytrip.train.data.DatabaseHelper;
import com.fixmytrip.train.fragments.AboutFragment;
import com.fixmytrip.train.fragments.SettingsFragment;
import com.fixmytrip.train.maps.MapFragment;
import com.fixmytrip.train.notifications.NotificationIntentService;
import com.fixmytrip.train.trains.AlertsFragment;
import com.fixmytrip.train.trains.Train;
import com.fixmytrip.train.trains.TrainInformationFragment;
import com.fixmytrip.train.trains.TrainStation;
import com.fixmytrip.train.trains.TrainStationInfoFragment;
import com.fixmytrip.train.trains.TrainSystem;
import com.fixmytrip.train.utils.Constants;
import com.fixmytrip.train.utils.FloatingActionButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.model.LatLng;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        MapFragment.UpdateStationListener,
        TrainStationInfoFragment.TrainSelectedListener
{


    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private String[] mDrawerOptions;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private static final String TAG ="MainActivity";
    static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;
    public static ArrayList<TrainSystem> trainSystems;
    private SlidingUpPanelLayout mSlidingLayout;
    private static Intent notificationIntentService;
    //private RelativeLayout mDrawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Constants.loadStations(); TODO: ADD THIS BACK IN FOR AAF
        setContentView(R.layout.activity_main_drawer);
        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerOptions=getResources().getStringArray(R.array.main_drawer_options_array);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.activity_main_drawer_list_item, mDrawerOptions));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer);
        getSupportActionBar().setHomeButtonEnabled(true);

        mSlidingLayout = (SlidingUpPanelLayout) findViewById(R.id.main_sliding_layout);
        mSlidingLayout.setPanelSlideListener(new TrainInfoPanelSlideListener(this));

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        DatabaseHelper myDbHelper = new DatabaseHelper(this);

        try {

            myDbHelper.createDataBase();

        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        try {
            myDbHelper.openDataBase();
            trainSystems=myDbHelper.loadAllSystems();
            for(TrainSystem trainSystem : trainSystems)
            {
                trainSystem.stations = myDbHelper.loadAllStations(trainSystem.id);
                ArrayList<Train> trains = myDbHelper.loadAllTrains(trainSystem);
                if(trains!=null)
                {
                    trainSystem.trains = trains;
                }
            }

        }catch(SQLException sqle) {
            throw new Error("Unable to create database");
        }

        Bundle extras = getIntent().getExtras();
        if(extras!=null)
        {
            String fragmentExtra = extras.getString(Constants.MAIN_ACTIVITY_FRAGMENT_EXTRA);
            if(fragmentExtra!=null && !fragmentExtra.isEmpty())
            {
                if(fragmentExtra.equals(Constants.MAIN_ACTIVITY_SETTINGS_FRAGMENT_VALUE))
                {
                    selectItem(Constants.SETTINGS_TAB);
                }
                else if(fragmentExtra.equals(Constants.MAIN_ACTIVITY_MAIN_FRAGMENT_VALUE))
                {
                    ;
                    selectItem(Constants.MAPS_TAB, extras.getInt(Constants.MAIN_ACTIVITY_TRAIN_EXTRA, -1),extras.getBoolean(Constants.MAIN_ACTIVITY_EXPAND_INFO_EXTRA,false));
                }
            }
        }
        else if (savedInstanceState == null ) {
            selectItem(Constants.MAPS_TAB);
        }

    }

    @Override
    public void onStart()
    {
        super.onStart();
        if(notificationIntentService == null)
        {
            notificationIntentService = new Intent(this, NotificationIntentService.class);
            notificationIntentService.putExtra(Constants.systemID,0);
            startService(notificationIntentService);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkPlayServices()) {
           Log.d(TAG,"Google Play Services Successful");
        }
    }

    private boolean checkPlayServices() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
                showErrorDialog(status);
            } else {
                Toast.makeText(this, "This device is not supported.",
                        Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    public void showErrorDialog(int code) {
        GooglePlayServicesUtil.getErrorDialog(code, this,
                REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_RECOVER_PLAY_SERVICES:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Google Play Services must be installed.",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) {
            case R.id.action_websearch:
                // create intent to perform web search for this planet
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, getSupportActionBar().getTitle());
                // catch event that there's no activity to handle intent
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onStationSelected(TrainStation trainStation, LatLng currentLocation, List<Train> allTrains) {
        TrainStationInfoFragment stationFrag = (TrainStationInfoFragment)
                getSupportFragmentManager().findFragmentById(R.id.station_frame);
        if (stationFrag != null)
        {
            if(currentLocation == null)
                stationFrag.updateTrainStationInfo(trainStation, currentLocation, allTrains, false);
            else
                stationFrag.updateTrainStationInfo(trainStation, currentLocation, allTrains, true);
        }
    }

    @Override
    public void onTrainSelected(Train train) {
        TrainStationInfoFragment stationFrag = (TrainStationInfoFragment)
                getSupportFragmentManager().findFragmentById(R.id.station_frame);
        if (stationFrag != null)
        {
            stationFrag.updateTrainInfo(train);
        }
    }

    @Override
    public void updateInformation() {
        TrainStationInfoFragment stationFrag = (TrainStationInfoFragment)
                getSupportFragmentManager().findFragmentById(R.id.station_frame);
        stationFrag.refresh();
    }

    @Override
    public void onTrainInfoRequested(Train train) {
        Fragment frag = new TrainInformationFragment();
        if(train!=null) {
            Bundle args = new Bundle();
            args.putString("Train", train.name);
            frag.setArguments(args);
        }
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction;
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, frag);
        fragmentTransaction.commit();
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position)
    {
        selectItem(position,-1,false);
    }

    private void selectItem(int position, int trainNumber, boolean expandInfoPanel) {
        // update the main content by replacing fragments
        Fragment frag=null;
        TrainStationInfoFragment trainStationInfoFragment = new TrainStationInfoFragment();
        Bundle stationInfoBundle = new Bundle();
        //TODO: need persistent storage for current mode we are in
        stationInfoBundle.putInt(Constants.systemID,0);
        stationInfoBundle.putInt(Constants.MAIN_ACTIVITY_TRAIN_EXTRA,trainNumber);
        stationInfoBundle.putBoolean(Constants.MAIN_ACTIVITY_EXPAND_INFO_EXTRA,expandInfoPanel);
        trainStationInfoFragment.setArguments(stationInfoBundle);

        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction;

        if (position==Constants.MAPS_TAB){
            Fragment oldFrag= getSupportFragmentManager().findFragmentById(R.id.map);
            if(oldFrag!=null)
            {

                fragmentTransaction= fragmentManager.beginTransaction();
                fragmentTransaction.remove(oldFrag);
                fragmentTransaction.commit();
                fragmentManager.executePendingTransactions();
            }
                frag = new MapFragment();
            Bundle args = new Bundle();
            //TODO: need persistent storage for current mode we are in
            args.putInt(Constants.systemID,0);
            args.putInt(Constants.MAIN_ACTIVITY_TRAIN_EXTRA, trainNumber);
            frag.setArguments(args);
            mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

        }
//        else if(position==1)
//        {
//            frag = new TrainInformationFragment();
//        }
        else if(position==Constants.ALERTS_TAB)
        { //Alerts
            frag = new AlertsFragment();
            mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        }
        else if(position == Constants.SETTINGS_TAB)
        {
            frag = new SettingsFragment();
            mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            Bundle args = new Bundle();
            //TODO: need persistent storage for current mode we are in
            args.putInt(Constants.systemID,0);
            frag.setArguments(args);
        }
        else
        { //About
            frag= new AboutFragment();
            mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        }


        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, frag);
        fragmentTransaction.replace(R.id.station_frame,trainStationInfoFragment);
        fragmentTransaction.commit();


        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mDrawerOptions[position]);
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }





}
