package com.usedopamine.dopaminekit.RESTfulAPI;

import org.json.JSONObject;

/**
 * Created by cuddergambino on 8/9/16.
 */

public interface DopamineAPIRequestCallback {
    /**
     * Called when an API request has completed.
     *
//     * @param statusCode The HTTP status code
     * @param response   The response from the API in an JSONObject
     */
    void onDopamineAPIRequestPostExecute(JSONObject response);
}
