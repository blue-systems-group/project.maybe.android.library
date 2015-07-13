package org.phone_lab.maybe.library;

import android.app.IntentService;
import android.content.Intent;
import org.phone_lab.maybe.library.utils.Utils;

/**
 * Created by ramyarao on 7/13/15.
 */
public class LogIntentService extends IntentService {
    @Override
    protected void onHandleIntent(Intent intent) {
        Utils.debug("onHandleIntent of Log");
        if (intent != null) {
        //TODO : Add logic to triger log method of maybeService
        }
    }
    public LogIntentService() {
        super("QueryIntentService");
    }
}
