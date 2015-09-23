package edu.buffalo.cse.maybe_.android.library.services;

import com.google.android.gms.iid.InstanceIDListenerService;

import edu.buffalo.cse.maybe_.android.library.MaybeService;
import edu.buffalo.cse.maybe_.android.library.utils.Utils;


/**
 * Created by xcv58 on 7/6/15.
 */
public class MyInstanceIDListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        Utils.debug("Token refresh!");
        MaybeService maybeService = MaybeService.getInstance(getApplicationContext());
        maybeService.init();
    }
}
