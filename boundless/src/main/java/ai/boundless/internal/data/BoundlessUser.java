package ai.boundless.internal.data;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import ai.boundless.BoundlessKit;
import ai.boundless.internal.data.storage.SqlUserIdentityDataHelper;
import ai.boundless.internal.data.storage.SqliteDataStore;
import ai.boundless.internal.data.storage.contracts.UserIdentityContract;


public class BoundlessUser extends ContextWrapper {

  private static BoundlessUser sharedInstance;
  @Nullable
  public String internalId;
  public String externalId;

  @Nullable
  public String experimentGroup;
  private SQLiteDatabase sqlDb;

  private BoundlessUser(Context base) {
    super(base);

    sqlDb = SqliteDataStore.getInstance(base).getWritableDatabase();

    UserIdentityContract saved = SqlUserIdentityDataHelper.find(sqlDb);
    if (saved == null) {
      saved = new UserIdentityContract(0,
          null,
          Settings.Secure.getString(base.getContentResolver(), Settings.Secure.ANDROID_ID),
          null
      );
      SqlUserIdentityDataHelper.insert(sqlDb, saved);
    }
    internalId = saved.internalId;
    externalId = saved.externalId;
    experimentGroup = saved.experimentGroup;
  }

  static BoundlessUser getSharedInstance(Context base) {
    if (sharedInstance == null) {
      sharedInstance = new BoundlessUser(base);
    }
    return sharedInstance;
  }

  /**
   * Updates this model.
   */
  public void update() {
    UserIdentityContract saved = SqlUserIdentityDataHelper.find(sqlDb);
    if (saved == null) {
      BoundlessKit.debugLog("BoundlessUser", "No user found to update.");
      return;
    }
    if (TextUtils.equals(saved.internalId, internalId) && TextUtils.equals(saved.externalId,
        externalId
    ) && TextUtils.equals(saved.experimentGroup, experimentGroup)) {
      return;
    }
    saved.internalId = internalId;
    saved.externalId = externalId;
    saved.experimentGroup = experimentGroup;
    BoundlessKit.debugLog("BoundlessUser",
        "Updating user to internalId:" + internalId + " externalId:" + externalId
            + " experimentGroup:" + experimentGroup
    );
    SqlUserIdentityDataHelper.update(sqlDb, saved);
  }

}
