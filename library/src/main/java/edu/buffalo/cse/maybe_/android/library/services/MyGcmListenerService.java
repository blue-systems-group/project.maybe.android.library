package edu.buffalo.cse.maybe_.android.library.services;

import android.app.NotificationManager;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;

import edu.buffalo.cse.android.maybe_.android.library.R;
import edu.buffalo.cse.maybe_.android.library.MaybeService;
import edu.buffalo.cse.maybe_.android.library.utils.Utils;

/**
 * Created by xcv58 on 7/6/15.
 */
public class MyGcmListenerService extends GcmListenerService {
    @Override
    public void onMessageReceived(String from, Bundle data) {
        Utils.debug("from: " + from + "; data: " + data.toString());
        this.sendNotification("Push from Maybe Backend");
        Utils.debug("trigger init for MaybeService!");
        MaybeService maybeService = MaybeService.getInstance(getApplicationContext());
        maybeService.init();
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.common_full_open_on_phone)
                .setContentTitle("GCM Message")
                .setContentText(message)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
