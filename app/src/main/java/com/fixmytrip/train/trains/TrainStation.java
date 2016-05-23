package com.fixmytrip.train.trains;

import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by philipko on 12/1/2014.
 */
public class TrainStation {
    public int system;
    public String name;
    public String shortName;
    public double lat;
    public double lng;
    public String address;
    public String city;
    public int zipCode;
    public int rank;
    public String last_update;
    public Marker marker;
    public List<String> abbreviations;

    public TrainStation()
    {
        abbreviations = new ArrayList<String>();
    }


}
