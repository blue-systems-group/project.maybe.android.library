package edu.buffalo.cse.maybe_.android.library.services;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.iid.InstanceIDListenerService;

import edu.buffalo.cse.maybe_.android.library.MaybeService;
import edu.buffalo.cse.maybe_.android.library.utils.Constants;
import edu.buffalo.cse.maybe_.android.library.utils.Utils;


/**
 * Created by xcv58 on 7/6/15.
 */
public class MyInstanceIDListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        Utils.debug("Token refresh, clean up local cache.");
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.SHARED_PREFERENCE_GCM_ID, Constants.NO_REGISTRATION_ID);
        if (editor.commit()) {
            MaybeService maybeService = MaybeService.getInstance(getApplicationContext());
            maybeService.syncWithBackend();
        } else {
            Utils.debug("Clean up local cache failed!");
        }
    }
}
