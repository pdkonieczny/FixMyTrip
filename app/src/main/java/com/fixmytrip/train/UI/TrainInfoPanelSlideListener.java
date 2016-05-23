package com.fixmytrip.train.UI;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.fixmytrip.train.R;
import com.fixmytrip.train.maps.MapFragment;
import com.fixmytrip.train.trains.TrainStationInfoFragment;
import com.fixmytrip.train.utils.FloatingActionButton;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.model.LatLng;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

/**
 * Created by philipko on 11/6/15.
 */
public class TrainInfoPanelSlideListener implements SlidingUpPanelLayout.PanelSlideListener {
    boolean isHeaderColored= false;
    boolean isdirectionsHidden = false;
    ImageView trainImage;
    FloatingActionButton directionsFab;
    CameraUpdate oldCameraLocation;
    MapFragment mapFragment;
    TrainStationInfoFragment trainStationInfoFragment;
    AppCompatActivity mActivity;

    public TrainInfoPanelSlideListener(AppCompatActivity activity)
    {
        mActivity = activity;
        Fragment frag = mActivity.getSupportFragmentManager().findFragmentById(R.id.content_frame);
        Fragment infoFrag = mActivity.getSupportFragmentManager().findFragmentById(R.id.station_frame);
        if(frag != null && infoFrag != null)
        {
            try{
                mapFragment = (MapFragment) frag;
                trainStationInfoFragment = (TrainStationInfoFragment) infoFrag;
            }
            catch(Exception e){}
        }

    }


    @Override
    public void onPanelSlide(View panel, float slideOffset) {
        if(trainImage==null)
            trainImage = (ImageView)  mActivity.findViewById(R.id.trainInfoImage);
        if(directionsFab==null)
            directionsFab = (FloatingActionButton) mActivity.findViewById(R.id.trainInfoNavigationFabButton);

        if(isHeaderColored && slideOffset < .05)
        {
            updatePanelHeader(false,false);
            isHeaderColored=false;
        }
        else if(!isHeaderColored && slideOffset > .05)
        {
            if(trainStationInfoFragment==null || trainStationInfoFragment.isTrainStation())
                updatePanelHeader(true,true);
            else
                updatePanelHeader(true,false);
            isHeaderColored=true;
        }

        if(isdirectionsHidden && slideOffset<.95 )
        {
            if(directionsFab.getVisibility() == View.GONE) {
                directionsFab.setVisibility(View.VISIBLE);
                trainImage.setVisibility(View.VISIBLE);
            }
            isdirectionsHidden=false;
        }
        else if(!isdirectionsHidden && slideOffset>.95)
        {
            if(directionsFab.getVisibility() == View.VISIBLE) {
                directionsFab.setVisibility(View.GONE);
                trainImage.setVisibility(View.GONE);
            }
            isdirectionsHidden=true;
        }
    }

    @Override
    public void onPanelExpanded(View panel) {

    }

    @Override
    public void onPanelCollapsed(View panel) {
        if(mapFragment!=null && oldCameraLocation!=null)
        {
            mapFragment.focusMap(oldCameraLocation);
        }
    }

    @Override
    public void onPanelAnchored(View panel) {
        Fragment frag = mActivity.getSupportFragmentManager().findFragmentById(R.id.content_frame);
        Fragment infoFrag = mActivity.getSupportFragmentManager().findFragmentById(R.id.station_frame);
        if(frag != null && infoFrag != null)
        {
            try{
                mapFragment = (MapFragment) frag;
                trainStationInfoFragment = (TrainStationInfoFragment) infoFrag;
                oldCameraLocation = mapFragment.getCurrentCameraLocation();
                mapFragment.focusMap(trainStationInfoFragment.getCurrentlySelectedItemLocation());
            }
            catch(Exception e){}
        }
    }

    @Override
    public void onPanelHidden(View panel) {}

    private void updatePanelHeader(boolean isExpanded, boolean isTrainStation)
    {
        int backgroundColor;
        int textColor;
        float stationNameWeight;
        float textSize;
        LinearLayout stationTimeLayout = (LinearLayout) mActivity.findViewById(R.id.StationTimeLayout);


        if(isExpanded)
        {
            stationTimeLayout.setVisibility(LinearLayout.GONE);
            backgroundColor = mActivity.getResources().getColor(isTrainStation ? R.color.Primary3_Orange : R.color.Primary4_Purple);
            textColor = mActivity.getResources().getColor(R.color.White);
            directionsFab.setDrawable(mActivity.getResources().getDrawable(R.drawable.ic_navigation_orange_24dp));
            directionsFab.setColor(mActivity.getResources().getColor(R.color.White));

            stationNameWeight = 1f;
            textSize = 25f;
        }
        else
        {
            stationTimeLayout.setVisibility(LinearLayout.VISIBLE);
            backgroundColor = mActivity.getResources().getColor(R.color.OffWhite);
            textColor = mActivity.getResources().getColor(R.color.Black);
            directionsFab.setDrawable(mActivity.getResources().getDrawable(R.drawable.ic_navigation_white_24dp));
            directionsFab.setColor(mActivity.getResources().getColor(R.color.Primary3_Orange));
            stationNameWeight = 3f;
            textSize = 20f;
        }
        directionsFab.invalidate();

        LinearLayout trainInfoHeader = (LinearLayout) mActivity.findViewById(R.id.trainInfoHeader);
        TextView stationName = (TextView) mActivity.findViewById(R.id.stationName);
        TextView southboundStationLabel = (TextView) mActivity.findViewById(R.id.southboundStationLabel);
        TextView southboundStationName = (TextView) mActivity.findViewById(R.id.southboundStationName);
        TextView northboundStationLabel = (TextView) mActivity.findViewById(R.id.northboundStationLabel);
        TextView northboundStationName = (TextView) mActivity.findViewById(R.id.northboundStationName);

        trainInfoHeader.setBackgroundColor(backgroundColor);
        stationName.setTextColor(textColor);
        southboundStationLabel.setTextColor(textColor);
        southboundStationName.setTextColor(textColor);
        northboundStationLabel.setTextColor(textColor);
        northboundStationName.setTextColor(textColor);
        stationName.setTextSize(textSize);
        stationName.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, stationNameWeight));
    }
}
