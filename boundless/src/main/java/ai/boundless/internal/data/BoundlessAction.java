package ai.boundless.internal.data;

import java.util.TimeZone;

import android.support.annotation.Nullable;
import org.json.JSONObject;

/**
 * Created by cuddergambino on 7/17/16.
 */
public class BoundlessAction {
  static final String NEUTRAL_DECISION = "neutralResponse";

  /**
   * The Action id.
   */
  public String actionId;

  @Nullable
  String cartridgeId;

  @Nullable
  String reinforcementDecision;

  /**
   * The Meta data.
   */
  @Nullable
  public JSONObject metaData;

  /**
   * The Utc.
   */
  public long utc;
  /**
   * The Timezone offset.
   */
  public long timezoneOffset;

  /**
   * Instantiates a new Boundless action.
   *
   * @param actionId the action id
   * @param reinforcementDecision the reinforcement decision
   * @param metaData the meta data
   */
  public BoundlessAction(
      String actionId, @Nullable String reinforcementDecision, @Nullable JSONObject metaData) {
    this.actionId = actionId;
    this.reinforcementDecision = reinforcementDecision;
    this.metaData = metaData;
    utc = System.currentTimeMillis();
    timezoneOffset = TimeZone.getDefault().getOffset(System.currentTimeMillis());
  }
}
