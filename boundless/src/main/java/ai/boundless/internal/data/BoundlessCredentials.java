package ai.boundless.internal.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TimeZone;

import ai.boundless.BoundlessKit;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build.VERSION;
import android.support.annotation.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The type Boundless credentials.
 */
public class BoundlessCredentials {

  private static final String CLIENT_OS = "Android";
  private static final String CLIENT_SDK_VERSION = "4.1.0";

  private final String clientBuild;

  private final String appId;

  private final String developmentSecret;

  private final String productionSecret;

  String versionId;

  private final boolean inProduction;

  @Nullable
  String primaryIdentity;

  private BoundlessCredentials(
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

    return valueOf(context, resourceString);
  }

  @Nullable
  private static BoundlessCredentials valueOf(Context context, String jsonString) {
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
   * As json object json object.
   *
   * @return the json object
   * @throws JSONException the json exception
   */
  public JSONObject asJsonObject() throws JSONException {
    JSONObject jsonObject = new JSONObject();

    jsonObject.put("clientOS", CLIENT_OS);
    jsonObject.put("clientOSVersion", VERSION.SDK_INT);
    jsonObject.put("clientBuild", clientBuild);
    jsonObject.put("clientSDKVersion", CLIENT_SDK_VERSION);

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
