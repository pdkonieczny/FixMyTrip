package com.fixmytrip.train.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.fixmytrip.train.trains.Train;
import com.fixmytrip.train.trains.TrainStation;
import com.fixmytrip.train.trains.TrainSystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by philipkonieczny on 12/14/14.
 */
public class DatabaseHelper extends SQLiteOpenHelper{

    //The Android's default system path of your application database.
    private static String DB_PATH = "/data/data/com.fixmytrip.train/databases/";

    private static String DB_NAME = "database.sqlite";

    protected SQLiteDatabase myDataBase;

    private final Context myContext;

    private static final int DB_VERSION = 1;

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     */
    public DatabaseHelper(Context context) {

        super(context, DB_NAME, null, DB_VERSION);
        this.myContext = context;
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();

        if(!dbExist){
            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();

            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){

        SQLiteDatabase checkDB = null;
        try{
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        }catch(SQLiteException e){

            //database does't exist yet.

        }

        if(checkDB != null){

            checkDB.close();

        }

        return checkDB != null;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{

        //Open your local db as the input stream

      InputStream myInput = myContext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    public void openDataBase() throws SQLException {

        //Open the database
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public synchronized void close() {

        if(myDataBase != null)
            myDataBase.close();

        super.close();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public ArrayList<Train> loadAllTrains(TrainSystem system)
    {
        int system_id = system.id;
        ArrayList<Train> trains = new ArrayList<Train>();
        Cursor c = myDataBase.rawQuery("Select * FROM Train WHERE System_id=" + system_id,null);

        c.moveToFirst();

        while(!c.isAfterLast()){
            Train train_temp = new Train();
            train_temp.name = c.getString(c.getColumnIndex(dbTrain.TrainEntry.COLUMN_NAME_TRAINNAME));
            train_temp.system = system;
            if(c.getInt(c.getColumnIndex(dbTrain.TrainEntry.COLUMN_NAME_ISWEEKDAY)) == 1)
                train_temp.isWeekdayNotWeekend = true;
            else
                train_temp.isWeekdayNotWeekend = false;

            if(c.getInt(c.getColumnIndex(dbTrain.TrainEntry.COLUMN_NAME_ISLSTOP))== 1)
                train_temp.isLStop = true;
            else
                train_temp.isLStop = false;

            if(c.getInt(c.getColumnIndex(dbTrain.TrainEntry.COLUMN_NAME_ISSOUTHBOUND))== 1)
                train_temp.isSouthbound = true;
            else
                train_temp.isSouthbound = false;

            if(c.getInt(c.getColumnIndex(dbTrain.TrainEntry.COLUMN_NAME_ISMETRORAILSHUTTLE))== 1)
                train_temp.isMetroRailShuttle = true;
            else
                train_temp.isMetroRailShuttle = false;

            Cursor c2 = myDataBase.rawQuery("Select * FROM TrainSchedule WHERE System_id=" + system_id + " AND Train_id=" + c.getInt(c.getColumnIndex(dbTrain.TrainEntry.COLUMN_NAME_ENTRY_ID)) + " ORDER BY Station_id asc",null);
            ArrayList<Date> dates = new ArrayList<Date>();
            DateFormat inputFormat  =  new SimpleDateFormat("HH:mm");
            c2.moveToFirst();
            while(!c2.isAfterLast()){
                String str_date = c2.getString(c2.getColumnIndex(dbTrainSchedule.TrainScheduleEntry.COLUMN_NAME_TIME));
                Date d = null;
                try {
                   d = inputFormat.parse(str_date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                dates.add(d);
                c2.moveToNext();
            }
            train_temp.trainTimes = dates;
            trains.add(train_temp);
            c.moveToNext();
        }

        return trains;
    }

    public ArrayList<TrainStation> loadAllStations(int system_id)
    {
        ArrayList<TrainStation> trainStations = new ArrayList<TrainStation>();
        Cursor c = myDataBase.rawQuery("Select * FROM TrainStop WHERE System_id=" + system_id + " ORDER BY NorthboundOrder asc",null);

        c.moveToFirst();

        while(!c.isAfterLast()){
            TrainStation trainStation = new TrainStation();
            trainStation.system = c.getInt(c.getColumnIndex(dbTrainStop.TrainStopEntry.COLUMN_NAME_SYSTEMID));
            trainStation.name = c.getString(c.getColumnIndex(dbTrainStop.TrainStopEntry.COLUMN_NAME_STATIONNAME));
            trainStation.shortName = c.getString(c.getColumnIndex(dbTrainStop.TrainStopEntry.COLUMN_NAME_SHORTNAME));
            trainStation.lat = c.getDouble(c.getColumnIndex(dbTrainStop.TrainStopEntry.COLUMN_NAME_LAT));
            trainStation.lng = c.getDouble(c.getColumnIndex(dbTrainStop.TrainStopEntry.COLUMN_NAME_LNG));
            trainStation.address = c.getString(c.getColumnIndex(dbTrainStop.TrainStopEntry.COLUMN_NAME_ADDRESS));
            trainStation.city = c.getString(c.getColumnIndex(dbTrainStop.TrainStopEntry.COLUMN_NAME_CITY));
            trainStation.zipCode = c.getInt(c.getColumnIndex(dbTrainStop.TrainStopEntry.COLUMN_NAME_ZIPCODE));
            trainStation.rank = c.getInt(c.getColumnIndex(dbTrainStop.TrainStopEntry.COLUMN_NAME_NORTHBOUNDORDER));
            trainStation.last_update = c.getString(c.getColumnIndex(dbTrainStop.TrainStopEntry.COLUMN_NAME_LASTUPDATED));
            String abbrev1 = c.getString(c.getColumnIndex(dbTrainStop.TrainStopEntry.COLUMN_ABBREV1));
            if(abbrev1!=null)
                trainStation.abbreviations.add(abbrev1);

            String abbrev2 = c.getString(c.getColumnIndex(dbTrainStop.TrainStopEntry.COLUMN_ABBREV2));
            if(abbrev2!=null)
                trainStation.abbreviations.add(abbrev2);

            String abbrev3 = c.getString(c.getColumnIndex(dbTrainStop.TrainStopEntry.COLUMN_ABBREV3));
            if(abbrev3!=null)
                trainStation.abbreviations.add(abbrev3);

            c.moveToNext();
            trainStations.add(trainStation);
        }

        return trainStations;
    }

    public ArrayList<TrainSystem> loadAllSystems()
    {
        ArrayList<TrainSystem> trainSystems = new ArrayList<TrainSystem>();
        Cursor c=myDataBase.rawQuery("Select * FROM System ORDER BY _id",null);

        c.moveToFirst();

        while(!c.isAfterLast()){
            TrainSystem trainSystem = new TrainSystem ();
            trainSystem.id = c.getInt(c.getColumnIndex(dbTrainSystem.TrainSystemEntry.COLUMN_NAME_ENTRY_ID));
            trainSystem.name =  c.getString(c.getColumnIndex(dbTrainSystem.TrainSystemEntry.COLUMN_NAME_NAME));
            trainSystem.last_update =  c.getString(c.getColumnIndex(dbTrainSystem.TrainSystemEntry.COLUMN_NAME_LASTUPDATE));
            trainSystem.cameraLat = c.getDouble(c.getColumnIndex(dbTrainSystem.TrainSystemEntry.COLUMN_NAME_LATITUDE));
            trainSystem.cameraLng = c.getDouble(c.getColumnIndex(dbTrainSystem.TrainSystemEntry.COLUMN_NAME_LONGITUDE));
            trainSystem.zoom = c.getDouble(c.getColumnIndex(dbTrainSystem.TrainSystemEntry.COLUMN_NAME_ZOOM));
            Cursor c2 = myDataBase.rawQuery("Select * FROM " + dbTrainRoute.TrainRouteEntry.TABLE_NAME + " WHERE System_id=" + trainSystem.id ,null);
            ArrayList<String> paths = new ArrayList<String>();
            c2.moveToFirst();
            while(!c2.isAfterLast()){
                String str = c2.getString(c2.getColumnIndex(dbTrainRoute.TrainRouteEntry.COLUMN_NAME_ROUTE));
                paths.add(str);
                c2.moveToNext();
            }
            trainSystem.pathCodes = paths;
            trainSystems.add(trainSystem);
            c.moveToNext();
        }
        return trainSystems;
    }






    // Add your public helper methods to access and get content from the database.
    // You could return cursors by doing "return myDataBase.query(....)" so it'd be easy
    // to you to create adapters for your views.

}
