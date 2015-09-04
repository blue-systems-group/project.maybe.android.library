package edu.buffalo.cse.android.maybelibrary;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;

import edu.buffalo.cse.android.maybelibrary.utils.Utils;

/**
 * Created by ramyarao on 8/25/15.
 */
public class GCMPeriodicLogUpdateService extends GcmTaskService{

    private MaybeService maybeService;
    @Override
    public int onRunTask(TaskParams taskParams) {
        maybeService = MaybeService.getInstance(getApplicationContext());
        Utils.debug("onRunTask : " + taskParams.getTag());
        //TODO:add logic to detect new log files
        //TODO:invoke POST here
//        JSONObject logging = new JSONObject();
//        try {
//            logging.put("label","test");
//            logging.put("parameter2","testAgain");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        maybeService.log(logging);
        return GcmNetworkManager.RESULT_SUCCESS;
    }
}
