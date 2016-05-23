package com.fixmytrip.train.data;

import android.provider.BaseColumns;

/**
 * Created by philipkonieczny on 12/20/14.
 */
public class dbTrainRoute {
    public dbTrainRoute()
    {}
    /* Inner class that defines the table contents */
    public static abstract class TrainRouteEntry implements BaseColumns {
        public static final String TABLE_NAME = "TrainRoute";
        public static final String COLUMN_NAME_ENTRY_ID = "_id";
        public static final String COLUMN_NAME_ENTRY_SYSTEMID = "System_id";
        public static final String COLUMN_NAME_ROUTE="Route";
        public static final String COLUMN_NAME_LASTUPDATE="last_update_time";
    }
}


