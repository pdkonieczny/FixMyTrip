package com.fixmytrip.train.trains;

import com.fixmytrip.train.MainActivity;
import com.fixmytrip.train.R;
import com.fixmytrip.train.utils.Constants;
import com.fixmytrip.train.utils.TimeHelper;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * Created by philipkonieczny on 12/14/14.
 */
public class Train {

    public ArrayList<Date> trainTimes;
    public boolean isWeekdayNotWeekend;
    public boolean isLStop;
    public boolean isSouthbound;
    public boolean isMetroRailShuttle;
    public String name;
    private MarkerOptions markerOptions;
    private Marker mark;
    public TrainSystem system;
    private double direction; //clockwise degrees from due north (-180<x<180)
    private LatLng currentLocation;

    public void updateLocation(GoogleMap mMap){
        if(markerOptions==null){
            markerOptions=new MarkerOptions();
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.train_icon));
            if(isSouthbound)
                markerOptions.rotation(180); //TODO:NEED TO ADJUST DIRECTION OF TRAIN BASED ON ROUTE

            markerOptions.title(name);
        }

        if(isTrainActive())
        {
            if(mark==null)
                mark=mMap.addMarker(markerOptions.position(new LatLng(0,0)));

            setCurrentLocationAndDirection();
            /*Random r = new Random();
            int stat=r.nextInt(Constants.ts_allStations.size());
            LatLng currentPos= new LatLng(Constants.ts_allStations.get(stat).lat,Constants.ts_allStations.get(stat).lng);*/
            if(currentLocation!=null)
            {
                mark.setPosition(currentLocation);
            }
            mark.setRotation((float)direction);

        }
        else
        {
            if(mark!=null)
                mark.remove();
        }
    }

    public boolean isTrainActive()
    {
        Calendar now= TimeHelper.getCurrentTime();
        Calendar start=Calendar.getInstance();
        Calendar end=Calendar.getInstance();

        //Either train is weekend and its a weekday or train is weekday and its a weekend
        if((!isWeekdayNotWeekend && TimeHelper.todayIsWeekday()) || (isWeekdayNotWeekend && !TimeHelper.todayIsWeekday())) {
            return false;
        }

        if(isSouthbound){
            start.setTime(trainTimes.get(0));
            end.setTime(trainTimes.get(trainTimes.size() - 1));
        }
        else {
            start.setTime(trainTimes.get(trainTimes.size()-1));
            end.setTime(trainTimes.get(0));
        }
        return now.after(start) && now.before(end);
    }

    private void setCurrentLocationAndDirection()
    {
        Calendar now = TimeHelper.getCurrentTime();
        TrainStation nextStation = getNextStation();
        if(nextStation==null)
        {
            currentLocation=null;
            direction=0;
            return;
        }
        TrainStation previousStation;
        int nextstationNum=nextStation.rank-1;

        int prevstationNum;
        long timeSincePreviousStation=0;
        long timeToNextStation=0;
        Calendar stopTime=Calendar.getInstance();
        stopTime.setTime(trainTimes.get(nextstationNum));
        Calendar lastStopTime= Calendar.getInstance();
        if(isSouthbound)
        {
            prevstationNum = nextstationNum-1;
            previousStation= system.stations.get(nextstationNum-1);
        }
        else
        {
            prevstationNum = nextstationNum+1;
            previousStation = system.stations.get(nextstationNum+1);
        }

        lastStopTime.setTime(trainTimes.get(prevstationNum));
        timeToNextStation= stopTime.getTimeInMillis() - now.getTimeInMillis();
        timeSincePreviousStation=now.getTimeInMillis()- lastStopTime.getTimeInMillis();

        if(system==null)
        {
            currentLocation = new LatLng(previousStation.lat,previousStation.lng);
            double degrees=Math.toDegrees(Math.atan((nextStation.lng-previousStation.lng)/(nextStation.lat-previousStation.lat)));
            if(isSouthbound)
            {
                degrees+=180; //need to flip to southbound so add 180 degrees
            }
            direction=degrees;
            return;
        }

        double percentOfLegComplete = ((double)timeSincePreviousStation)/(timeSincePreviousStation+timeToNextStation);
        double newLatitude;

        newLatitude = previousStation.lat - percentOfLegComplete*(previousStation.lat-nextStation.lat);

        //TODO:HANDLE CASE WHERE NOT ALL NORTH/SOUTH ORDERED AND MULTI ROUTE SYSTEMS
        List<LatLng> points = system.routes.get(0).getPoints();

        for(int i=0;i<points.size();i++)
        {
            LatLng pts = points.get(i);
            if(pts.latitude<newLatitude)
            {
                currentLocation=pts;
                int nextStationIndex=i + (isSouthbound ? 1:-1);
                if(i<0) //at first point so must be northbound
                {
                    direction=0;
                }
                else if(i>=points.size()) //at last point so must be southbound
                {
                    direction=180;
                }
                else
                {
                    LatLng next=points.get(nextStationIndex);
                    double degrees=Math.toDegrees(Math.atan((next.longitude-pts.longitude)/(next.latitude-pts.latitude)));
                    if(isSouthbound)
                    {
                        degrees+=180;
                    }
                    direction=degrees;
                }
                currentLocation=pts;
                return;
            }
        }

        currentLocation = new LatLng(previousStation.lat,previousStation.lng);
        double degrees = Math.toDegrees(Math.atan((nextStation.lng-previousStation.lng)/(nextStation.lat-previousStation.lat)));
        if(isSouthbound)
        {
            degrees+=180;
        }
        direction=degrees;
    }

    public TrainStation getNextStation()
    {
        if(!isTrainActive())
        {
            return null;
        }
        Calendar now= TimeHelper.getCurrentTime();
        if(isSouthbound)
        {
            for(int i=0;i<trainTimes.size();i++)
            {
                Calendar stopTime=Calendar.getInstance();
                stopTime.setTime(trainTimes.get(i));
                if(now.before(stopTime))
                {
                    return system.stations.get(i);
                }
            }
        }
        else
        {
            for(int i=trainTimes.size()-1;i>-1;i--)
            {
                Calendar stopTime=Calendar.getInstance();
                stopTime.setTime(trainTimes.get(i));
                if(now.before(stopTime))
                {
                    return system.stations.get(i);
                }
            }
        }
        return null; //should not be getting to this point
    }

    public Date getTrainStopTime(TrainStation trainStation)
    {
        if(trainStation ==null)
        {
            return null;
        }
        return trainTimes.get(trainStation.rank-1);
    }

    public int getTrainNumber()
    {
        if(name==null || name.length()!=4)
            return -1;
        try
        {
            return Integer.parseInt(name.substring(1));
        }
        catch (Exception e)
        {
            return -1;
        }
    }


    public LatLng getCurrentLocation() {
        return currentLocation;
    }

    public boolean isSouthboundTrain()
    {
        return getTrainNumber() % 2 ==1;
    }

    public boolean isUpcoming(TrainStation trainStation) {
        Calendar now= TimeHelper.getCurrentTime();
        Calendar stopTime=Calendar.getInstance();

        //Either train is weekend and its a weekday or train is weekday and its a weekend
        if((!isWeekdayNotWeekend && TimeHelper.todayIsWeekday()) || (isWeekdayNotWeekend && !TimeHelper.todayIsWeekday())) {
            return false;
        }

        Date stopDate = getTrainStopTime(trainStation);
        if(stopDate == null)
            return false;

        stopTime.setTime(stopDate);

        return now.before(stopTime);

    }
}
