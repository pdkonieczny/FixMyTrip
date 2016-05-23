package com.fixmytrip.train.notifications;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by philipkonieczny on 10/31/15.
 */
public class WebsiteWakefulReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, NotificationIntentService.class);
        startWakefulService(context, service);
    }
}
