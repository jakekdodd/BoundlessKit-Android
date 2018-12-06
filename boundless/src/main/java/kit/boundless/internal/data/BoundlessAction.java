package kit.boundless.internal.data;

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

  /**
   * The Cartridge id.
   */
  @Nullable
  public String cartridgeId = null;

  /**
   * The Reinforcement decision.
   */
  @Nullable
  public String reinforcementDecision = null;

  /**
   * The Meta data.
   */
  @Nullable
  public JSONObject metaData = null;

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
    this(
        actionId,
        reinforcementDecision,
        metaData,
        System.currentTimeMillis(),
        TimeZone.getDefault().getOffset(System.currentTimeMillis())
    );
  }

  /**
   * Instantiates a new Boundless action.
   *
   * @param actionId the action id
   * @param reinforcementDecision the reinforcement decision
   * @param metaData the meta data
   * @param utc the utc
   * @param timezoneOffset the timezone offset
   */
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
