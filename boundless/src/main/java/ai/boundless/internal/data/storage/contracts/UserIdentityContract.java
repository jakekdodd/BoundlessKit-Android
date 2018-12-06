package ai.boundless.internal.data.storage.contracts;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

/**
 * The type User identity contract.
 */
public class UserIdentityContract implements BaseColumns {

  /**
   * The constant TABLE_NAME.
   */
  public static final String TABLE_NAME = "App_State";
  /**
   * The constant COLUMNS_NAME_INTERNALID.
   */
  public static final String COLUMNS_NAME_INTERNALID = "internalId";
  /**
   * The constant COLUMNS_NAME_EXTERNALID.
   */
  public static final String COLUMNS_NAME_EXTERNALID = "externalId";
  /**
   * The constant COLUMNS_NAME_EXPERIMENTGROUP.
   */
  public static final String COLUMNS_NAME_EXPERIMENTGROUP = "experimentGroup";

  /**
   * The constant SQL_CREATE_TABLE.
   */
  public static final String SQL_CREATE_TABLE =
      "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY CHECK (" + _ID
          + " = 0)," + COLUMNS_NAME_INTERNALID + " TEXT," + COLUMNS_NAME_EXTERNALID + " TEXT,"
          + COLUMNS_NAME_EXPERIMENTGROUP + " TEXT" + " )";


  /**
   * The constant SQL_DROP_TABLE.
   */
  public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

  /**
   * The Id.
   */
  public long id;
  /**
   * The Internal id.
   */
  @Nullable
  public String internalId;
  /**
   * The External id.
   */
  @Nullable
  public String externalId;
  /**
   * The Experiment group.
   */
  @Nullable
  public String experimentGroup;

  /**
   * Instantiates a new User identity contract.
   *
   * @param id the id
   * @param internalId the internal id
   * @param externalId the external id
   * @param experimentGroup the experiment group
   */
  public UserIdentityContract(
      long id, @Nullable String internalId, String externalId, @Nullable String experimentGroup) {
    this.id = id;
    this.internalId = internalId;
    this.externalId = externalId;
    this.experimentGroup = experimentGroup;
  }

  /**
   * From cursor user identity contract.
   *
   * @param cursor the cursor
   * @return the user identity contract
   */
  public static UserIdentityContract fromCursor(Cursor cursor) {
    return new UserIdentityContract(cursor.getLong(0),
        cursor.getString(1),
        cursor.getString(2),
        cursor.getString(3)
    );
  }

}
