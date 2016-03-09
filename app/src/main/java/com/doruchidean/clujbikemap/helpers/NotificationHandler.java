package com.doruchidean.clujbikemap.helpers;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.NotificationCompat;

import com.doruchidean.clujbikemap.R;
import com.doruchidean.clujbikemap.activities.MapsActivity;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Doru on 12/01/16.
 *
 */
public class NotificationHandler extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {

        GregorianCalendar calendar = new GregorianCalendar();

        Intent resultIntent = new Intent(context, MapsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, Activity.RESULT_OK, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(String.format(context.getString(R.string.notification_text),
                                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)))
                        .setVibrate(new long[]{0, 200, 200, 200, 200, 200, 400, 200, 200, 200, 200, 200})
                        .setLights(Color.WHITE, 100, 100)
                        .setContentIntent(pendingIntent);

        Notification notification = builder.build();
        notification.flags = Notification.DEFAULT_SOUND |
                            Notification.FLAG_AUTO_CANCEL |
                            Notification.PRIORITY_HIGH;
        notificationManager.notify(0, notification);

        PersistenceManager.getInstance(context).setIsCountingDown(false);
    }
}
