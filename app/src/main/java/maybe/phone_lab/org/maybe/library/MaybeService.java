package maybe.phone_lab.org.maybe.library;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.telephony.TelephonyManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import maybe.phone_lab.org.maybe.library.utils.Constants;
import maybe.phone_lab.org.maybe.library.utils.Utils;

/**
 * Created by xcv58 on 5/8/15.
 */
public class MaybeService {
    private volatile static MaybeService maybeService;

    public static MaybeService getInstance(Context context) {
        if (maybeService == null) {
            Utils.debug("maybeService is null, init it!");
            synchronized (MaybeService.class) {
                maybeService = new MaybeService(context);
            }
        } else {
            Utils.debug("maybeService is not null, just return!");
        }
        return maybeService;
    }

    private class AsyncTask implements Runnable {

        @Override
        public void run() {
            // TODO: handle no internet, basically try to re-run below codes after fixed time.
            this.GCM();
            this.maybeServer();
        }

        private void maybeServer() {
            if (Constants.NO_REGISTRATION_ID == registrationId) {
                // TODO: connect with server with registrationId
            } else {
                // TODO: connect with server with NO registrationId
            }

            // TODO: Try GET, if not found (404) or , then POST with deviceID and registrationId (if have)
            // TODO: if something stale, then PUT to update
            JSONObject getResponseJSON = this.get();
            try {
                JSONObject deviceJSON = this.getDeviceJSON();
                int responseCode = getResponseJSON.getInt(Constants.RESPONSE_CODE);
                if (responseCode == Constants.STATUS_OK) {
                    // TODO: finish or put
                    JSONObject deviceChoicesJSONObject = (new JSONArray(getResponseJSON.getString(Constants.RESPONSE_CONTENT))).getJSONObject(0);
                    this.jsonToHashMap(deviceChoicesJSONObject);
                    if (!registrationId.equals(deviceChoicesJSONObject.getString(Constants.GCM_ID))) {
                        this.put(deviceJSON);
                    }
                    // server only return array
                } else if (responseCode == Constants.STATUS_NOT_FOUND) {
                    // TODO: post, if failed set timer to retry else just update
                    JSONObject postResponseJSONObject = this.post(deviceJSON);
                    responseCode = postResponseJSONObject.getInt(Constants.RESPONSE_CODE);
                    if (responseCode == Constants.STATUS_CREATED) {
                        JSONObject deviceChoicesJSONObject = new JSONObject(postResponseJSONObject.getString(Constants.RESPONSE_CONTENT));
                        this.jsonToHashMap(deviceChoicesJSONObject);
                    } else {
                        Utils.debug("POST failed: " + postResponseJSONObject.toString());
                    }
                } else {
                    Utils.debug(responseCode + " haven't supported yet!");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private JSONObject getDeviceJSON() throws JSONException {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.DEVICE_ID, mDeviceMEID);
            jsonObject.put(Constants.GCM_ID, registrationId);
//            if (!registrationId.equals(Constants.NO_REGISTRATION_ID)) {
//                jsonObject.put(Constants.GCM_ID, registrationId);
//            }
            return jsonObject;
        }

        private JSONObject get() {
            JSONObject getResponseJSON = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(MAYBE_SERVER_URL + mDeviceMEID);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                getResponseJSON  = Utils.getResponseJSONObject(connection);
                Utils.debug("GET response: " + getResponseJSON.toString());
            } catch (Exception e) {
                Utils.debug(e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return getResponseJSON;
        }

        private JSONObject post(JSONObject deviceJSONObject) {
            Utils.debug("POST to " + MAYBE_SERVER_URL + " -d " + deviceJSONObject.toString());
            HttpURLConnection connection = null;
            JSONObject postResponseJSON = null;
            try {
                URL url = new URL(MAYBE_SERVER_URL);
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

        private void put(JSONObject deviceJSONObject) {
            Utils.debug("PUT to " + MAYBE_SERVER_URL + mDeviceMEID + " -d " + deviceJSONObject.toString());
            HttpURLConnection connection = null;
            JSONObject putResponseJSON = null;
            try {
                URL url = new URL(MAYBE_SERVER_URL + mDeviceMEID);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");

                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");

                connection.setDoOutput(true);
                connection.setChunkedStreamingMode(0);

                OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(connection.getOutputStream()));
                writer.write(deviceJSONObject.toString());
                writer.close();

                putResponseJSON  = Utils.getResponseJSONObject(connection);
                Utils.debug("PUT response: " + putResponseJSON.toString());
            } catch (Exception e) {
                Utils.debug(e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        private void GCM() {
            if (registrationId != Constants.NO_REGISTRATION_ID) {
                Utils.debug("already register for GCM, skip it");
                return;
            }
            if (checkPlayServices()) {
                GoogleCloudMessaging googleCloudMessaging = GoogleCloudMessaging.getInstance(mContext);
                registrationId = this.registerInBackground(googleCloudMessaging);
                if (registrationId.equals(Constants.NO_REGISTRATION_ID)) {
                    Utils.debug("register Google Cloud Messaging failed!");
                } else {
                    Utils.debug("registration id: " + registrationId);
                }
            }
        }

        // TODO: store registrationId locally
//        private String getRegistrationId() {
//
//        }

        private void jsonToHashMap(JSONObject deviceChoiceJSONObject) {
            Utils.debug("response for device: " + deviceChoiceJSONObject.toString());
            try {
                JSONObject choicesJSONObject = deviceChoiceJSONObject.getJSONObject(Constants.CHOICES);
                JSONObject choiceForPackage = choicesJSONObject.getJSONObject(packageName);
                Utils.debug("choices for package " + packageName + ": " + choiceForPackage.toString());
                JSONObject labelJSONObject = choiceForPackage.getJSONObject(Constants.LABEL_JSON);
                Iterator<String> iterator = labelJSONObject.keys();
                while (iterator.hasNext()) {
                    String label = iterator.next();

                    JSONObject choiceJSONObject = labelJSONObject.getJSONObject(label);
                    int choice = choiceJSONObject.getInt(Constants.CHOICE);

                    Utils.debug(label + ": " + choice);
                    variableMap.put(label, choice);
                }
                flush();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private String registerInBackground(GoogleCloudMessaging googleCloudMessaging) {
            try {
                return googleCloudMessaging.register(SENDER_ID);
            } catch (IOException e) {
                Utils.debug(e);
                return Constants.NO_REGISTRATION_ID;
            }
        }

        private boolean checkPlayServices() {
            int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
            if (resultCode != ConnectionResult.SUCCESS) {
                Utils.debug("No Google Play Services!");
                return false;
            }
            return true;
        }

        public void log(JSONObject logJSONObject) {
            // TODO: 1. add device related data: device_id, timestamp, current battery status, etc.
            // TODO: 2. cache the logJSONObject
            // TODO: 3. batch upload
            //upload files to handle concurrency, handle resend.
            Utils.debug("input log JSON : " + logJSONObject.toString());
            try {
                JSONObject deviceJSONObject = new JSONObject();
                long timeElapsed = System.currentTimeMillis();
                float batteryLevel = getBatteryLevel();

                //deviceJSONObject.put("device_id",logJSONObject.getJSONObject(Constants.DEVICE_ID));
                deviceJSONObject.put("timestamp",timeElapsed);
                deviceJSONObject.put("batterystatus",batteryLevel);
                deviceJSONObject.put(Constants.DEVICE_ID, mDeviceMEID);

                //logic to save into file locally
                String localCache = "Local Cache" + label_count;
                label_count++;
                String toWriteLogString = deviceJSONObject.toString();
                FileOutputStream fos = mContext.openFileOutput(localCache, Context.MODE_PRIVATE);
                fos.write(toWriteLogString.getBytes());
                fos.close();

                //upload to server after max size limit is reached
                file_size = Integer.parseInt(String.valueOf(localCache.length()));
                if(file_size >= MAX_SIZE) {
                    JSONObject responseJSON = post(deviceJSONObject);
                    int responseCode = responseJSON.getInt(Constants.RESPONSE_CODE);
                    while (responseCode != Constants.STATUS_CREATED) {
                        Utils.debug("POST failed, now retrying: " + deviceJSONObject.toString());
                        responseJSON = post(deviceJSONObject);
                        responseCode = responseJSON.getInt(Constants.RESPONSE_CODE);
                    }
                    Utils.debug("POST success: " + deviceJSONObject.toString());
                    // delete cache file after upload
                    Boolean bFileDeleted = mContext.deleteFile(localCache);
                    Utils.debug(localCache + "deleted :" + bFileDeleted);
                }
            } catch (JSONException | IOException e ) {
                e.printStackTrace();
            }
        }

        public float getBatteryLevel() {
            Intent batteryIntent = mContext.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            return ((float) level / (float) scale) * 100.0f;
        }
    }

    private Context mContext;
    private String mDeviceMEID;
    private String packageName;
    private HashMap<String, Integer> variableMap;
    private JSONArray logJSONArray = new JSONArray();

    private static String registrationId = Constants.NO_REGISTRATION_ID;
    // TODO: add set method for url and sender id
    private static String MAYBE_SERVER_URL = "https://maybe.xcv58.me/maybe-api-v1/logs/";
    private static String SENDER_ID = "1068479230660";
    private static int label_count = 0;
    private static final long MAX_SIZE = 4096;
    private static float file_size = 0;


     private MaybeService(Context context) {
        mContext = context;
        // get MEID
        this.getDeviceMEID();
        // TODO: change implementation to get Android package name
        this.setPackageName();
        // DONE: load local variables SYNC
        this.initVariableMap();
        // TODO: connect to Google Cloud Messaging ASYNC
        // TODO: then connect to server ASYNC
        this.asyncTasks();
    }

    protected void asyncTasks() {
        Thread thread = new Thread(new AsyncTask());
        thread.start();
    }

    private void setPackageName() {
        this.packageName = "testing_inputs.maybe";
    }

    private String getDeviceMEID() {
        if(mDeviceMEID == null){
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            mDeviceMEID = tm.getDeviceId();
            Utils.debug("getDeviceMEID() return: " + mDeviceMEID);
        }
//        mDeviceMEID = "35823905271971";
        return mDeviceMEID;
    }

    private void initVariableMap() {
        if (this.variableMap == null) {
            try {
                FileInputStream fileInputStream = mContext.openFileInput(Constants.MAP_FILE);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

                Object object = objectInputStream.readObject();
                this.variableMap = (HashMap<String, Integer>) object;

                objectInputStream.close();
                fileInputStream.close();
            } catch (Exception e) {
                Utils.debug(e);
                this.variableMap = new HashMap<String, Integer>();
            }
        }
    }

    private void flush() {
        ObjectOutputStream objectOutputStream;
        try {
            objectOutputStream = new ObjectOutputStream(mContext.openFileOutput(Constants.MAP_FILE, Context.MODE_PRIVATE));

            objectOutputStream.writeObject(this.variableMap);

            objectOutputStream.close();
        } catch (Exception e) {
            Utils.debug(e);
        }
    }

    public int get(String label) {
        Integer choice = this.variableMap.get(label);
        if (choice == null) {
            choice = Constants.DEFAULT_CHOICE;
            this.variableMap.put(label, choice);
            this.flush();
        } else {
            this.variableMap.put(label, choice + 1);
            this.flush();
        }
        return choice;
    }

}
