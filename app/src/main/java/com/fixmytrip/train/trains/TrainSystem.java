package com.fixmytrip.train.trains;

import android.graphics.Color;

import com.fixmytrip.train.R;
import com.fixmytrip.train.utils.PathJSONParser;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by philipkonieczny on 12/5/14.
 */
public class TrainSystem {
    public int id;
    public ArrayList<String> pathCodes;
    public GoogleMap mMap;
    public ArrayList<TrainStation> stations;
    public double cameraLat, cameraLng;
    public double zoom;
    public String last_update;
    public String name;
    public List<PolylineOptions> routes;
    public List<Train> trains;

    public TrainSystem() {
        pathCodes = null;
        mMap = null;
        stations = null;
        cameraLat = cameraLng = 0.0;
        zoom = 0.0;
        routes = new ArrayList<PolylineOptions>();
        trains = new ArrayList<Train>();
    }

    public ArrayList<PolylineOptions> generateMainPath()
    {
        return generateMainPath(pathCodes);
    }

    public PolylineOptions generateMainPathFromPoints(ArrayList<LatLng> points)
    {
        PolylineOptions polyLineOptions;
        polyLineOptions = new PolylineOptions();
        polyLineOptions.addAll(points);
        polyLineOptions.width(12);
        polyLineOptions.color(Color.BLACK);
        routes = new ArrayList<PolylineOptions>();
        routes.add(polyLineOptions);
        mMap.addPolyline(polyLineOptions);
        return polyLineOptions;

    }

    private ArrayList<PolylineOptions> generateMainPath(ArrayList<String> paths)
    {
        ArrayList<PolylineOptions> polyOptions=new ArrayList<PolylineOptions>(paths.size());
        for(String pathString : paths) {
            List<LatLng> trainPath = PathJSONParser.decodePoly(pathString);
            /** Traversing all points */
            List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();
            for (int l = 0; l < trainPath.size(); l++) {
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("lat",
                        Double.toString((trainPath.get(l)).latitude));
                hm.put("lng",
                        Double.toString((trainPath.get(l)).longitude));
                path.add(hm);
            }
            ArrayList<LatLng> points = new ArrayList<LatLng>();
            PolylineOptions polyLineOptions;
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }
            polyLineOptions = new PolylineOptions();
            polyLineOptions.addAll(points);
            polyLineOptions.width(12);
            polyLineOptions.color(Color.BLACK);
            polyOptions.add(polyLineOptions);
        }

        routes=polyOptions;
        for(PolylineOptions p : polyOptions)
        {
            mMap.addPolyline(p);

        }
        return polyOptions;
    }

    //must define a method to add all stations
    public void AddAllStations()
    {
        AddAllStations(stations);
    }

    public void AddAllStations(ArrayList<TrainStation> allStations)
    {
        for (int i=0; i< allStations.size();i++)
        {
            TrainStation tmp=allStations.get(i);
            MarkerOptions mark=new MarkerOptions().position(new LatLng(tmp.lat,tmp.lng)).title(tmp.name);
            mark.icon(BitmapDescriptorFactory.fromResource(R.drawable.station));
            tmp.marker=mMap.addMarker(mark);
        }
    }


    public void zoom()
    {
        mMap.animateCamera( CameraUpdateFactory.zoomTo((float)zoom) );
    }

    public void centerAndZoom()
    {
        mMap.animateCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(cameraLat, cameraLng), (float) zoom) );
    }

    public TrainStation getStationByName(String name)
    {
        if(name==null)
            return null;

        for(TrainStation station : stations)
        {
            if(station.name.equals(name))
                return station;
        }

        return null;
    }

    public TrainStation getStationByAbbreviation(String name)
    {
        if(name==null)
            return null;

        for(TrainStation station : stations)
        {
            if(station.abbreviations.contains(name))
                return station;
        }

        return null;
    }

    public Train getTrainById(int id)
    {
        if(id == -1)
            return null;

        for(Train train : trains)
        {
            if(train.getTrainNumber()== id)
                return train;
        }

        return null;
    }

}
