package com.usedopamine.dopaminekit.RESTfulAPI;

/**
 * Created by cuddergambino on 6/1/16.
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;

import com.usedopamine.dopaminekit.DopamineKit;
import com.usedopamine.dopaminekit.DopeAction;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

class DopamineAPIRequest extends AsyncTask<JSONObject, Void, JSONObject> {
    public enum RequestType{
        TRACK("track/"),
        REPORT("report/"),
        REFRESH("refresh/")
        ;

        private final String value;
        private RequestType(final String value){
            this.value = value;
        }

    }


    private Context context = null;
    private RequestType type = null;
    private DopamineAPIRequestCallback callbackHandler;

    DopamineAPIRequest(Context c, RequestType type, DopamineAPIRequestCallback handler){
        this.context = c;
        this.type = type;
        this.callbackHandler = handler;


    }

    @Override
    protected JSONObject doInBackground(JSONObject... payload) {
        try {
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), payload.toString());
            Request request = new Request.Builder()
                    .url(DopamineAPI.DopamineAPIURL + type.value)
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();

            JSONObject result = new JSONObject( response.body().string() );
            DopamineKit.debugLog("DopamineAPIRequest", "Request resulted in - " + result.toString());
            return result;
        } catch (IOException e){
            e.printStackTrace();
            Log.v("DopamineKit", "Network Error - " + e.getMessage());
            return new JSONObject();
        } catch (JSONException e){
            e.printStackTrace();
            Log.v("DopamineKit", "Parse Error - " + e.getMessage());
            return new JSONObject();
        }

    }

    @Override
    protected void onPostExecute(JSONObject response) {
        super.onPostExecute(response);

        if(callbackHandler != null){
            callbackHandler.onDopamineAPIRequestPostExecute(response);
        }
    }


}