package edu.buffalo.cse.maybe_.android.library;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.telephony.TelephonyManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import edu.buffalo.cse.maybe_.android.library.rest.Choice;
import edu.buffalo.cse.maybe_.android.library.rest.Device;
import edu.buffalo.cse.maybe_.android.library.rest.MaybeRESTService;
import edu.buffalo.cse.maybe_.android.library.rest.PackageChoices;
import edu.buffalo.cse.maybe_.android.library.rest.ServiceFactory;
import edu.buffalo.cse.maybe_.android.library.services.LogIntentService;
import edu.buffalo.cse.maybe_.android.library.utils.Constants;
import edu.buffalo.cse.maybe_.android.library.utils.Utils;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/*
 * Created by xcv58 on 5/8/15.
 */
public class MaybeService {

    private volatile static MaybeService maybeService;

    private Context mContext;

    private String mDeviceMEID;
    private String packageName;

    private Device mDevice;

    private static String registrationId = Constants.NO_REGISTRATION_ID;

    // TODO: add set method for url and sender id
    private static String SENDER_ID = "1068479230660";
    private static int label_count = 0;
    private static final long MAX_SIZE = 10;

    public static MaybeService getInstance(Context context) {
        if (maybeService == null) {
            Utils.debug("maybeService is null, init it!");
            synchronized (MaybeService.class) {
                // Using the context-application instead of a context-activity to avoid memory leak
                // http://android-developers.blogspot.co.il/2009/01/avoiding-memory-leaks.html
                maybeService = new MaybeService(context.getApplicationContext());
            }
        } else {
            Utils.debug("maybeService is not null, just return!");
        }
        return maybeService;
    }

    private class LogTask implements Runnable {

        public void run() {

            this.log(logJSONObject);
        }

        JSONObject logJSONObject;

        public LogTask(JSONObject logObject) {
            this.logJSONObject = logObject;
        }

        private void log(JSONObject logJSONObject) {

            Utils.debug("input log JSON : " + logJSONObject.toString());
            try {
                JSONObject deviceJSONObject = new JSONObject();
                long timeElapsed = System.currentTimeMillis();
                float batteryLevel = getBatteryLevel();
                deviceJSONObject.put("timestamp", timeElapsed);
                deviceJSONObject.put("batterystatus", batteryLevel);
                deviceJSONObject.put("logObject", logJSONObject);
                deviceJSONObject.put(Constants.DEVICE_ID, mDeviceMEID);

                //logic to save into file locally
                String localCache = "LocalCache" + label_count;
                Utils.debug("LocalCache Name = " + localCache);
                SharedPreferences.Editor editor = mContext.getSharedPreferences("CacheFile",
                        Context.MODE_PRIVATE).edit();
                editor.putString("PreviousCache",localCache);
                editor.commit();
                String toWriteLogString = deviceJSONObject.toString()+"\n";
                Utils.debug("Write Log = " + toWriteLogString);
                FileOutputStream fos = mContext.openFileOutput(localCache, Context.MODE_APPEND|Context.MODE_PRIVATE);
                fos.write(toWriteLogString.getBytes());
                fos.close();

//                //initiate GCM services for post
//                long periodSecs = 30L; // the task should be executed every 30 seconds
//                long flexSecs = 15L; //the task can run as early as -15 seconds from the scheduled time
//                String tag = "PeriodicLogUpdate"; // a unique task identifier
//                Utils.debug("Main Activity Context1 =" + mContext.getApplicationContext());
//                PeriodicTask periodic = new PeriodicTask.Builder()
//                .setService(GCMPeriodicLogUpdateService.class)
//                .setPeriod(periodSecs)
//                .setFlex(flexSecs)
//                .setTag(tag)
//                .setPersisted(true)
//                .setRequiredNetwork(com.google.android.gms.gcm.Task.NETWORK_STATE_CONNECTED)
//                .setRequiresCharging(true)
//                .build();
//                GcmNetworkManager.getInstance(mContext.getApplicationContext()).schedule(periodic);
//                Utils.debug("Main Activity Context2 =" + mContext.getApplicationContext());

                //upload to server after max size limit is reached
                float file_size = Integer.parseInt(String.valueOf(localCache.length()));
                Utils.debug("file_size now = " + file_size);
                if(file_size >= MAX_SIZE) {
                    Intent logIntent = new Intent(mContext,LogIntentService.class);
                    logIntent.setAction("edu.buffalo.cse.maybe.android.library.action.LOG");
                    File file = new File(mContext.getFilesDir().getAbsolutePath()+"/"+localCache);
                    logIntent.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(file));
                    mContext.startService(logIntent);
                    label_count++;
                }
            } catch (JSONException|IOException e) {
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

    private MaybeService(Context context) {
        mContext = context;
        // get deviceid
        this.getDeviceMEID();
        // TODO: change implementation to get Android package name
        this.setPackageName();
        // DONE: load local variables SYNC
        // DONE: load mDevice
        this.load();
        // DONE: connect to Google Cloud Messaging ASYNC
        // DONE: then connect to server ASYNC
        this.init();
    }

    private Observable<String> observableGCMID() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                if (registrationId.equals(Constants.NO_REGISTRATION_ID)) {
                    if (checkPlayServices()) {
                        InstanceID instanceID = InstanceID.getInstance(mContext);
                        try {
                            registrationId = instanceID.getToken(Constants.SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE);
                            if (registrationId.equals(Constants.NO_REGISTRATION_ID)) {
                                Utils.debug("register Google Cloud Messaging failed!");
                            } else {
                                Utils.debug("registration id: " + registrationId);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                subscriber.onNext(registrationId);
                subscriber.onCompleted();
            }
        });
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        if (resultCode != ConnectionResult.SUCCESS) {
            Utils.debug("No Google Play Services!");
            return false;
        }
        return true;
    }

    private boolean hasActiveNetwork() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public void init() {
        if (!hasActiveNetwork()) {
            Utils.debug("No network connection, cancel init()");
            return;
        }

        Observable<String> gcmID = this.observableGCMID()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
        gcmID.subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {
                Utils.debug("gcmID onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Utils.debug("gcmID onError: " + e.getMessage());
            }

            @Override
            public void onNext(String s) {
                Utils.debug("gcmID is: " + s);
                registrationId = s;
            }
        });


        MaybeRESTService maybeRESTService = ServiceFactory.createRetrofitService(MaybeRESTService.class, Constants.BASE_URL);

        Observable<List<Device>> get = maybeRESTService.getDevice(mDeviceMEID)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());

