package edu.buffalo.cse.maybe_.android.library.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import edu.buffalo.cse.maybe_.android.library.MaybeService;
import edu.buffalo.cse.maybe_.android.library.utils.Utils;


public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String intentAction = intent.getAction();
        if (intentAction.equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            Utils.debug("receive BOOT UP intent. Simulate app init MaybeService");
            MaybeService maybeService = MaybeService.getInstance(context);
            Utils.debug("Manually call syncWithBackend");
            maybeService.syncWithBackend();
            // maybeService.periodicTask();
        } else {
            Utils.debug("receive unknown intent action: " + intentAction);
        }
    }
}
