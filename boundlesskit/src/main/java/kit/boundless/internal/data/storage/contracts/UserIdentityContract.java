package kit.boundless.internal.data.storage.contracts;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

public class UserIdentityContract implements BaseColumns {

    public static final String TABLE_NAME = "App_State";
    public static final String COLUMNS_NAME_INTERNALID = "internalId";
    public static final String COLUMNS_NAME_EXTERNALID = "externalId";
    public static final String COLUMNS_NAME_EXPERIMENTGROUP = "experimentGroup";

    public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
            + _ID + " INTEGER PRIMARY KEY CHECK (" + _ID + " = 0),"
            + COLUMNS_NAME_INTERNALID + " TEXT,"
            + COLUMNS_NAME_EXTERNALID + " TEXT,"
            + COLUMNS_NAME_EXPERIMENTGROUP + " TEXT"
            + " )";


    public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public long id;
    @Nullable public String internalId;
    @Nullable public String externalId;
    @Nullable public String experimentGroup;

    public UserIdentityContract(long id, @Nullable String internalId, String externalId, @Nullable String experimentGroup) {
        this.id = id;
        this.internalId = internalId;
        this.externalId = externalId;
        this.experimentGroup = experimentGroup;
    }

    public static UserIdentityContract fromCursor(Cursor cursor) {
        return new UserIdentityContract(
                cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3)
        );
    }

}
