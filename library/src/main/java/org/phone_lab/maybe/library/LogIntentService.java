package org.phone_lab.maybe.library;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.telephony.TelephonyManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.phone_lab.maybe.library.utils.Constants;
import org.phone_lab.maybe.library.utils.Utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 * Created by ramyarao on 7/13/15.
 */
public class LogIntentService extends IntentService {

    private static final String ACTION_LOG = "maybe.phone_lab.org.maybelibrary.action.LOG";
    private MaybeService maybeService;
    private String mDeviceMEID;
    private static String MAYBE_SERVER_URL_LOG = "https://maybe.xcv58.me/maybe-api-v1/logs/";

    @Override
    protected void onHandleIntent(Intent intent) {
        mDeviceMEID = this.getDeviceMEID();
        MAYBE_SERVER_URL_LOG = MAYBE_SERVER_URL_LOG+ mDeviceMEID+ "/testing_inputs.maybe";
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("CacheFile", MODE_PRIVATE);
        Utils.debug("Shared prefs =" +prefs);
        if (intent != null) {
            final String action = intent.getAction();
            if(ACTION_LOG.equals(action)) {
                maybeService = MaybeService.getInstance(getApplicationContext());
                String fileName =   prefs.getString("PreviousCache", null);
                if (fileName!= null) {
                    Uri fileUri = (Uri)intent.getExtras().get(Intent.EXTRA_STREAM);
                    File localFile = new File(fileUri.getPath());
                    int sendCounter = 2;
                    JSONObject responseJSON;
                    try {
                        BufferedReader br = new BufferedReader(
                                new FileReader(localFile));
                        String line = "";
                        JSONArray logJSONArray = new JSONArray();
                        while((line = br.readLine()) != null) {
                            Utils.debug(" line = " + line);
                            JSONObject logJSONObject = new JSONObject(line);
                            logJSONArray.put(logJSONObject);
                        }
                        long timeElapsed = System.currentTimeMillis();
                        String label = "1";
                        JSONObject updatejsonObject = new JSONObject();
                        updatejsonObject.put("timestamp",timeElapsed);
                        updatejsonObject.put("label",label);
                        updatejsonObject.put("logObject",logJSONArray);
                        responseJSON = post(updatejsonObject);
                        int responseCode = responseJSON.getInt(Constants.RESPONSE_CODE);
                        while (sendCounter > 0 && responseCode != Constants.STATUS_CREATED) {
                            sendCounter--;
                            responseCode = responseJSON.getInt(Constants.RESPONSE_CODE);
                        }
                        if(sendCounter == 0) {
                            Utils.debug("POST failed: " + updatejsonObject.toString());
                        } else {
                            Utils.debug("POST Success: " + updatejsonObject.toString());
                        }
                        //delete cache file after upload
                        if(localFile.delete()) {
                            Utils.debug("Deleted :" + localFile.toString());
                        } else {
                            Utils.debug("Not deleted :" + localFile.toString());
                        }
                    } catch ( JSONException |IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private JSONObject post(JSONObject deviceJSONObject) {
        Utils.debug("POST to " + MAYBE_SERVER_URL_LOG +" "+deviceJSONObject.toString());
        HttpURLConnection connection = null;
        JSONObject postResponseJSON = null;
        try {
            URL url = new URL(MAYBE_SERVER_URL_LOG);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            connection.setDoOutput(true);
            connection.setChunkedStreamingMode(0);

            OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(connection.getOutputStream()));
            writer.write(deviceJSONObject.toString());
            writer.close();

            postResponseJSON = Utils.getResponseJSONObject(connection);
            Utils.debug("POST response: " + postResponseJSON.toString());
        } catch (Exception e) {
            Utils.debug(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return postResponseJSON;
    }

    private String getDeviceMEID() {
        if (mDeviceMEID == null) {
            TelephonyManager tm = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            mDeviceMEID = tm.getDeviceId();
            Utils.debug("getDeviceMEID() return: " + mDeviceMEID);
        }
        return mDeviceMEID;
    }

    public LogIntentService() {
        super("LogIntentService");
    }

}
