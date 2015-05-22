package maybe.phone_lab.org.maybelibrary.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

/**
 * Created by xcv58 on 5/6/15.
 */
public class Utils {
    public static boolean DEBUG = true;

    public static void debug(String log) {
        if (DEBUG) {
            Log.d(Constants.TAG, log);
        }
    }

    public static void debug(Exception e) {
        if (DEBUG) {
            Log.d(Constants.TAG, Log.getStackTraceString(e));
        }
    }

    public static String readFromInputStream(InputStream inputStream) {
        return Utils.readFromInputStream(inputStream, -1, Constants.DEFAULT_ENCODING);
    }

    public static String readFromInputStream(InputStream inputStream, String encoding) {
        return Utils.readFromInputStream(inputStream, -1, encoding);
    }

    public static String readFromInputStream(InputStream inputStream, int length) {
        return Utils.readFromInputStream(inputStream, length, Constants.DEFAULT_ENCODING);
    }

    public static String readFromInputStream(InputStream inputStream, int length, String encoding) {
        if (inputStream == null) {
            return Constants.EMPTY;
        }
        if (encoding == null) {
            encoding = Constants.DEFAULT_ENCODING;
        }
        if (length == -1) {
            StringBuilder stringBuilder = new StringBuilder();
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, encoding));
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    stringBuilder.append(line);
                }
                reader.close();
            } catch (IOException e) {
                Utils.debug(e);
            }
            return stringBuilder.toString();
        } else {
            byte[] bytes = new byte[length];
            try {
                inputStream.read(bytes, 0, length);
                return new String(bytes, encoding);
            } catch (IOException e) {
                Utils.debug(e);
                return Constants.EMPTY;
            }
        }
    }

    public static JSONObject getResponseJSONObject(HttpURLConnection connection) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        int responseCode = -1;
        InputStream inputStream = null;
        String responseMessage = null;

        try {
            responseCode = connection.getResponseCode();
            inputStream = connection.getInputStream();
            responseMessage = connection.getResponseMessage();
        } catch (IOException e) {
            inputStream = connection.getErrorStream();
        }
        int length = connection.getContentLength();

        jsonObject.put(Constants.RESPONSE_CODE, responseCode);
        jsonObject.put(Constants.RESPONSE_MESSAGE, responseMessage);
        jsonObject.put(Constants.RESPONSE_LENGTH, length);

//        InputStream errorStream = connection.getErrorStream();

        String content = Utils.readFromInputStream(inputStream, length, connection.getContentEncoding());
        jsonObject.put(Constants.RESPONSE_CONTENT, content);
//        String error = Utils.readFromInputStream(errorStream, length, connection.getContentEncoding());
//        jsonObject.put(Constants.RESPONSE_ERROR, error);

        return jsonObject;
    }
}