        get.subscribe(new Subscriber<List<Device>>() {
            @Override
            public void onCompleted() {
                Utils.debug("GET onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                // TODO: POST device
                Utils.debug("GET onError: " + e.getMessage());
                postDevice();
            }

            @Override
            public void onNext(List<Device> devices) {
                if (devices.size() == 0) {
                    Utils.debug("GET got 0 result, try POST it");
                    postDevice();
                    return;
                }
                Device device = devices.get(0);
                Utils.debug("GET: " + new Gson().toJson(device));
                if (registrationId.equals(device.gcmid)) {
                    Utils.debug("GET updated result, just finish");
                    finish(device);
                } else {
                    Utils.debug("GET result with conflict gcmid, try PUT");
                    putDevice(device);
                }
            }
        });
    }

    private void postDevice() {
        Device device = new Device();
        device.deviceid = mDeviceMEID;
        if (registrationId != Constants.NO_REGISTRATION_ID) {
            device.gcmid = registrationId;
        }
        Utils.debug("POST device: " + new Gson().toJson(device));
        MaybeRESTService maybeRESTService = ServiceFactory.createRetrofitService(MaybeRESTService.class, Constants.BASE_URL);
        Observable<Device> postDevice = maybeRESTService.postDevice(device);
        postDevice.subscribe(new Subscriber<Device>() {
            @Override
            public void onCompleted() {
                Utils.debug("POST onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Utils.debug("TODO POST onError: " + e.getMessage());
            }

            @Override
            public void onNext(Device device) {
                Utils.debug("POST result: " + new Gson().toJson(device));
                finish(device);
            }
        });
    }

    private void putDevice(Device device) {
        if (!device.deviceid.equals(mDeviceMEID)) {
            Utils.debug("The device.deviceid " + device.deviceid + " is not equals with mDeviceMEID: " + mDeviceMEID);
        }
        if (registrationId != Constants.NO_REGISTRATION_ID) {
            device.gcmid = registrationId;
        }
        Utils.debug("PUT device: " + new Gson().toJson(device));
        MaybeRESTService maybeRESTService = ServiceFactory.createRetrofitService(MaybeRESTService.class, Constants.BASE_URL);
        Observable<Device> putDevice = maybeRESTService.putDevice(mDeviceMEID, device);
        putDevice.subscribe(new Subscriber<Device>() {
            @Override
            public void onCompleted() {
                Utils.debug("PUT onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                Utils.debug("TODO PUT onError: " + e.getMessage());
            }

            @Override
            public void onNext(Device device) {
                Utils.debug("PUT result: " + new Gson().toJson(device));
                finish(device);
            }
        });
    }

    private void finish(Device device) {
        mDevice = device;
        Utils.debug("Finish with device: " + new Gson().toJson(mDevice));
        flush();
    }

    // TODO: store registrationId locally
//        private String getRegistrationId() {
//        }

    private void setPackageName() {
        this.packageName = mContext.getPackageName();
    }

    private String getDeviceMEID() {
        if (mDeviceMEID == null) {
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            mDeviceMEID = tm.getDeviceId();
            Utils.debug("getDeviceMEID() return: " + mDeviceMEID);
        }
//        mDeviceMEID = "35823905271971";
        return mDeviceMEID;
    }

    private void flush() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.SHARED_PREFERENCE_KEY, new Gson().toJson(mDevice));
        editor.apply();
    }

    private void load() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String jsonString = sharedPreferences.getString(Constants.SHARED_PREFERENCE_KEY, "");
        Utils.debug("Load from SharedPreferences: " + jsonString);
        mDevice = new Gson().fromJson(jsonString, Device.class);
    }

    public int get(String label) {
        if (mDevice == null) {
            Utils.debug("Call get(" + label + ") before service is ready!");
            return 0;
        }
        if (mDevice.choices == null) {
            Utils.debug("mDevice.choices is null: " + new Gson().toJson(mDevice));
            return 0;
        }
        PackageChoices choices = mDevice.choices.get(packageName);
        if (choices == null) {
            Utils.debug("No PackageChoices for package: " + packageName + " from mDevice: " + new Gson().toJson(mDevice));
            return 0;
        }
        if (choices.labelJSON == null) {
            Utils.debug("mDevice.choices.labelJSON is null: " + new Gson().toJson(mDevice));
            return 0;
        }
        Choice choice = choices.labelJSON.get(label);
        if (choice == null) {
            Utils.debug("mDevice.choices.labelJSON.label is null: " + new Gson().toJson(mDevice));
            return 0;
        }
        return choice.choice;
    }

    public void log(JSONObject logObject) {
        Thread thread = new Thread(new LogTask(logObject));
        thread.start();
    }
}
