package com.example.onedee;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/** Class to publish notification **/

public class NotificationPublisher extends BroadcastReceiver {
    public static String NOTIFICATION_ID = "assignment due";
    public static String NOTIFICATION = "notification";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = intent.getParcelableExtra(NOTIFICATION);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "Notification_channel_name";
            String description = "Include all the personal importance";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel notificationChannel = new NotificationChannel(AddEventScreen.NOTIFICATION_CHANNEL_ID, name, importance);
//            notificationChannel.setDescription(description);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);
        }

        int notificationId = intent.getIntExtra(NOTIFICATION_ID, 0);
        assert notificationManager != null;
        notificationManager.notify(notificationId, notification);
    }
}
