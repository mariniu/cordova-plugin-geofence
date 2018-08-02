package com.cowbell.cordova.geofence;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class GeoNotificationNotifier {

    protected static final int SMALL_NOTIFICATION_MAX_CHARACTER_LIMIT = 38;

    private static final String GENERIC_CHANNEL_ID = "geofencing";
    private static final String GENERIC_CHANNEL_NAME = "Geo";
    private static final int GENERIC_CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_HIGH;

    private NotificationManager notificationManager;
    private Context context;
    private BeepHelper beepHelper;
    private Logger logger;

    public GeoNotificationNotifier(NotificationManager notificationManager, Context context) {
        this.notificationManager = notificationManager;
        this.context = context;
        this.beepHelper = new BeepHelper();
        this.logger = Logger.getLogger();
    }

    public void notify(Notification notification) {
        notification.setContext(context);

        // create notification channel if needed
        createNotificationChannel();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, GENERIC_CHANNEL_ID)
            .setVibrate(notification.getVibrate())
            .setSmallIcon(notification.getSmallIcon())
            //.setLargeIcon(notification.getLargeIcon())
            .setAutoCancel(true)
            .setContentTitle(notification.getTitle())
            .setContentText(notification.getText());

        // set style to large if message is longer than 38 characters
        if (notification.getTitle() != null && notification.getTitle().length() > SMALL_NOTIFICATION_MAX_CHARACTER_LIMIT) {
            mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(notification.getTitle()));
        }

        if (notification.openAppOnClick) {
            String packageName = context.getPackageName();
            Intent resultIntent = context.getPackageManager()
                .getLaunchIntentForPackage(packageName);

            if (notification.data != null) {
                resultIntent.putExtra("geofence.notification.data", notification.getDataJson());
            }

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                notification.id, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
        }
        try {
            Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, notificationSound);
            r.play();
        } catch (Exception e) {
        	beepHelper.startTone("beep_beep_beep");
            e.printStackTrace();
        }
        notificationManager.notify(notification.id, mBuilder.build());
        logger.log(Log.DEBUG, notification.toString());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // create channel
            NotificationChannel channel = new NotificationChannel(GENERIC_CHANNEL_ID, GENERIC_CHANNEL_NAME, GENERIC_CHANNEL_IMPORTANCE);
            // Sets whether notifications posted to this channel should display notification lights
            channel.enableLights(true);
            // Sets whether notification posted to this channel should vibrate.
            channel.enableVibration(true);
            // Sets the notification light color for notifications posted to this channel
            channel.setLightColor(Color.GREEN);
            // Sets whether notifications posted to this channel appear on the lockscreen or not
            channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PRIVATE);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
