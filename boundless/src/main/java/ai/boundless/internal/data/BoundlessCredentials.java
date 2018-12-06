package ai.boundless.internal.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TimeZone;

import ai.boundless.BoundlessKit;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The type Boundless credentials.
 */
public class BoundlessCredentials {

  /**
   * The Client os.
   */
  final String clientOs = "Android";
  /**
   * The Client os version.
   */
  final int clientOsVersion = android.os.Build.VERSION.SDK_INT;
  /**
   * The Client build.
   */
  final String clientBuild;
  /**
   * The Client sdk version.
   */
  final String clientSdkVersion = "4.1.0";

  /**
   * The App id.
   */
  final String appId;
  /**
   * The Development secret.
   */
  final String developmentSecret;
  /**
   * The Production secret.
   */
  final String productionSecret;
  /**
   * The Version id.
   */
  String versionId;
  /**
   * The In production.
   */
  boolean inProduction;
  /**
   * The Primary identity.
   */
  @Nullable
  String primaryIdentity;

  /**
   * Instantiates a new Boundless credentials.
   *
   * @param context the context
   * @param appId the app id
   * @param versionId the version id
   * @param inProduction the in production
   * @param developmentSecret the development secret
   * @param productionSecret the production secret
   */
  public BoundlessCredentials(
      Context context,
      String appId,
      String versionId,
      boolean inProduction,
      String developmentSecret,
      String productionSecret) {
    this.appId = appId;
    this.versionId = versionId;
    this.inProduction = inProduction;
    this.developmentSecret = developmentSecret;
    this.productionSecret = productionSecret;
    String clientBuild = "UNKNOWNBUILD";
    try {
      clientBuild =
          context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
      BoundlessKit.debugLog("BoundlessCredentials",
          "Error - Could not retrieve client build version."
      );
      Telemetry.storeException(e);
    }
    this.clientBuild = clientBuild;
  }

  /**
   * Value of boundless credentials.
   *
   * @param context the context
   * @param resourceId the resource id
   * @return the boundless credentials
   */
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

  /**
   * Extracts credentials from the given JSON. Credentials are obtained from dashboard.boundless.ai.
   *
   * @param context Context
   * @param jsonString A JSON formatted string
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
      return new BoundlessCredentials(context,
          appId,
          versionId,
          inProduction,
          developmentSecret,
          productionSecret
      );
    } catch (JSONException e) {
      e.printStackTrace();
      BoundlessKit.debugLog("BoundlessCredentials", "Error - invalid JSON string for credentials");
      Telemetry.storeException(e);
      return null;
    }
  }

  /**
   * Gets secret.
   *
   * @return the secret
   */
  String getSecret() {
    return inProduction ? productionSecret : developmentSecret;
  }

  /**
   * As json object json object.
   *
   * @return the json object
   * @throws JSONException the json exception
   */
  public JSONObject asJsonObject() throws JSONException {
    JSONObject jsonObject = new JSONObject();

    jsonObject.put("clientOS", clientOs);
    jsonObject.put("clientOSVersion", clientOsVersion);
    jsonObject.put("clientBuild", clientBuild);
    jsonObject.put("clientSDKVersion", clientSdkVersion);

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

}
