package edu.buffalo.cse.maybe_.android.library.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import edu.buffalo.cse.maybe_.android.library.MaybeService;
import edu.buffalo.cse.maybe_.android.library.utils.Constants;
import edu.buffalo.cse.maybe_.android.library.utils.Utils;


public class BootCompletedAndPullReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String intentAction = intent.getAction();
        if (intentAction.equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            Utils.debug("receive BOOT UP intent. Simulate app init MaybeService");
            MaybeService maybeService = MaybeService.getInstance(context, true);
            maybeService.setRepeatPull(context);
        } else if (intentAction.equalsIgnoreCase(Constants.PULL_INTENT)) {
            Utils.debug("receive PULL intent. Start sync");
            MaybeService maybeService = MaybeService.getInstance(context, true);
        } else {
            Utils.debug("receive unknown intent action: " + intentAction);
        }
    }
}
