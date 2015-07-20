package org.phone_lab.maybe.library;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import org.json.JSONObject;
import org.phone_lab.maybe.library.MaybeService;
import org.phone_lab.maybe.library.utils.Constants;
import org.phone_lab.maybe.library.utils.Utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 * Created by ramyarao on 7/13/15.
 */
public class LogIntentService extends IntentService {

    private static final String ACTION_LOG = "maybe.phone_lab.org.maybelibrary.action.LOG";
    private MaybeService maybeService;
    private static String MAYBE_SERVER_URL_LOG = "https://maybe.xcv58.me/maybe-api-v1/logs/";
    SharedPreferences prefs = getApplicationContext().getSharedPreferences("CacheFile", MODE_PRIVATE);
    @Override
    protected void onHandleIntent(Intent intent) {
        Utils.debug("onHandleIntent of Log");
        if (intent != null) {
            final String action = intent.getAction();
            if(ACTION_LOG.equals(action)) {
                maybeService = MaybeService.getInstance(getApplicationContext());
                //TODO : POST Operation + cache file deletion
                //TODO : retry post 3 time
                String fileName =   prefs.getString("PreviousCache", null);
                if (fileName!= null) {
                    String cacheFile = intent.getStringExtra(fileName);
                    Uri fileUri = intent.getData();
                    new File(fileUri.getPath());
                    int sendCounter = 2;
                    JSONObject responseJSON,deviceJSONObject;
                    //todo: 1.post,
                    //todo: 2.failure handling of post,
                    //todo: 3.delete local cache
                }

//                   JSONObject responseJSON = post(deviceJSONObject);
//                    int sendCounter = 3;
//                    int responseCode = responseJSON.getInt(Constants.RESPONSE_CODE);
//                    while (sendCounter > 0 && responseCode != Constants.STATUS_CREATED) {
//                        Utils.debug("POST failed, now retrying: " + deviceJSONObject.toString());
//                        responseJSON = post(deviceJSONObject);
//                        sendCounter--;
//                        responseCode = responseJSON.getInt(Constants.RESPONSE_CODE);
//                    }
//                    if(sendCounter == 0) {
//                        Utils.debug("POST failed: " + deviceJSONObject.toString());
//                    } else {
//                        Utils.debug("POST success: " + deviceJSONObject.toString());
//                    }
//                                    //delete cache file after upload
//                    Boolean bFileDeleted = getApplicationContext().deleteFile(localCache);
//                    Utils.debug(localCache + "deleted :" + bFileDeleted);

            }
        }
    }

    private JSONObject post(JSONObject deviceJSONObject) {
        Utils.debug("POST to " + MAYBE_SERVER_URL_LOG + " -d " + deviceJSONObject.toString());
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

    public LogIntentService() {
        super("QueryIntentService");
    }

}
