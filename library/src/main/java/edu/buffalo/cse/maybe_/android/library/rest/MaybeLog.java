package edu.buffalo.cse.maybe_.android.library.rest;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import org.json.JSONObject;

/**
 * Created by xcv58 on 9/28/15.
 */
public class MaybeLog {
    public String label;
    public long timestamp;
    public float batteryStatus;
    public JSONObject logObject;

    private float getBatteryStatus(Context context) {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryIntent == null) {
            return -1.0f;
        }
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return ((float) level / (float) scale) * 100.0f;
    }

    public MaybeLog(String label, JSONObject jsonObject, Context context) {
        this.logObject = jsonObject;
        this.label = label;
        this.timestamp = System.currentTimeMillis();
        this.batteryStatus = this.getBatteryStatus(context);
    }
}
