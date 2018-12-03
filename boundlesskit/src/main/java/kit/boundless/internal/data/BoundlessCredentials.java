package kit.boundless.internal.data;

import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TimeZone;

import kit.boundless.BoundlessKit;

public class BoundlessCredentials {

    final String clientOS = "Android";
    final int clientOSVersion = android.os.Build.VERSION.SDK_INT;
    final String clientBuild;
    final String clientSDKVersion = "4.1.0";

    final String appId;
    final String versionId;
    boolean inProduction;
    final String developmentSecret;
    final String productionSecret;
    String getSecret() {
        return inProduction ? productionSecret : developmentSecret;
    }
    String primaryIdentity;

    public BoundlessCredentials(Context context, String appId, String versionId, boolean inProduction, String developmentSecret, String productionSecret) {
        this.appId = appId;
        this.versionId = versionId;
        this.inProduction = inProduction;
        this.developmentSecret = developmentSecret;
        this.productionSecret = productionSecret;
        this.primaryIdentity = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String clientBuild = "UNKNOWNBUILD";
        try {
            clientBuild = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            BoundlessKit.debugLog("BoundlessCredentials", "Error - Could not retrieve client build version.");
            Telemetry.storeException(e);
        }
        this.clientBuild = clientBuild;
    }

    public JSONObject asJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("clientOS", clientOS);
        jsonObject.put("clientOSVersion", clientOSVersion);
        jsonObject.put("clientBuild", clientBuild);
        jsonObject.put("clientSDKVersion", clientSDKVersion);

        jsonObject.put("appId", appId);
        jsonObject.put("versionId", versionId);
        jsonObject.put("inProduction", inProduction);
        jsonObject.put("secret", inProduction ? productionSecret : developmentSecret);
        jsonObject.put("primaryIdentity", primaryIdentity);

        long utc = System.currentTimeMillis();
        jsonObject.put("utc", utc);
        jsonObject.put("timezoneOffset", TimeZone.getDefault().getOffset(utc));

        return jsonObject;
    }

    /**
     * Extracts credentials from the given JSON. Credentials are obtained from dashboard.boundless.ai
     *
     * @param context       Context
     * @param jsonString    A JSON formatted string
     * @return An object initiated with the values from file.
     */
    @Nullable
    public static BoundlessCredentials valueOf(Context context, String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            String appId = jsonObject.getString("appID");
            boolean inProduction = jsonObject.getBoolean("inProduction");
            String developmentSecret = jsonObject.getString("developmentSecret");
            String productionSecret = jsonObject.getString("productionSecret");
            String versionId = jsonObject.getString("versionID");
            return new BoundlessCredentials(context, appId, versionId, inProduction, developmentSecret, productionSecret);
        } catch (JSONException e) {
            e.printStackTrace();
            BoundlessKit.debugLog("BoundlessCredentials", "Error - invalid JSON string for credentials");
            Telemetry.storeException(e);
            return null;
        }
    }

    @Nullable
    public static BoundlessCredentials valueOf(Context context, int resourceId) {
        if (resourceId == 0) {
            BoundlessKit.debugLog("BoundlessCredentials", "No resource found for properties file.");
            return null;
        }

        String resourceString;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            InputStream inputStream = context.getResources().openRawResource(resourceId);
            for (int character = inputStream.read(); character != -1; character = inputStream.read()) {
                byteArrayOutputStream.write(character);
            }
            resourceString = byteArrayOutputStream.toString();
            inputStream.close();
            byteArrayOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            BoundlessKit.debugLog("BoundlessCredentials", "Could not read read file.");
            Telemetry.storeException(e);
            return null;
        }

        return BoundlessCredentials.valueOf(context, resourceString);
    }

}
