package com.fixmytrip.train.data;

import android.provider.BaseColumns;

/**
 * Created by philipkonieczny on 12/15/14.
 */
public class dbTrainSchedule {

    public dbTrainSchedule()
    {}
    /* Inner class that defines the table contents */
    public static abstract class TrainScheduleEntry implements BaseColumns {
        public static final String TABLE_NAME = "TrainSchedule";
        public static final String COLUMN_NAME_ENTRY_ID = "_id";
        public static final String COLUMN_NAME_SYSTEMID = "System_id";
        public static final String COLUMN_NAME_TRAINID= "Train_id";
        public static final String COLUMN_NAME_STATIONID= "Station_id";
        public static final String COLUMN_NAME_TIME= "Time";
        public static final String COLUMN_NAME_LASTUPDATED= "last_update_time";

    }
}
