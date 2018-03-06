package boundless.boundlesskit.Synchronization;

import android.support.annotation.Nullable;

import org.json.JSONObject;

import java.util.TimeZone;

/**
 * Created by cuddergambino on 7/17/16.
 */

public class DopeAction {

    public String actionID;
    public @Nullable String reinforcementDecision = null;
    public @Nullable JSONObject metaData = null;
    public long utc;
    public long timezoneOffset;

    public DopeAction(String actionID, @Nullable String reinforcementDecision, @Nullable JSONObject metaData, long utc, long timezoneOffset) {
        this.actionID = actionID;
        this.reinforcementDecision = reinforcementDecision;
        this.metaData = metaData;
        this.utc = utc;
        this.timezoneOffset = timezoneOffset;
    }

    public DopeAction(String actionID, @Nullable String reinforcementDecision, @Nullable  JSONObject metaData) {
        this(actionID, reinforcementDecision, metaData, System.currentTimeMillis(), TimeZone.getDefault().getOffset(System.currentTimeMillis()));
    }

}
