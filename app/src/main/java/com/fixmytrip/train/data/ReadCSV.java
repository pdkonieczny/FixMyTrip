package com.fixmytrip.train.data;

import android.content.Context;

import com.fixmytrip.train.R;
import com.fixmytrip.train.trains.Train;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by philipkonieczny on 12/14/14.
 */
public class ReadCSV {

    public static void ReadTrainSchedules(Context context, DatabaseHelper myDbHelper)
    {
        ArrayList<Train> weekdayNorthTrains= new ArrayList<Train>();
        ArrayList<Train> weekdaySouthTrains= new ArrayList<Train>();
        ArrayList<Train> weekendNorthTrains= new ArrayList<Train>();
        ArrayList<Train> weekendSouthTrains= new ArrayList<Train>();

        InputStream inputStream= context.getResources().openRawResource(R.raw.nb_wkdays);
        List l= readCSVfile(inputStream);
        weekdayNorthTrains=parseTrainList(l);

        inputStream= context.getResources().openRawResource(R.raw.nb_wkends);
        l= readCSVfile(inputStream);
        weekendNorthTrains=parseTrainList(l);

        inputStream= context.getResources().openRawResource(R.raw.sb_wkdays);
        l= readCSVfile(inputStream);
        weekdaySouthTrains=parseTrainList(l);

        inputStream= context.getResources().openRawResource(R.raw.sb_wkends);
        l= readCSVfile(inputStream);
        weekendSouthTrains=parseTrainList(l);

        //myDbHelper.saveTrain(weekdayNorthTrains);
        //myDbHelper.saveTrain(weekdaySouthTrains);
        //myDbHelper.saveTrain(weekendNorthTrains);
        //myDbHelper.saveTrain(weekendSouthTrains);
        //myDbHelper.close();
    }

    private static ArrayList<Train> parseTrainList(List l)
    {
        ArrayList<Train> trains = new ArrayList<Train>();
        for(Object obj: l)
        {

            Train train = new Train();
            String[] str= (String[])obj;
            if(str[0].contains("WKEND") || str[0].contains("WKDAY"))
                continue;
            ArrayList<String> entries=new ArrayList<String>(Arrays.asList(str));

            train.name=entries.get(0);
            List<String> times = entries.subList(1,18);
            ArrayList<Date> dates= new ArrayList<Date>();
            final String TIME_FORMAT = "HH:mm";
            final SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
            for(String time : times)
            {
                try {
                    dates.add((Date)timeFormat.parse(time));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            train.trainTimes=dates;
            if(entries.get(18).equals("TRUE"))
                train.isWeekdayNotWeekend=true;
            else
                train.isWeekdayNotWeekend=false;

            if(entries.get(19).equals("TRUE"))
                train.isLStop=true;
            else
                train.isLStop=false;

            if(entries.get(20).equals("TRUE"))
                train.isSouthbound=true;
            else
                train.isSouthbound=false;

            if(entries.get(21).equals("TRUE"))
                train.isMetroRailShuttle=true;
            else
                train.isMetroRailShuttle=false;

            trains.add(train);
        }
        return trains;
    }

    private static List readCSVfile(InputStream inputStream){
        List resultList = new ArrayList();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String csvLine;
            while ((csvLine = reader.readLine()) != null) {
                String[] row = csvLine.split(",");
                resultList.add(row);
            }
        }

        catch (IOException ex) {
            throw new RuntimeException("Error in reading CSV file: "+ex);
        }
        finally {
            try {
                inputStream.close();
            }
            catch (IOException e) {
                throw new RuntimeException("Error while closing input stream: "+e);
            }
        }
        return resultList;
    }

}
