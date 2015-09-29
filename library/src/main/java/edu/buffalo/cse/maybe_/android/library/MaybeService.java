package edu.buffalo.cse.maybe_.android.library;

import android.app.AlarmManager;
import android.app.PendingIntent;
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
import edu.buffalo.cse.maybe_.android.library.rest.LogResponse;
import edu.buffalo.cse.maybe_.android.library.rest.MaybeLog;
import edu.buffalo.cse.maybe_.android.library.rest.MaybeRESTService;
import edu.buffalo.cse.maybe_.android.library.rest.PackageChoices;
import edu.buffalo.cse.maybe_.android.library.rest.ServiceFactory;
import edu.buffalo.cse.maybe_.android.library.services.LogIntentService;
import edu.buffalo.cse.maybe_.android.library.utils.Constants;
import edu.buffalo.cse.maybe_.android.library.utils.Utils;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
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
    private static int label_count = 0;
    private static final long MAX_SIZE = 10;

    public static MaybeService getInstance(Context context, boolean needSync) {
        if (maybeService == null) {
            Utils.debug("maybeService is null, init it!");
            synchronized (MaybeService.class) {
                // Using the context-application instead of a context-activity to avoid memory leak
                // http://android-developers.blogspot.co.il/2009/01/avoiding-memory-leaks.html
                maybeService = new MaybeService(context.getApplicationContext(), needSync);
            }
        } else {
            if (needSync) {
                Utils.debug("maybeService already exist, but still sync because needSync is true!");
                maybeService.syncWithBackend();
            } else {
                Utils.debug("maybeService already exist, just return!");
            }
        }
        return maybeService;
    }

    public static MaybeService getInstance(Context context) {
        if (maybeService == null) {
            Utils.debug("maybeService is null, init it!");
            synchronized (MaybeService.class) {
                // Using the context-application instead of a context-activity to avoid memory leak
                // http://android-developers.blogspot.co.il/2009/01/avoiding-memory-leaks.html
                maybeService = new MaybeService(context.getApplicationContext(), false);
            }
        } else {
            Utils.debug("maybeService already exist, just return!");
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

    private MaybeService(Context context, boolean needSync) {
        mContext = context;
        // get deviceid
        this.getDeviceMEID();
        // TODO: change implementation to get Android package name
        this.setPackageName();
        if (needSync) {
            // Background sync task, so don't load
            this.syncWithBackend();
        } else {
            this.load();
            this.setRepeatPull(mContext);
//            if (!this.hasPeriodicTask()) {
//                this.periodicTask();
//            } else {
//                Utils.debug("Periodic Task already exist!");
//            }
        }
        // DONE: load local variables SYNC
        // DONE: load mDevice
        // DONE: connect to Google Cloud Messaging ASYNC
        // DONE: then connect to server ASYNC
    }

    final MaybeRESTService maybeRESTService = ServiceFactory.createRetrofitService(MaybeRESTService.class, Constants.BASE_URL);

    Observable<String> observableGCMID = Observable.create(new Observable.OnSubscribe<String>() {
        @Override
        public void call(Subscriber<? super String> subscriber) {
            SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
            registrationId = sharedPreferences.getString(Constants.SHARED_PREFERENCE_GCM_ID, Constants.NO_REGISTRATION_ID);

            if (registrationId.equals(Constants.NO_REGISTRATION_ID)) {
                if (checkPlayServices()) {
                    InstanceID instanceID = InstanceID.getInstance(mContext);
                    try {
                        registrationId = instanceID.getToken(Constants.SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE);
                        if (registrationId.equals(Constants.NO_REGISTRATION_ID)) {
                            Utils.debug("register Google Cloud Messaging failed!");
                        } else {
                            Utils.debug("registration id: " + registrationId);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(Constants.SHARED_PREFERENCE_GCM_ID, registrationId);
                            if (!editor.commit()) {
                                Utils.debug("Update registrationId failed!");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        subscriber.onError(e);
                    }
                }
            }
            subscriber.onNext(registrationId);
            subscriber.onCompleted();
        }
    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

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
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public void syncWithBackend() {
        if (!hasActiveNetwork()) {
            Utils.debug("No network connection, cancel syncWithBackend()");
            return;
        }

        Observable<Device> zip = Observable.zip(observableGCMID, maybeRESTService.getDevice(mDeviceMEID), new Func2<String, List<Device>, Device>() {
                    @Override
                    public Device call(String s, List<Device> devices) {
                        return devices.size() > 0 ? devices.get(0) : null;
                    }
                }
        );

        Observable<Device> serverDevice = zip.flatMap(new Func1<Device, Observable<Device>>() {
            @Override
            public Observable<Device> call(Device device) {
                if (device == null || !mDeviceMEID.equals(device.deviceid)) {
                    // DONE: POST and log
                    if (device != null) {
                        Utils.debug("The device.deviceid " + device.deviceid + " is not equals with mDeviceMEID: " + mDeviceMEID);
                    }
                    device = new Device();
                    device.deviceid = mDeviceMEID;
                    if (registrationId != Constants.NO_REGISTRATION_ID) {
                        device.gcmid = registrationId;
                    }
                    Utils.debug("POST device: " + new Gson().toJson(device));
                    return maybeRESTService.postDevice(device);
                }
                if (!registrationId.equals(Constants.NO_REGISTRATION_ID) && !registrationId.equals(device.gcmid)) {
                    // TODO: PUT and log
                    Utils.debug("GET result with conflict gcmid, try PUT. Local gcmid: " + registrationId + " server gcmid: " + device.gcmid);
                    device.gcmid = registrationId;
                    Utils.debug("PUT device: " + new Gson().toJson(device));
                    return maybeRESTService.putDevice(mDeviceMEID, device);
                }
                // No local gcm id, just return
                Utils.debug("Just return GET result!");
                return Observable.just(device);
            }
        });

        serverDevice.subscribe(new Subscriber<Device>() {
            @Override
            public void onCompleted() {
                Utils.debug("serverDevice onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Utils.debug("serverDevice onError: " + e.getMessage());
            }

            @Override
            public void onNext(Device device) {
                flush(device);
                Utils.debug("serverDevice onNext, device: " + new Gson().toJson(device));
            }
        });
    }

    private void setPackageName() {
        this.packageName = mContext.getPackageName();
    }

    private String getDeviceMEID() {
        if (mDeviceMEID == null) {
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            mDeviceMEID = tm.getDeviceId();
            Utils.debug("getDeviceMEID() return: " + mDeviceMEID);
        }
        return mDeviceMEID;
    }

    private boolean flush(Device device) {
        mDevice = device;
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.SHARED_PREFERENCE_KEY, new Gson().toJson(mDevice));
        Utils.debug("flush mDevice to cache.");
        return editor.commit();
    }

    private void load() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        String jsonString = sharedPreferences.getString(Constants.SHARED_PREFERENCE_KEY, "");
        Utils.debug("Load from SharedPreferences: " + jsonString);
        mDevice = new Gson().fromJson(jsonString, Device.class);
    }

    public void setRepeatPull(Context context) {
        Intent intent = new Intent(Constants.PULL_INTENT);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent == null) {
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Utils.debug("Set repeat pull for every " + (Constants.PULL_INTERVAL / 1000) + " seconds");
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), Constants.PULL_INTERVAL, pendingIntent);
        } else {
            Utils.debug("PULL PendingIntent already exist, cancel setRepeatPull!");
        }
    }

    public int get(String label) {
        if (mDevice == null) {
            Utils.debug("Call get(" + label + ") before service is ready!");
            return Constants.DEFAULT_CHOICE;
        }
        if (mDevice.choices == null) {
            Utils.debug("mDevice.choices is null: " + new Gson().toJson(mDevice));
            return Constants.DEFAULT_CHOICE;
        }
        PackageChoices choices = mDevice.choices.get(packageName);
        if (choices == null) {
            Utils.debug("No PackageChoices for package: " + packageName + " from mDevice: " + new Gson().toJson(mDevice));
            return Constants.DEFAULT_CHOICE;
        }
        if (choices.labelJSON == null) {
            Utils.debug("mDevice.choices.labelJSON is null: " + new Gson().toJson(mDevice));
            return Constants.DEFAULT_CHOICE;
        }
        Choice choice = choices.labelJSON.get(label);
        if (choice == null) {
            Utils.debug("mDevice.choices.labelJSON.label is null: " + new Gson().toJson(mDevice));
            return Constants.DEFAULT_CHOICE;
        }
        Utils.debug("get(" + label + ") = " + choice.choice);
        return choice.choice;
    }

    public void log(String label, JSONObject logObject) {
        // 1. cache mechanism
        // TODO: cache to file
        // TODO: periodic task to update

        // 2. real-time mechanism
        // TODO: just POST
        final MaybeLog maybeLog = new MaybeLog(label, logObject, mContext);
        Observable<LogResponse> postLog = maybeRESTService.postLog(mDeviceMEID, packageName, maybeLog)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        postLog.subscribe(new Subscriber<LogResponse>() {
            @Override
            public void onCompleted() {
                Gson gson = new Gson();
                Utils.debug("postLog onCompleted: " + gson.toJson(maybeLog));
            }

            @Override
            public void onError(Throwable e) {
                Utils.debug("postLog onError: " + e.getMessage());
            }

            @Override
            public void onNext(LogResponse logResponse) {
                Gson gson = new Gson();
                Utils.debug("postLog onNext: " + gson.toJson(logResponse));
            }
        });
    }
}
