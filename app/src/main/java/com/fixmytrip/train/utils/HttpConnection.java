package com.fixmytrip.train.utils;

/**
 * Created by philipko on 12/2/2014.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import android.util.Log;

import com.fixmytrip.train.trains.TrainStation;

public class HttpConnection {
    public String readUrl(String mapsApiDirectionsUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(mapsApiDirectionsUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            Log.d("Exception while reading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /*private String getMapsApiDirectionsUrl() {
        TrainStation start=Constants.ts_allStations.get(0);
        TrainStation end =Constants.allStations.get(Constants.allStations.size()-1);

        Date date=new Date();
        String params = "origin="+ Constants.startStation+"&destination="+Constants.endStation +"&mode=transit&departure_time="+(date.getTime()/1000);
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params;
        return url;
    }*/

}
