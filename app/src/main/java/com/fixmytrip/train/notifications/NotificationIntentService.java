package com.fixmytrip.train.notifications;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;

import com.fixmytrip.train.MainActivity;
import com.fixmytrip.train.trains.TrainSystem;
import com.fixmytrip.train.utils.Constants;

import java.util.Calendar;
import java.util.List;

/**
 * Created by philipkonieczny on 10/31/15.
 */
//TODO: Transition this to Server and GCM using a sync adapter : http://developer.android.com/training/scheduling/alarms.html

public class NotificationIntentService extends IntentService{
    private AlarmManager mAlarmManager;
    private PendingIntent mPendingIntent;
    private static boolean isInNightMode = true;
    private int systemId=0;
    private TrainSystem mTrainSystem;

    public NotificationIntentService() {
        super("NotificationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        systemId = extras.getInt(Constants.systemID,0);
        mTrainSystem = MainActivity.trainSystems.get(systemId);

        List<TrainStatus> newStatuses = Parser.updateOfficialNotifications();
        for(TrainStatus trainStatus : newStatuses)
        {
            if(trainStatus!=null && (trainStatus.getStatusReason()!= TrainStatus.StatusReason.GENERAL || trainStatus.getStatusReason()!= TrainStatus.StatusReason.UNKNOWN))
                NotificationHandler.createOrUpdateNotification(this,trainStatus,mTrainSystem);
        }

        Calendar now = Calendar.getInstance();
        if(now.get(Calendar.HOUR_OF_DAY) >= Constants.hourEndNotifications)
            setAlarmNightMode(this);
        else if(isInNightMode)
            setAlarmDayMode(this);

        WebsiteWakefulReceiver.completeWakefulIntent(intent);
    }

    public void setAlarmDayMode(Context context)
    {
        isInNightMode = false;
        setAlarm(context);
    }

    public void setAlarmNightMode(Context context)
    {
        isInNightMode = true;
        setAlarm(context);
    }

    private void setAlarm(Context context)
    {
        if(mAlarmManager != null)
            mAlarmManager.cancel(mPendingIntent);

        mAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WebsiteWakefulReceiver.class);
        mPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        //After after 15 minutes
        if(isInNightMode)
        {
            Calendar now = Calendar.getInstance();
            Calendar alarm = Calendar.getInstance();
            alarm.set(Calendar.HOUR_OF_DAY, Constants.hourStartNotifications);
            alarm.set(Calendar.MINUTE, 1);
            if (alarm.before(now))
                alarm.add(Calendar.DAY_OF_MONTH, 1);  //Add 1 day if time selected before now

            mAlarmManager.set(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(), mPendingIntent);
        }
        else
        {
            //TODO: Need better logic on timing
            mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES ,AlarmManager.INTERVAL_FIFTEEN_MINUTES, mPendingIntent);
        }

    }

}
