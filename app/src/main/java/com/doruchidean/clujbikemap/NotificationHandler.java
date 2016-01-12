package com.doruchidean.clujbikemap;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

/**
 * Created by Doru on 12/01/16.
 *
 */
public class NotificationHandler extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent resultIntent = new Intent(context, MapsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, Activity.RESULT_OK, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.notification_text))
                .setVibrate(new long[]{0, 100, 50, 100, 50, 100, 50, 100, 50, 100})
                .setContentIntent(pendingIntent);
        notificationManager.notify(0, builder.build());
    }
}
