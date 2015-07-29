package org.phone_lab.maybe.library;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;
import org.phone_lab.maybe.library.MaybeService;
import org.phone_lab.maybe.library.utils.Constants;
import org.phone_lab.maybe.library.utils.Utils;
<<<<<<< HEAD

=======
>>>>>>> 9750182eaa23c2ca5d6dc67ed4d2d45305ab5a02

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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
    private static String MAYBE_SERVER_URL_LOG = "https://maybe.xcv58.me/maybe-api-v1/logs/";
    SharedPreferences prefs = getApplicationContext().getSharedPreferences("CacheFile", MODE_PRIVATE);
    @Override
    protected void onHandleIntent(Intent intent) {
        Utils.debug("onHandleIntent of Log");
        if (intent != null) {
            final String action = intent.getAction();
            if(ACTION_LOG.equals(action)) {
                maybeService = MaybeService.getInstance(getApplicationContext());
                String fileName =   prefs.getString("PreviousCache", null);
                if (fileName!= null) {
                    String cacheFile = intent.getStringExtra(fileName);
                    Utils.debug(cacheFile + "cacheFile :" + cacheFile.toString());
                    Uri fileUri = intent.getData();
                    File localFile = new File(fileUri.getPath());
                    int sendCounter = 2;
                    JSONObject responseJSON;
                    try {
                        BufferedReader br = new BufferedReader(
                                new FileReader(localFile));
                        StringBuilder allLines = new StringBuilder();
                        String line = "";
                        while( (line = br.readLine()) != null) {
                            allLines.append(line);
                        }
                        line = "["+allLines.toString()+"]";
                        JSONObject updatejsonObject = new JSONObject(line);
                        responseJSON = post(updatejsonObject);
                        int responseCode = responseJSON.getInt(Constants.RESPONSE_CODE);
                        while (sendCounter > 0 && responseCode != Constants.STATUS_CREATED) {
                            Utils.debug("POST failed, now retrying: " + updatejsonObject.toString());
                            responseJSON = post(updatejsonObject);
                            sendCounter--;
                            responseCode = responseJSON.getInt(Constants.RESPONSE_CODE);
                        }
                        if(sendCounter == 0) {
                            Utils.debug("POST failed: " + updatejsonObject.toString());
                        } else {
                            Utils.debug("POST success: " + updatejsonObject.toString());
                        }
                        //delete cache file after upload
                        if(localFile.delete()) {
                            Utils.debug(localFile + "Deleted :" + localFile.toString());
                        } else {
                            Utils.debug(localFile + "Not deleted :" + localFile.toString());
                        }

                    } catch ( JSONException | IOException e) {
                        e.printStackTrace();
                    }
                }
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
