package luci.uci.edu.groupstatus.datastore;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "groupStatus.db";
	private static final int DATABASE_VERSION = 1;

	public static final String TABLE_GROUPSTATUS = "tableOfGroupStatus";
	public static final String TABLE_GROUPSTATUS_COLUMN_ID = "_id";
	public static final String TABLE_GROUPSTATUS_COLUMN_USERID = "userID";
	public static final String TABLE_GROUPSTATUS_COLUMN_GROUP = "mGroup";		//GROUP is a keyword of SQLite
	public static final String TABLE_GROUPSTATUS_COLUMN_TIMESTAMP = "timestamp";
	public static final String TABLE_GROUPSTATUS_COLUMN_STATUS = "status";
	public static final String TABLE_GROUPSTATUS_COLUMN_GROUPSTATUS = "mgroupStatus"; //GROUP is a keyword of SQLite
	public static final String TABLE_GROUPSTATUS_COLUMN_WIFILIST = "wifiList";
	public static final String TABLE_GROUPSTATUS_COLUMN_NOISELEVEL = "noiseLevel";
	public static final String TABLE_GROUPSTATUS_COLUMN_LOCATION = "location";
	public static final String TABLE_GROUPSTATUS_COLUMN_ADDRESS = "address";
	public static final String TABLE_GROUPSTATUS_COLUMN_UPLOADED = "uploaded";
	

	private static final String DATABASE_CREATE_TABLE_GROUPSTATUS = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_GROUPSTATUS
			+ " ( "
			+ TABLE_GROUPSTATUS_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "	
			+ TABLE_GROUPSTATUS_COLUMN_USERID + " TEXT NOT NULL , "
			+ TABLE_GROUPSTATUS_COLUMN_GROUP + " TEXT NOT NULL , "
			+ TABLE_GROUPSTATUS_COLUMN_TIMESTAMP + " TEXT NOT NULL , "
			+ TABLE_GROUPSTATUS_COLUMN_STATUS + " TEXT NOT NULL , "
			+ TABLE_GROUPSTATUS_COLUMN_GROUPSTATUS + " TEXT NOT NULL , "
			+ TABLE_GROUPSTATUS_COLUMN_WIFILIST + " TEXT NOT NULL , "
			+ TABLE_GROUPSTATUS_COLUMN_NOISELEVEL + " TEXT NOT NULL , "
			+ TABLE_GROUPSTATUS_COLUMN_LOCATION + " TEXT NOT NULL , "
			+ TABLE_GROUPSTATUS_COLUMN_ADDRESS + " TEXT NOT NULL , "
			+ TABLE_GROUPSTATUS_COLUMN_UPLOADED + " INT NOT NULL"
			+ " );";

	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE_TABLE_GROUPSTATUS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(MySQLiteHelper.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion + ", which will destroy all old data");

		db.execSQL("DROP TABLE IF EXISTS " + DATABASE_CREATE_TABLE_GROUPSTATUS);
		onCreate(db);
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}

	@Override
	public synchronized void close() {
		super.close();
	}

}
