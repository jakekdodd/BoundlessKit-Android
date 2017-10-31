package com.usedopamine.dopaminekit;

/**
 * Created by cuddergambino on 6/1/16.
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings.Secure;
import android.util.Log;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

class DopamineRequest extends AsyncTask<Void, Void, String> {

    public String resultData = "";
    public int status = 200;

    public enum RequestType{
        OTHER(""),
        TRACK("track/"),
        REINFORCE("reinforce/")
        ;

        private final String value;
        private RequestType(final String value){
            this.value = value;
        }

    }


    private Context context = null;
    private RequestType type = null;
    private DopamineKit.ReinforcementCallback callback = null;

    final String DopamineAPIURL = "https://api.usedopamine.com/v3/app/";
    private JSONObject requestData = null;
    private String value_clientSDKVersion = "3.1.02";
    private String key_APPID = "appID";
    private String key_DEVSECRET = "developmentSecret";
    private String key_PROSECRET = "productionSecret";
    private String key_INPRO = "inProduction";

    DopamineRequest(Context c, RequestType type, DopamineKit.ReinforcementCallback callback){
        this.context = c;
        this.type = type;
        this.callback = callback;
        setBaseRequestData();
    }

    /**
     * Add a key-value pair to {@code requestData}
     * @param key	The key to be put in
     * @param value	The value to be put in
     * @return		Returns itself for chaining
     */
    public DopamineRequest addData(String key, String value){
        try {
            requestData.put(key, value);
        } catch(JSONException e){
            e.printStackTrace();
            Log.v("DopamineKit", "invalid add to request data:" + key + "->" + value);
        }
        return this;
    }

    /**
     * Add a set of key-value pairs, contained in a Map, to {@code requestData}
     * @param topKey	The key for the entire Map
     * @param map		The set of entries to be put in
     * @return			Returns itself for chaining
     */
    public DopamineRequest addData(String topKey, Map<String,String> map){
        try {
            JSONObject topValue = new JSONObject();
            for(Map.Entry<String, String> entry : map.entrySet()) {
                topValue.put(entry.getKey(), entry.getValue());
            }
            requestData.put(topKey, topValue);
        } catch(JSONException e){
            e.printStackTrace();
            Log.v("DopamineKit", "invalid add to request data:" + topKey);
        }
        return this;
    }

    /**
     * Prints the {@code requestData} to be sent
     */
    public void printData(){ try { Log.v("DopamineKit requestData", this.requestData.toString(4)); } catch(JSONException e){ }
    }

    /**
     * Addes the local and UTC time to {@code requestData}
     */
    void addTime(){
        try {
            long utcTime = System.currentTimeMillis();
            long localTime = utcTime + TimeZone.getDefault().getOffset(utcTime);
            requestData.put("UTC", utcTime);
            requestData.put("localTime", localTime);
        } catch(JSONException e){
            e.printStackTrace();
            Log.v("DopamineKit", "Error - cannot add time to requestData");
        }
    }

    private void setBaseRequestData(){
        if(requestData != null)
            return;

        requestData = new JSONObject();

        // Reading from the file res/raw/dopamineproperties.json

        int credentialRes = context.getResources().getIdentifier("dopamineproperties", "raw", context.getPackageName());
        if(credentialRes != 0){
            if(DopamineKit.debugMode) Log.v("DopamineKit", "Found dopamineproperties.json");
        } else{
            Log.v("DopamineKit", "Couldn't find dopamineproperties.json");
            return;
        }
        InputStream inputStream = context.getResources().openRawResource(credentialRes);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int character;
        try{
            character = inputStream.read();
            while( character != -1){
                byteArrayOutputStream.write(character);
                character = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e){
            System.out.println("DopamineKit: Error - cannot find dopamineproperties.json");
            e.printStackTrace();
        }

        if(DopamineKit.debugMode) Log.v("DopamineKit", "property file" + byteArrayOutputStream.toString());

        try{
            // Data independent of json file but in here because throws JSONException
            requestData.put("clientOS", "Android");
            requestData.put("clientOSVersion", android.os.Build.VERSION.SDK_INT);
            requestData.put("clientSDKVersion", value_clientSDKVersion);

            // File as JSONObject
            JSONObject jsonObject = new JSONObject( byteArrayOutputStream.toString() );

            // AppID
            requestData.put(key_APPID, jsonObject.getString(key_APPID));

            // Secret
            if( jsonObject.getBoolean(key_INPRO) ){
                requestData.put("secret", jsonObject.getString(key_PROSECRET));
            } else{
                requestData.put("secret", jsonObject.getString(key_DEVSECRET));
            }

            // VersionID
            requestData.put("versionID", jsonObject.getString("versionID"));

            // Primary Identity
            requestData.put("primaryIdentity", Secure.getString(context.getContentResolver(), Secure.ANDROID_ID));

        } catch (JSONException e){
            Log.v("DopamineKit", "Error - dopamineproperties.json not configured properly");
            e.printStackTrace();
        }

    }

    static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected String doInBackground(Void... params) {
        // add time to requestData
        addTime();
        try {
            OkHttpClient client = new OkHttpClient();

            RequestBody body = RequestBody.create(JSON, requestData.toString());
            Request request = new Request.Builder()
                    .url(DopamineAPIURL + type.value)
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();

            JSONObject rawData = new JSONObject( response.body().string() );
            if(DopamineKit.debugMode) Log.v("DopamineKit Request Response", rawData.toString());
            if(type == RequestType.REINFORCE) {
                status = rawData.getInt("status");
                resultData = rawData.getString("reinforcementDecision");
            } else {
                status = rawData.getInt("status");
                if(rawData.has("errors")){
                    resultData = rawData.getJSONArray("errors").toString();
                }
            }
        } catch (IOException e){
            e.printStackTrace();
            Log.v("DopamineKit Network Error", e.getMessage());
        } catch (JSONException e){
            e.printStackTrace();
            Log.v("DopamineKit Parse Error", e.getMessage());
        }



        return resultData;
    }

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);

        if(callback != null){
            callback.onReinforcement(response);
        }
    }


}