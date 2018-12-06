package kit.boundless.internal.data;

import java.util.TimeZone;

import android.support.annotation.Nullable;
import org.json.JSONObject;

/**
 * Created by cuddergambino on 7/17/16.
 */

public class BoundlessAction {

  final public static String NEUTRAL_DECISION = "neutralResponse";

  public String actionId;
  public @Nullable
  String cartridgeId = null;
  public @Nullable
  String reinforcementDecision = null;
  public @Nullable
  JSONObject metaData = null;
  public long utc;
  public long timezoneOffset;

  public BoundlessAction(
      String actionId, @Nullable String reinforcementDecision, @Nullable JSONObject metaData) {
    this(
        actionId,
        reinforcementDecision,
        metaData,
        System.currentTimeMillis(),
        TimeZone.getDefault().getOffset(System.currentTimeMillis())
    );
  }

  public BoundlessAction(
      String actionId,
      @Nullable String reinforcementDecision,
      @Nullable JSONObject metaData,
      long utc,
      long timezoneOffset) {
    this.actionId = actionId;
    this.reinforcementDecision = reinforcementDecision;
    this.metaData = metaData;
    this.utc = utc;
    this.timezoneOffset = timezoneOffset;
  }

}
