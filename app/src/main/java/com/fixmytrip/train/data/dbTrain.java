package com.fixmytrip.train.data;

import android.provider.BaseColumns;

/**
 * Created by philipkonieczny on 12/15/14.
 */
public class dbTrain {

    public dbTrain()
    {}
    /* Inner class that defines the table contents */
    public static abstract class TrainEntry implements BaseColumns {
        public static final String TABLE_NAME = "Train";
        public static final String COLUMN_NAME_ENTRY_ID = "_id";
        public static final String COLUMN_NAME_SYSTEMID = "System_id";
        public static final String COLUMN_NAME_TRAINNAME="Name";
        public static final String COLUMN_NAME_ISWEEKDAY="isWeekday";
        public static final String COLUMN_NAME_ISLSTOP="isLStop";
        public static final String COLUMN_NAME_ISSOUTHBOUND="isSouthBound";
        public static final String COLUMN_NAME_ISMETRORAILSHUTTLE="isMetroRailShuttle";
        public static final String COLUMN_NAME_LASTUPDATED= "last_update_time";



    }
}
