package kit.boundless.internal.data.storage.contracts;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

public class UserIdentityContract implements BaseColumns {

    public static final String TABLE_NAME = "App_State";
    public static final String COLUMNS_NAME_INTERNALID = "internalId";

    public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
            + _ID + " INTEGER PRIMARY KEY CHECK (" + _ID + " = 0),"
            + COLUMNS_NAME_INTERNALID + " TEXT"
            + " )";


    public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public long id;
    @Nullable public String internalId;

    public UserIdentityContract(long id, @Nullable String internalId) {
        this.id = id;
        this.internalId = internalId;
    }

    public static UserIdentityContract fromCursor(Cursor cursor) {
        return new UserIdentityContract(
                cursor.getLong(0), cursor.getString(1)
        );
    }

}
