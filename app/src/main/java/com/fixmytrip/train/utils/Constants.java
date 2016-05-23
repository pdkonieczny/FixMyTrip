package com.fixmytrip.train.utils;

import com.fixmytrip.train.trains.TrainStation;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by philipko on 12/1/2014.
 */
public class Constants {

    //TODO: Need to move to cloud
    public static double GEOFENCE_MAX_DISTANCE_TO_PATH = 1000; //in meters.
    public final static int GEOFENCE_STATION_RADIUS = 200;
    public enum trainSystems
    {
        None,
        TriRail,
        MetroRail,
        AllAboardFlorida

    }

    public final static int MAPS_TAB = 0;
    public final static int ALERTS_TAB = 1;
    public final static int SETTINGS_TAB = 2;
    public final static int ABOUT_TAB = 3;

    public final static String ACTION_PREFERENCES = "com.fixmytrip.train.PREFERENCES";
    public final static String ACTION_MAPS = "com.fixmytrip.train.MAPS";

    public final static String AlertsURL = "http://www.tri-rail.com/vip.mobile/mobile_vip_message.asp"; //"http://fix-my-trip.s3-website-us-east-1.amazonaws.com/Tri-Rail_Webpages/2015_10_27.html"
    public final static String ScheduleURL = "http://www.tri-rail.com/train-schedules/TrainSchedule.aspx?dp=%DEPART%&ar=%ARRIVE%&dt=%WEEKDAY%";
    public final static String TrainInfoURL = "https://fixmytrip.herokuapp.com/";
    public final static String FixMyTripURL = "http://www.fixmytrip.com";
    public final static String GooglePlayURL = "https://play.google.com/store/apps/details?id=";
    public final static String FacebookURL ="https://www.facebook.com/FixMyTripUSA";
    public final static String FacebookAPIURL ="fb://page/931593433538644"; //get from https://graph.facebook.com/fixmytripUSA
    public final static String TwitterURL = "https://twitter.com/intent/tweet?hashtags=FixMyTrip&original_referer=http%3A%2F%2Ffixmytrip.herokuapp.com%2F&text=Check%20out%20FixMyTrip%20-%20A%20New%20Way%20to%20Track%20South%20Florida%20commuter%20trains&tw_p=tweetbutton&url=http%3A%2F%2Fwww.fixmytrip.com&via=FixMyTrip_com";
    public final static String LegalURL = "http://www.fixmytrip.com";

    public final static String facebookPackageName = "com.facebook.katana";

    public final static int hourStartNotifications=5;
    public final static int hourEndNotifications=22;

    public final static int UPDATE_INTERVAL = 1000*15; //1 minutes
    public static final String PACKAGE_NAME="com.google.android.apps.maps";
    public static final double defaultUserZoom=14;
    public static final double defaultZoomverticalOffset = .014; //how much to offset to center location when panel is anchored
    public static final String train_regex="^P[0-9][0-9][0-9]$";
    public static ArrayList<LatLng> aaf_points= new ArrayList<LatLng>(20);
    public static final String systemID="systemID";
    public static final String TrainSystem="TrainSystem";
    public static final String trainStationInfoKey="trainStationInfoKey";
    public static final String trainStationInfoExpanded= "trainStationInfoExpanded";
    public static final String MAIN_ACTIVITY_TRAIN_EXTRA = "com.fixmytrip.train.MAIN_ACTIVITY_TRAIN_EXTRA";
    public static final String  MAIN_ACTIVITY_EXPAND_INFO_EXTRA = "com.fixmytrip.train.MAIN_ACTIVITY_EXPAND_INFO_EXTRA";
    public static final String MAIN_ACTIVITY_FRAGMENT_EXTRA = "com.fixmytrip.train.MAIN_ACTIVITY_FRAGMENT_EXTRA";
    public static final String MAIN_ACTIVITY_SETTINGS_FRAGMENT_VALUE = "Settings";
    public static final String MAIN_ACTIVITY_MAIN_FRAGMENT_VALUE = "Main";


    public static void loadStations()
    {
        aaf_loadStations();
    }

    public static void aaf_loadStations()
    {
        aaf_points.add(new LatLng(25.781844, -80.195853));
        aaf_points.add(new LatLng(25.787554, -80.196375));
        aaf_points.add(new LatLng(25.811626, -80.190839));
        aaf_points.add(new LatLng(25.851257, -80.189036));
        aaf_points.add(new LatLng(25.890715, -80.170820));
        aaf_points.add(new LatLng(25.918278, -80.156658));
        aaf_points.add(new LatLng(25.947828, -80.147253));
        aaf_points.add(new LatLng(25.999788, -80.148590));
        aaf_points.add(new LatLng(26.124150, -80.145516));
        aaf_points.add(new LatLng(26.146219, -80.131412));
        aaf_points.add(new LatLng(26.209458, -80.132185));
        aaf_points.add(new LatLng(26.332773, -80.091241));
        aaf_points.add(new LatLng(26.446876, -80.072472));
        aaf_points.add(new LatLng(26.709766, -80.055337));
        aaf_points.add(new LatLng(26.903961, -80.068255));
        aaf_points.add(new LatLng(27.302491, -80.263263));
        aaf_points.add(new LatLng(27.601299, -80.381768));
        aaf_points.add(new LatLng(27.908767, -80.514977));
        aaf_points.add(new LatLng(28.421416, -80.749318));
        aaf_points.add(new LatLng(28.410546, -80.842702));
        aaf_points.add(new LatLng(28.455228, -80.866048));
        aaf_points.add(new LatLng(28.451498, -81.324949));
    }

}
