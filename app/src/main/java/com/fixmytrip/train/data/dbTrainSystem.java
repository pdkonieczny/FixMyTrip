package com.fixmytrip.train.data;

/**
 * Created by philipkonieczny on 12/20/14.
 */

import android.provider.BaseColumns;
/**
 * Created by philipkonieczny on 12/15/14.
 */
public class dbTrainSystem {

    public dbTrainSystem()
    {}
    /* Inner class that defines the table contents */
    public static abstract class TrainSystemEntry implements BaseColumns {
        public static final String TABLE_NAME = "System";
        public static final String COLUMN_NAME_ENTRY_ID = "_id";
        public static final String COLUMN_NAME_NAME="Name";
        public static final String COLUMN_NAME_LASTUPDATE="last_update_time";
        public static final String COLUMN_NAME_LATITUDE="latitude";
        public static final String COLUMN_NAME_LONGITUDE="longitude";
        public static final String COLUMN_NAME_ZOOM="zoom";
    }
}
