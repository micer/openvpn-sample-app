package eu.micer.openvpnsample.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import eu.micer.openvpnsample.util.NotificationUtil;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = BootReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null &&
                intent.getAction() != null &&
                intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

            NotificationUtil.getInstance().showNotConnectedInfo(context);
        }
    }
}
