package edu.buffalo.cse.maybe_.android.library.log;

import android.content.Context;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;

import edu.buffalo.cse.maybe_.android.library.rest.LogResponse;
import edu.buffalo.cse.maybe_.android.library.rest.MaybeLog;
import edu.buffalo.cse.maybe_.android.library.rest.MaybeRESTService;
import edu.buffalo.cse.maybe_.android.library.rest.ServiceFactory;
import edu.buffalo.cse.maybe_.android.library.utils.Constants;
import edu.buffalo.cse.maybe_.android.library.utils.Utils;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by xcv58 on 9/30/15.
 * Handle log/score related call for MaybeService.
 */
public class LogHandler {
    private static final String LCK_EXT = ".lck";
    private static final int DEFAULT_COUNT = 1;
    private static final int DEFAULT_LIMIT = 0;
    private static final boolean DEFAULT_APPEND = true;

    private Context mContext;
    private String deviceID;
    private String packageName;

    private final MaybeRESTService maybeRESTService = ServiceFactory.createRetrofitService(MaybeRESTService.class, Constants.BASE_URL);
    private final Gson gson = new Gson();

    private File currentFile;

    public LogHandler(Context context, String deviceID, String packageName) {
        this.mContext = context;
        this.deviceID = deviceID;
        this.packageName = packageName;
    }

    public void log(String label, JSONObject logObject) {
        final MaybeLog maybeLog = new MaybeLog(label, logObject, mContext);
        this.log(maybeLog);
    }

    public void close() {
        return;
    }

    private void log(MaybeLog maybeLog) {
        // 1. cache mechanism
        // TODO: cache to file
        // TODO: periodic task to update

        // 2. real-time mechanism
        // TODO: just POST
        Observable<LogResponse> postLog = maybeRESTService.postLog(deviceID, packageName, maybeLog)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        postLog.subscribe(new Subscriber<LogResponse>() {
            @Override
            public void onCompleted() {
                Utils.debug("postLog onCompleted");
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

    private void log(String logString) {
    }

    private void writeToFile() {
    }
}
