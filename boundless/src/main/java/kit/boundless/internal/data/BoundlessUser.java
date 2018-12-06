package kit.boundless.internal.data;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import kit.boundless.BoundlessKit;
import kit.boundless.internal.data.storage.SQLUserIdentityDataHelper;
import kit.boundless.internal.data.storage.SQLiteDataStore;
import kit.boundless.internal.data.storage.contracts.UserIdentityContract;


public class BoundlessUser extends ContextWrapper {

  private static BoundlessUser sharedInstance;
  public @Nullable
  String internalId;
  public String externalId;
  public @Nullable
  String experimentGroup;
  private SQLiteDatabase sqlDB;

  private BoundlessUser(Context base) {
    super(base);

    sqlDB = SQLiteDataStore.getInstance(base).getWritableDatabase();

    UserIdentityContract saved = SQLUserIdentityDataHelper.find(sqlDB);
    if (saved == null) {
      saved = new UserIdentityContract(0,
          null,
          Settings.Secure.getString(base.getContentResolver(), Settings.Secure.ANDROID_ID),
          null
      );
      SQLUserIdentityDataHelper.insert(sqlDB, saved);
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

  public void update() {
    UserIdentityContract saved = SQLUserIdentityDataHelper.find(sqlDB);
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
    SQLUserIdentityDataHelper.update(sqlDB, saved);
  }

}
