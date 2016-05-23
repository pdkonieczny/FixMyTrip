package com.fixmytrip.train.data;

import android.provider.BaseColumns;

/**
 * Created by philipkonieczny on 12/15/14.
 */
public final class dbTrainStop {

    public dbTrainStop()
    {}
    /* Inner class that defines the table contents */
    public static abstract class TrainStopEntry implements BaseColumns {
        public static final String TABLE_NAME = "TrainStop";
        public static final String COLUMN_NAME_ENTRY_ID = "_id";
        public static final String COLUMN_NAME_SYSTEMID = "System_id";
        public static final String COLUMN_NAME_STATIONNAME= "StationName";
        public static final String COLUMN_NAME_SHORTNAME= "ShortName";
        public static final String COLUMN_NAME_LAT= "Latitude";
        public static final String COLUMN_NAME_LNG= "Longitude";
        public static final String COLUMN_NAME_ADDRESS= "Address";
        public static final String COLUMN_NAME_CITY= "City";
        public static final String COLUMN_NAME_ZIPCODE ="ZipCode";
        public static final String COLUMN_NAME_NORTHBOUNDORDER= "NorthboundOrder";
        public static final String COLUMN_NAME_LASTUPDATED= "last_update_time";
        public static final String COLUMN_ABBREV1= "Abbrev1";
        public static final String COLUMN_ABBREV2= "Abbrev2";
        public static final String COLUMN_ABBREV3= "Abbrev3";


    }

}
