package org.coge.api;

import java.io.IOException;
import java.util.Iterator;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 * Wraps a JSON error message returned by the CoGe REST API.
 */
public class CoGeException extends Exception {

    /**
     * Instantiate from a string.
     */
    CoGeException(String error) {
        super(error);
    }
    
    /**
     * Instantiate from a JSON error message.
     */
    CoGeException(JSONObject json) throws JSONException {
        super(getErrorMessage(json));
    }

    /**
     * Instantiate from an IOException containing the JSON error message.
     */
    CoGeException(IOException ex) throws JSONException {
        super(getErrorMessage(ex));
    }

    /**
     * Return an exception for missing auth parameters.
     */
    static CoGeException missingAuthException() {
        return new CoGeException("Error: username and/or token not supplied. Data Store requests require authentication.");
    }

    /**
     * Return a nicely readable error message from an JSONObject containing "error".
     */
    static String getErrorMessage(JSONObject json) throws JSONException {
        JSONObject error = json.getJSONObject("error");
        Iterator<String> keys = error.keys();
        String errorType = keys.next();
        String errorReason = error.getString(errorType);
        return errorType+": "+errorReason;
    }

    /**
     * Return a nicely readable error message from an IOException containing the error JSON.
     */
    static String getErrorMessage(IOException ex) throws JSONException {
        String[] parts = ex.getMessage().split("\n");
        JSONObject json = new JSONObject(parts[1]);
        JSONObject error = json.getJSONObject("error");
        Iterator<String> keys = error.keys();
        String errorType = keys.next();
        String errorReason = error.getString(errorType);
        return errorType+":"+errorReason;
    }
    
}
