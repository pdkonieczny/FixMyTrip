package com.fixmytrip.train.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.fixmytrip.train.MainActivity;
import com.fixmytrip.train.R;
import com.fixmytrip.train.trains.TrainStation;
import com.fixmytrip.train.trains.TrainSystem;
import com.fixmytrip.train.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by philipkonieczny on 10/31/15.
 */
public class NotificationHandler {



    public static void createOrUpdateNotification(Context context, TrainStatus trainStatus, TrainSystem trainSystem)
    {
        Intent mapIntent = new Intent(context, MainActivity.class);
        mapIntent.setAction(Constants.ACTION_MAPS);
        if(trainStatus.getTrainNumber() != -1) {
            mapIntent.putExtra(Constants.MAIN_ACTIVITY_TRAIN_EXTRA, trainStatus.getTrainNumber());
            mapIntent.putExtra(Constants.MAIN_ACTIVITY_EXPAND_INFO_EXTRA, true);
        }
        mapIntent.putExtra(Constants.MAIN_ACTIVITY_FRAGMENT_EXTRA, Constants.MAIN_ACTIVITY_MAIN_FRAGMENT_VALUE);
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(mapIntent);
        PendingIntent mapPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        Intent settingsIntent = new Intent(context, MainActivity.class);
        settingsIntent.setAction(Constants.ACTION_PREFERENCES);
        settingsIntent.putExtra(Constants.MAIN_ACTIVITY_FRAGMENT_EXTRA, Constants.MAIN_ACTIVITY_SETTINGS_FRAGMENT_VALUE);
        TaskStackBuilder settingsStackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        settingsStackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        settingsStackBuilder.addNextIntent(settingsIntent);
        PendingIntent piSettings =
                settingsStackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(Parser.getNotificationTitle(trainStatus))
                        .setContentText(Parser.getNotificationMainText(trainStatus, trainSystem))
                        .setColor(context.getResources().getColor(R.color.Primary3_Orange))
                        .setDefaults(Notification.DEFAULT_ALL) // requires VIBRATE permission
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(Parser.getNotificationBigText(trainStatus)))
                                .addAction(R.drawable.ic_map_black_24dp, context.getResources().getString(R.string.notification_map_action), mapPendingIntent)
                                .addAction(R.drawable.ic_settings_black_24dp, context.getResources().getString(R.string.notification_settings_action), piSettings);

        mBuilder.setContentIntent(mapPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(trainStatus.getId(), mBuilder.build());
    }
}
