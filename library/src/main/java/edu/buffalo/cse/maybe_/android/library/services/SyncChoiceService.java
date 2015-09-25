package edu.buffalo.cse.maybe_.android.library.services;


import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;

import edu.buffalo.cse.maybe_.android.library.MaybeService;
import edu.buffalo.cse.maybe_.android.library.utils.Constants;
import edu.buffalo.cse.maybe_.android.library.utils.Utils;

/**
 * Created by xcv58 on 9/24/15.
 */
public class SyncChoiceService extends GcmTaskService {
    @Override
    public void onInitializeTasks() {
        Utils.debug("onInitializeTasks");
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.HAS_PERIODIC_TASK, false);
        if (!editor.commit()) {
            Utils.debug("Clean HAS_PERIODIC_TASK failed!");
        }
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        Utils.debug("SyncChoiceService onRunTask " + taskParams.getTag());
        MaybeService.getInstance(getApplicationContext(), true);
        return GcmNetworkManager.RESULT_SUCCESS;
    }
}
