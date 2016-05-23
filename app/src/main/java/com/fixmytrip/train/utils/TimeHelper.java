package com.fixmytrip.train.utils;

import java.util.Calendar;

/**
 * Created by philipkonieczny on 12/21/14.
 */
public class TimeHelper {


    public static Calendar getCurrentTime()
    {
        Calendar now=Calendar.getInstance();
        now.set(1970,0,1);
        return now;
    }

    public static boolean todayIsWeekday()
    {
        int day=Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        return day != Calendar.SATURDAY && day != Calendar.SUNDAY;
    }

}
