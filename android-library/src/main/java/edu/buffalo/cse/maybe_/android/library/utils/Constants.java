package edu.buffalo.cse.maybe_.android.library.utils;

/**
 * Created by xcv58 on 5/21/15.
 */
public class Constants {
    public static final long PULL_INTERVAL = 1000L * 60L * 60L;
    public static final String PULL_INTENT = "edu.buffalo.cse.maybe_.android.library.pull_intent";

    // TODO: put this in external json file
    public static String BASE_URL = "http://maybe.cse.buffalo.edu/maybe-api-v1/";
    public static final String SENDER_ID = "1068479230660";

    public static final String TAG = "Maybe_Library";

    public static final String SHARED_PREFERENCE_NAME = "SHARED_PREFERENCE_NAME";
    public static final String SHARED_PREFERENCE_KEY = "SHARED_PREFERENCE_KEY";
    public static final String SHARED_PREFERENCE_GCM_ID = "SHARED_PREFERENCE_GCM_ID";

    public static final String NO_REGISTRATION_ID = "";

    public static final String DEFAULT_ENCODING = "UTF-8";

    public static final String EMPTY = "";

    public static final Integer DEFAULT_CHOICE = 0;
    public static final String ERR_NO_RECORDS_FOUND = "No Record(s) Found";
    public static final String ERR_DUPLICATE_KEY = "E11000 duplicate key error index";
    public static final String ERR_GENERIC_ERROR = "Error";
    public static final String ERR_JSON_ERROR = "JSON parse error";

    public static final String RESPONSE_CODE = "RESPONSE_CODE";
    public static final String RESPONSE_LENGTH = "RESPONSE_LENGTH";
    public static final String RESPONSE_MESSAGE = "RESPONSE_MESSAGE";
    public static final String RESPONSE_CONTENT = "RESPONSE_CONTENT";
    public static final String RESPONSE_ERROR = "RESPONSE_ERROR";

    public static final String DEVICE_ID = "deviceid";
    public static final String GCM_ID = "gcmid";
    public static final String CHOICES = "choices";
    public static final String LABEL_JSON = "labelJSON";
    public static final String CHOICE = "choice";

    public static final int STATUS_CREATED = 201;
}
