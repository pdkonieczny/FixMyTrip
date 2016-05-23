package com.fixmytrip.train.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.fixmytrip.train.R;
import com.fixmytrip.train.trains.TrainStation;
import com.fixmytrip.train.trains.TrainSystem;

/**
 * Created by philipkonieczny on 10/27/15.
 */
public class Helper {

    public static void callPhoneNumber(Context context)
    {
        Uri number = Uri.parse("tel:18008747245,,1,,1");
        Intent callIntent = new Intent(Intent.ACTION_CALL, number);
        try{
            context.startActivity(callIntent);
        }
        catch (Exception e)
        {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL, number);
            context.startActivity(dialIntent);
        }
    }

    public static TrainStation getHomeStation(Context context, TrainSystem trainSystem) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        String homeStationString = sharedPref.getString(context.getString(R.string.preference_home_station), "");
        return trainSystem.getStationByName(homeStationString);

    }

    public static TrainStation getAwayStation(Context context, TrainSystem trainSystem) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        String awayStationString = sharedPref.getString(context.getString(R.string.preference_away_station), "");
        return trainSystem.getStationByName(awayStationString);

    }
}
