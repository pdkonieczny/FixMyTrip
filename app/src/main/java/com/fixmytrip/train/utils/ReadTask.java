package com.fixmytrip.train.utils;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.EventListener;

/**
 * Created by philipkonieczny on 12/5/14.
 */
public class ReadTask extends AsyncTask<String, Void, String> {
    NetworkEventListener networkEventL;
    public ReadTask(NetworkEventListener networkEventListener)
    {
        networkEventL=networkEventListener;
    }
    @Override
    protected String doInBackground(String... url) {
        String data="";
        JSONObject jsonObject=null;
        try {
            HttpConnection http = new HttpConnection();
            data = http.readUrl(url[0]);
            jsonObject = new JSONObject(data);
            int seconds=jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getInt("value");
            data=Integer.toString(seconds);
        } catch (Exception e) {
            Log.d("Background Task", e.toString());
        }

        //jsonObject.
        return data;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        networkEventL.onEventCompleted(result);
    }
}
