package edu.buffalo.cse.maybe_.android.library.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import edu.buffalo.cse.maybe_.android.library.MaybeService;
import edu.buffalo.cse.maybe_.android.library.utils.Utils;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private MaybeService maybeService;
    private Handler timerHandler = new Handler();

    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
//            TextView textView = (TextView) getActivity().findViewById(R.id.hello_world);
//            String maybeText = maybe("String") {"Maybe String 1", "Maybe String 2"};
//            textView.setText(maybeText);
//            maybe("color") {
//                textView.setBackgroundColor(Color.RED);
//            } or {
//                textView.setBackgroundColor(Color.YELLOW);
//            } or {
//                textView.setBackgroundColor(Color.GREEN);
//            } or {
//                textView.setBackgroundColor(Color.WHITE);
//            }
//            timerHandler.postDelayed(this, 500);
        }
    };


    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        maybeService = MaybeService.getInstance(getActivity().getApplicationContext());
        maybe("123") {
            Utils.debug("Test 1");
        } or {
            Utils.debug("Test 2");
        }
        int a = maybe ("test") {1, 2, 3, 4};
        Utils.debug("a = " + a);
        this.testMaybeVariable();
        this.logMaybe();
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    public void onResume() {
//        timerHandler.postDelayed(timerRunnable, 0);
        Button updateButton = (Button) getActivity().findViewById(R.id.update);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = (TextView) getActivity().findViewById(R.id.hello_world);
                String maybeText = maybe("String") {"Maybe String 1", "Maybe String 2"};
                textView.setText(maybeText);
                maybe("color") {
                    textView.setBackgroundColor(Color.RED);
                } or {
                    textView.setBackgroundColor(Color.YELLOW);
                } or {
                    textView.setBackgroundColor(Color.GREEN);
                } or {
                    textView.setBackgroundColor(Color.WHITE);
                }
            }
        });

        super.onResume();
    }

    public void onPause() {
//        timerHandler.removeCallbacks(timerRunnable);
        super.onPause();
    }


    public void testMaybeVariable() {
        int choice = maybeService.get("test");
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
        maybeService.log("123", logging);
    }
}
