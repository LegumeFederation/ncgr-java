package org.coge.api;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 * A simple wrapper for a JSON response from a PUT request.
 */
public class CoGeResponse {

    boolean success;
    int id;
    String siteURL;

    CoGeResponse(JSONObject json) throws JSONException {
        if (json.has("id")) id = json.getInt("id");
        if (json.has("success")) success = json.getBoolean("success");
        if (json.has("site_url")) siteURL = json.getString("site_url");
    }

    public int getId() {
        return id;
    }

    public boolean getSuccess() {
        return success;
    }

    public String getSiteURL() {
        return siteURL;
    }


}

                           
