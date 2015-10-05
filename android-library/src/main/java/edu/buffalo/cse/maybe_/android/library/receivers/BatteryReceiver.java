package edu.buffalo.cse.maybe_.android.library.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import edu.buffalo.cse.maybe_.android.library.utils.Utils;

/**
 * Created by xcv58 on 9/29/15.
 */
public class BatteryReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Utils.debug("BatteryReceiver: " + action);
    }
}
