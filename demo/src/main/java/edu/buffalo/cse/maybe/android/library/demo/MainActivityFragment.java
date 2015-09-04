package edu.buffalo.cse.maybe.android.library.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import edu.buffalo.cse.android.maybelibrary.MaybeService;
import edu.buffalo.cse.android.maybelibrary.QueryIntentService;
import edu.buffalo.cse.android.maybelibrary.utils.Utils;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private MaybeService maybeService;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        maybeService = MaybeService.getInstance(getActivity().getApplicationContext());
//        this.testIntentService();
        this.testMaybeVariable();
        this.logMaybe();
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    public void testMaybeVariable() {
        int choice = maybeService.get("another block test");
        Utils.debug("choice: " + choice);
        switch (choice) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
            default:
                break;
        }
    }

    public void logMaybe() {

//        long periodSecs = 30L; // the task should be executed every 30 seconds
//        long flexSecs = 15L; //the task can run as early as -15 seconds from the scheduled time
//        String tag = "PeriodicLogUpdate"; // a unique task identifier
//        Utils.debug("Main Activity Context1 =" + getActivity().getApplicationContext());
//        PeriodicTask periodic = new PeriodicTask.Builder()
//                .setService(GCMPeriodicLogUpdateService.class)
//                .setPeriod(periodSecs)
//                .setFlex(flexSecs)
//                .setTag(tag)
//                .setPersisted(true)
//                .setRequiredNetwork(com.google.android.gms.gcm.Task.NETWORK_STATE_CONNECTED)
//                .setRequiresCharging(true)
//                .build();
//        GcmNetworkManager.getInstance(getActivity().getApplicationContext()).schedule(periodic);
//        Utils.debug("Main Activity Context2 =" + getActivity().getApplicationContext());
        JSONObject logging = new JSONObject();
        try {
            logging.put("label","test");
            logging.put("parameter2","testAgain");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        maybeService.log(logging);
    }

    private void testIntentService() {
        Intent intent = new Intent(getActivity(), QueryIntentService.class);
        intent.setAction("maybe.phone_lab.org.edu.buffalo.cse.android.edu.buffalo.cse.android.maybelibrary.action.FOO");
        intent.putExtra("maybe.phone_lab.org.edu.buffalo.cse.android.edu.buffalo.cse.android.maybelibrary.extra.PARAM1", "1");
        intent.putExtra("maybe.phone_lab.org.edu.buffalo.cse.android.edu.buffalo.cse.android.maybelibrary.extra.PARAM2", "2");
        getActivity().startService(intent);
    }
}
