package com.fixmytrip.train.trains;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fixmytrip.train.MainActivity;
import com.fixmytrip.train.R;
import com.fixmytrip.train.fragments.AboutFragment;
import com.fixmytrip.train.notifications.Parser;
import com.fixmytrip.train.notifications.TrainStatus;
import com.fixmytrip.train.notifications.UpdateAdapter;
import com.fixmytrip.train.utils.Constants;
import com.fixmytrip.train.utils.FloatingActionButton;
import com.fixmytrip.train.utils.Helper;
import com.fixmytrip.train.utils.NetworkEventListener;
import com.fixmytrip.train.utils.ReadTask;
import com.fixmytrip.train.utils.TimeHelper;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.Serializable;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by philipkonieczny on 12/10/14.
 */
public class TrainStationInfoFragment extends Fragment implements NetworkEventListener,View.OnClickListener {
    private static final String TAG= "TrainStationInfoFrag";
    private TrainStation currentSelectedStation;
    private Train currentTrain;
    private LatLng currentLocation;
    private List<Train> currentTrains;
    private TextView trainInfoUpdateLabel, directionView, southboundStationLabel, northboundStationLabel, southboundStationNameExpandedLabel, northboundStationNameExpandedLabel, callLabel, saveLabel, scheduleLabel, stationName, southboundStationName, northboundStationName, directionsName, southboundStationNameExpanded, northboundStationNameExpanded;
    private FloatingActionButton directionsFAB;
    private LinearLayout callButton, saveButton, scheduleButton;
    private ListView updateList;
    private TrainSelectedListener mCallback;

    private int mapId;

    public boolean isTrainStation() {
        return currentSelectedStation !=null;
    }


    // The container Activity must implement this interface so the frag can deliver messages
    public interface TrainSelectedListener {
        /** Called by HeadlinesFragment when a list item is selected */
        public void onTrainInfoRequested(Train train);
    }

    public LatLng getCurrentlySelectedItemLocation()
    {
        if(currentSelectedStation != null)
            return new LatLng(currentSelectedStation.lat,currentSelectedStation.lng);
        else if(currentTrain != null)
            return currentTrain.getCurrentLocation();

        return null;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.train_station_fragment, container, false);
        mapId= getArguments().getInt(Constants.systemID);
        int trainNumber = getArguments().getInt(Constants.MAIN_ACTIVITY_TRAIN_EXTRA,-1);
        Train trainFromArgs = MainActivity.trainSystems.get(mapId).getTrainById(trainNumber);
        if(trainFromArgs != null)
        {
            updateTrainInfo(trainFromArgs);
        }
        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Activity activity = getActivity();
        directionView=(TextView) activity.findViewById(R.id.directionsName);
        directionView.setOnClickListener(this);

        updateList = (ListView) activity.findViewById(R.id.trainStationUpdatelist);

        directionsFAB = (FloatingActionButton) activity.findViewById(R.id.trainInfoNavigationFabButton);
        directionsFAB.setOnClickListener(this);

        callButton = (LinearLayout) activity.findViewById(R.id.trainInfoCallActionLinearLayout);
        saveButton = (LinearLayout) activity.findViewById(R.id.trainInfoSaveActionLinearLayout);
        scheduleButton = (LinearLayout) activity.findViewById(R.id.trainInfoScheduleActionLinearLayout);

        stationName=(TextView) getActivity().findViewById(R.id.stationName);
        southboundStationName = (TextView) getActivity().findViewById(R.id.southboundStationName);
        northboundStationName = (TextView) getActivity().findViewById(R.id.northboundStationName);
        southboundStationLabel = (TextView) getActivity().findViewById(R.id.southboundStationLabel);
        northboundStationLabel = (TextView) getActivity().findViewById(R.id.northboundStationLabel);
        directionsName = (TextView) getActivity().findViewById(R.id.directionsName);
        southboundStationNameExpanded = (TextView) getActivity().findViewById(R.id.southboundStationNameExpanded);
        northboundStationNameExpanded = (TextView) getActivity().findViewById(R.id.northboundStationNameExpanded);
        southboundStationNameExpandedLabel = (TextView) getActivity().findViewById(R.id.southboundStationNameExpandedLabel);
        northboundStationNameExpandedLabel = (TextView) getActivity().findViewById(R.id.northboundStationNameExpandedLabel);
        callLabel = (TextView) getActivity().findViewById(R.id.trainInfoCallLabel);
        saveLabel = (TextView) getActivity().findViewById(R.id.trainInfoSaveLabel);
        scheduleLabel = (TextView) getActivity().findViewById(R.id.trainInfoScheduleLabel);
        trainInfoUpdateLabel = (TextView) getActivity().findViewById(R.id.trainInfoUpdateLabel);

        callButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        scheduleButton.setOnClickListener(this);

        int trainNumber = getArguments().getInt(Constants.MAIN_ACTIVITY_TRAIN_EXTRA,-1);
        if(trainNumber != -1)
        {
            updateTrainInfo(MainActivity.trainSystems.get(mapId).getTrainById(trainNumber));
            if(getArguments().getBoolean(Constants.MAIN_ACTIVITY_EXPAND_INFO_EXTRA,false))
            {
                SlidingUpPanelLayout mSlidingLayout = (SlidingUpPanelLayout) getActivity().findViewById(R.id.main_sliding_layout);
                mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
            }
        }
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (TrainSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement TrainSelectedListener");
        }
    }

    public void refresh()
    {
        //TODO: WILL NOT UPDATE DIRECTIONS
        Log.d(TAG, "Refreshing TrainStationInfoFragment");
        if(currentTrain!=null) //is a train
        {
            updateTrainInfo(currentTrain);
        }
        else if(currentSelectedStation!=null)
        {
            updateTrainStationInfo(currentSelectedStation,currentLocation,currentTrains,false);
        }
    }

    public void updateTrainStationInfo(TrainStation trainStation, LatLng mCurrentLocation, List<Train> allTrains,boolean updateDirections)
    {
        currentTrain=null;
        if(currentSelectedStation!=null)
        {
            currentSelectedStation.marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.station));
        }
        if(trainStation.marker!=null)
        {
            trainStation.marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.station_white));
        }

        currentSelectedStation=trainStation;

        if(mCurrentLocation != null)
            currentLocation=mCurrentLocation;

        currentTrains = allTrains;

        boolean isweekday=TimeHelper.todayIsWeekday();
        long southboundMillis=Long.MAX_VALUE;
        long northboundMillis=Long.MAX_VALUE;
        long currentTimeMillis=TimeHelper.getCurrentTime().getTimeInMillis();
        for(Train train : allTrains)
        {
            if(( isweekday && train.isWeekdayNotWeekend) || (!isweekday && !train.isWeekdayNotWeekend))
            {
                long stoptime=train.getTrainStopTime(currentSelectedStation).getTime();
                if(train.isSouthbound && stoptime>currentTimeMillis && (stoptime - currentTimeMillis)<southboundMillis)
                {
                    southboundMillis=stoptime-currentTimeMillis;
                }
                else if(!train.isSouthbound && stoptime>currentTimeMillis && (stoptime - currentTimeMillis)<northboundMillis)
                {
                    northboundMillis=stoptime-currentTimeMillis;
                }
            }
        }
        if(southboundMillis!=Long.MAX_VALUE)
        {
            String timingString = southboundMillis/(1000*60) + " min";
            southboundStationName.setText(timingString);
            southboundStationNameExpanded.setText(timingString);

        }
        else
        {
            southboundStationName.setText("N/A");
            southboundStationNameExpanded.setText("N/A");
        }

        if(northboundMillis!=Long.MAX_VALUE)
        {
            String timingString = northboundMillis / (1000 * 60) + " min";
            northboundStationName.setText(timingString);
            northboundStationNameExpanded.setText(timingString);
        }
        else
        {
            northboundStationName.setText("N/A");
            northboundStationNameExpanded.setText("N/A");
        }

        stationName.setText(trainStation.shortName);

        if(updateDirections) {
            String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + mCurrentLocation.latitude + "," + mCurrentLocation.longitude + "&destination=" + trainStation.address.replace(" ", "+") + "+" + trainStation.city.replace(" ", "+") + "+FL+" + trainStation.zipCode;
            new ReadTask(this).execute(url);
        }

        refreshStationUpdates(trainStation);
        refreshUI(true);
        updateSavedButton();
    }

    public void updateTrainInfo(Train train)
    {
        currentTrain=train;
        currentSelectedStation=null;

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float dp = 5f;
        float fpixels = metrics.density * dp;

        stationName.setText(train.name);
        TrainStation trainStation = train.getNextStation();
        if(trainStation != null) {
            southboundStationName.setText(trainStation.shortName);
            long timeInMillis=train.getTrainStopTime(trainStation).getTime() - TimeHelper.getCurrentTime().getTimeInMillis();
            northboundStationName.setText(timeInMillis/(1000*60) + " mins");
        }else {
            southboundStationName.setText("N/A");
            northboundStationName.setText("N/A");
        }

        refreshTrainUpdates(train);
        refreshUI(false);
        updateSavedButton();
    }

    private void refreshTrainUpdates(Train train) {
        refreshUpdates(Parser.getAllNotificationsByTrain(train));
    }

    private void refreshStationUpdates(TrainStation trainStation)
    {
        refreshUpdates(Parser.getAllNotificationsByStation(trainStation, currentTrains));
    }

    private void refreshUpdates(List<TrainStatus> trainStatuses)
    {
        TrainStatus trainStatusArray[] = new TrainStatus[trainStatuses.size()];
        trainStatuses.toArray(trainStatusArray);
        UpdateAdapter adapter = new UpdateAdapter(getActivity(),R.layout.updates_listview_item,trainStatusArray);
        updateList.setAdapter(adapter);
    }


    @Override
    public void onEventCompleted(String result) {
        int secondsToStation=0;
        try
        {
            secondsToStation = Integer.parseInt(result);
        }
        catch(Exception e)
        {
            Log.d(TAG,"Unable to parse seconds from result");
        }
        if(secondsToStation>0) {
            directionView.setText(""+(int)Math.ceil((double) secondsToStation / 60) + " min");
        }
        else
        {
            directionView.setText("Err");
        }
    }

    @Override
    public void onClick(View view) {
        if(currentSelectedStation == null && currentTrain == null)
            return;

        if(view.equals(directionView) || view.equals(directionsFAB)){
            if(currentSelectedStation!=null)
            {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("google.navigation:q=" + currentSelectedStation.address.replace(" ","+") + ",+" + currentSelectedStation.city.replace(" ","+") + ",+FL+" + currentSelectedStation.zipCode));
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            }
            else
            {
                Log.d(TAG,"onClick: currentSelectedStation = null");
            }
        }
        else if(view.equals(callButton))
        {
            Helper.callPhoneNumber(getContext());
        }
        else if(view.equals(saveButton))
        {
            boolean prevSaved = isSaved();

            updateSavedButtonUI(!prevSaved); //toggle

            SharedPreferences sharedPref = getContext().getSharedPreferences(
                    getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            TextView stationName = (TextView) getActivity().findViewById(R.id.stationName);
            if (currentTrain != null) //is a train
            {
                Set<String> starTrains = sharedPref.getStringSet(getString(R.string.preference_star_trains), null);
                if (starTrains != null && !prevSaved) {
                    starTrains.add(stationName.getText().toString());
                }
                else if(starTrains != null && prevSaved)
                {
                    starTrains.remove(stationName.getText().toString());
                }
                else {
                    starTrains = new HashSet<String>();
                    starTrains.add(stationName.getText().toString());
                }
                editor.putStringSet(getString(R.string.preference_star_trains), starTrains);
            } else if (currentSelectedStation != null) {
                Set<String> starStations = sharedPref.getStringSet(getString(R.string.preference_star_stations), null);
                if (starStations != null && !prevSaved) {
                    starStations.add(currentSelectedStation.name);
                } else if(starStations != null && prevSaved)
                {
                    starStations.remove(currentSelectedStation.name);
                } else
                {
                    starStations = new HashSet<String>();
                    starStations.add(currentSelectedStation.name);
                }
                editor.putStringSet(getString(R.string.preference_star_stations), starStations);
            }
            editor.commit();
        }
        else if(view.equals(scheduleButton))
        {
            if(currentSelectedStation!=null) {
                TrainStation home = Helper.getHomeStation(getContext(), MainActivity.trainSystems.get(mapId));
                TrainStation away = Helper.getAwayStation(getContext(), MainActivity.trainSystems.get(mapId));
                if (currentSelectedStation.equals(home))
                    showSchedule(currentSelectedStation, away, TimeHelper.todayIsWeekday());
                else
                    showSchedule(currentSelectedStation, home, TimeHelper.todayIsWeekday());
                //TODO:NEED TO SHOW SCHEDULE
            }
            else
            {
                showTrainSchedule();
            }
        }
        else
        {
            mCallback.onTrainInfoRequested(currentTrain);
        }
    }

    private void updateSavedButton()
    {
        updateSavedButtonUI(isSaved());
    }

    private boolean isSaved()
    {
        SharedPreferences sharedPref = getContext().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        TextView stationName=(TextView) getActivity().findViewById(R.id.stationName);
        boolean isSaved=false;
        if(currentTrain!=null) //is a train
        {
            Set<String> starTrains = sharedPref.getStringSet(getString(R.string.preference_star_trains), null);
            if(starTrains!=null && starTrains.contains(stationName.getText().toString()))
            {
                return true;
            }
        }
        else if(currentSelectedStation!=null)
        {
            Set<String> starStations = sharedPref.getStringSet(getString(R.string.preference_star_stations), null);
            if(starStations!=null && starStations.contains(currentSelectedStation.name)) {
                return true;
            }
        }

        return false;
    }

    private void updateSavedButtonUI(boolean isSaved)
    {
        ImageView saveIcon = (ImageView) getActivity().findViewById(R.id.trainInfoSaveIcon);
        TextView saveLabel = (TextView) getActivity().findViewById(R.id.trainInfoSaveLabel);

        if(isSaved) {
            saveIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_black_24dp));
            saveLabel.setText(getString(R.string.saved));
        }
        else
        {
            saveIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_border_black_24dp));
            saveLabel.setText(getString(R.string.not_saved));
        }
    }

    private void showSchedule(TrainStation departure, TrainStation arrival, boolean isWeekday)
    {

        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getLayoutInflater();

        WebView popupView = (WebView) layoutInflater.inflate(R.layout.popup_schedule, null);
        popupView.setWebViewClient(new WebViewClient());
        String scheduleURL = Constants.ScheduleURL;
        if (isWeekday)
            scheduleURL = scheduleURL.replace("%WEEKDAY%","1");
        else
            scheduleURL = scheduleURL.replace("%WEEKDAY%","2");


        scheduleURL = scheduleURL.replace("%DEPART%","" + (18-departure.rank+1)); //TODO: FIX the 18
        int arrivalrank = 1;
        if(arrival!=null)
            arrivalrank = 18 - arrival.rank; //TODO: FIX the 18
        else if(departure.rank<9)
            arrivalrank = 1;
        else
            arrivalrank = 18;

        scheduleURL = scheduleURL.replace("%ARRIVE%","" + arrivalrank);

        popupView.loadUrl(scheduleURL);

        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.MATCH_PARENT,
                true);

        popupWindow .setTouchable(true);
        popupWindow .setFocusable(true);

        popupWindow .showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }

    private void refreshUI(boolean isTrainStation) {
        if(isTrainStation)
        {
            updateLabelColor(getResources().getColor(R.color.Primary3_Orange));
            southboundStationLabel.setText(getActivity().getString(R.string.SBTrainLabel));
            southboundStationNameExpandedLabel.setText(getActivity().getString(R.string.SBTrainExpandedLabel));
            northboundStationLabel.setText(getActivity().getString(R.string.NBTrainLabel));
            northboundStationNameExpandedLabel.setText(getActivity().getString(R.string.NBTrainExpandedLabel));
            directionsFAB.setVisibility(View.VISIBLE);
            directionsName.setVisibility(View.VISIBLE);
        }

        else
        {
            updateLabelColor(getResources().getColor(R.color.Primary4_Purple));
            southboundStationLabel.setText(getActivity().getString(R.string.nextStopLabel));
            southboundStationNameExpandedLabel.setText(getActivity().getString(R.string.nextStopLabel));
            northboundStationLabel.setText(getActivity().getString(R.string.scheduledTrainLabel));
            northboundStationNameExpandedLabel.setText(getActivity().getString(R.string.scheduledTrainLabel));
            directionsFAB.setVisibility(View.GONE);
            directionsName.setVisibility(View.GONE);
        }
    }

    private void updateLabelColor(int color)
    {
        southboundStationNameExpandedLabel.setTextColor(color);
        northboundStationNameExpandedLabel.setTextColor(color);
        callLabel.setTextColor(color);
        saveLabel.setTextColor(color);
        scheduleLabel.setTextColor(color);
        trainInfoUpdateLabel.setTextColor(color);
    }

    private void showTrainSchedule() {
        if(currentTrain==null)
            return;

        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getLayoutInflater();
        RelativeLayout popupView = (RelativeLayout)layoutInflater.inflate(R.layout.popup_schedule_train, null);

        if(currentTrain.isSouthboundTrain())
        {
            getActivity().findViewBy.popup_schedule_train_stationLabel1)
        }
        else
        {

        }

        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.MATCH_PARENT,
                true);

        popupWindow .setTouchable(true);
        popupWindow .setFocusable(true);

        popupWindow .showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }
}
