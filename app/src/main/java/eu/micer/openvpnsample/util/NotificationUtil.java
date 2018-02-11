package eu.micer.openvpnsample.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import eu.micer.openvpnsample.ui.activity.MainActivity;
import eu.micer.openvpnsample.R;

public class NotificationUtil {
    private static final String TAG = NotificationUtil.class.getSimpleName();
    private static final String CHANNEL_ID = "vpnapp_channel";
    public static final int NOT_CONNECTED_INFO_NOTIFICATION_ID = 100;
    private static NotificationUtil instance;

    // It'd be better to use Dagger instead.
    public synchronized static NotificationUtil getInstance() {
        if (instance == null) {
            instance = new NotificationUtil();
        }
        return instance;
    }

    private NotificationUtil() {
    }

    public void showNotConnectedInfo(@NonNull Context context) {
        Intent connectIntent = new Intent(context, MainActivity.class);
        connectIntent.putExtra(MainActivity.ACTION_CONNECT, true);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(connectIntent);

        PendingIntent connectPendingIntent =
                stackBuilder.getPendingIntent(
                        10,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.stat_sys_warning)
                        .setContentTitle(context.getString(R.string.vpn_not_connected))
                        .setContentText(context.getString(R.string.tap_to_connect))
                        .setOngoing(true)
                        .addAction(android.R.drawable.ic_media_play, context.getString(R.string.connect), connectPendingIntent);

        connectIntent.removeExtra(MainActivity.ACTION_CONNECT);
        PendingIntent openAppPendingIntent =
                stackBuilder.getPendingIntent(
                        20,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        builder.setContentIntent(openAppPendingIntent);


        NotificationManager notificationManager = getNotificationManager(context);

        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "VPNapp Notifications", NotificationManager.IMPORTANCE_DEFAULT);

                // Configure the notification channel.
                notificationChannel.setDescription("Channel description");
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }
            notificationManager.notify(NOT_CONNECTED_INFO_NOTIFICATION_ID, builder.build());
        } else {
            Log.e(TAG, "Can't init notification manager.");
        }
    }

    public void cancelNotification(@NonNull Context context, int id) {
        getNotificationManager(context).cancel(id);
    }

    private NotificationManager getNotificationManager(@NonNull Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
}
