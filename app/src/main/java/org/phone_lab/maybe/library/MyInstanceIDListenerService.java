package org.phone_lab.maybe.library;

import com.google.android.gms.iid.InstanceIDListenerService;

import org.phone_lab.maybe.library.utils.Utils;


/**
 * Created by xcv58 on 7/6/15.
 */
public class MyInstanceIDListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
//        Intent intent = new Intent(this, RegistrationIntentService.class);
//        startService(intent);
        Utils.debug("Token refresh!");
    }
}
