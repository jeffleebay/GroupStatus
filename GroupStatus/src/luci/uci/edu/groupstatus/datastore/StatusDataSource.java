package luci.uci.edu.groupstatus.datastore;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class StatusDataSource {
	// Database fields
		private SQLiteDatabase database;
		private MySQLiteHelper dbHelper;
		private String[] allColumns = { 
				MySQLiteHelper.TABLE_GROUPSTATUS_COLUMN_ID,
				MySQLiteHelper.TABLE_GROUPSTATUS_COLUMN_USERID, 
				MySQLiteHelper.TABLE_GROUPSTATUS_COLUMN_GROUP, 
				MySQLiteHelper.TABLE_GROUPSTATUS_COLUMN_TIMESTAMP,
				MySQLiteHelper.TABLE_GROUPSTATUS_COLUMN_STATUS, 
				MySQLiteHelper.TABLE_GROUPSTATUS_COLUMN_GROUPSTATUS, 
				MySQLiteHelper.TABLE_GROUPSTATUS_COLUMN_WIFILIST, 
				MySQLiteHelper.TABLE_GROUPSTATUS_COLUMN_NOISELEVEL, 
				MySQLiteHelper.TABLE_GROUPSTATUS_COLUMN_LOCATION, 
				MySQLiteHelper.TABLE_GROUPSTATUS_COLUMN_ADDRESS, 
				};

		public StatusDataSource(Context context) {
			dbHelper = new MySQLiteHelper(context);
		}

		public void open() throws SQLException {
			database = dbHelper.getWritableDatabase();
		}

		public void close() {
			dbHelper.close();
		}

		public StatusObject createAStatusObject(String userID, String group, String timestamp, String status, 
				String groupStatus, String wifiList, String noiseLevel, String location, String address) {
			ContentValues values = new ContentValues();
			values.put(MySQLiteHelper.TABLE_GROUPSTATUS_COLUMN_USERID, userID);
			values.put(MySQLiteHelper.TABLE_GROUPSTATUS_COLUMN_GROUP, group);
			values.put(MySQLiteHelper.TABLE_GROUPSTATUS_COLUMN_TIMESTAMP, timestamp);
			values.put(MySQLiteHelper.TABLE_GROUPSTATUS_COLUMN_STATUS, status);
			values.put(MySQLiteHelper.TABLE_GROUPSTATUS_COLUMN_GROUPSTATUS, groupStatus);
			values.put(MySQLiteHelper.TABLE_GROUPSTATUS_COLUMN_WIFILIST, wifiList);
			values.put(MySQLiteHelper.TABLE_GROUPSTATUS_COLUMN_NOISELEVEL, noiseLevel);
			values.put(MySQLiteHelper.TABLE_GROUPSTATUS_COLUMN_LOCATION, location);
			values.put(MySQLiteHelper.TABLE_GROUPSTATUS_COLUMN_ADDRESS, address);
			
			
			long insertId = database.insert(MySQLiteHelper.TABLE_GROUPSTATUS, null, values);
			Cursor cursor = database.query(MySQLiteHelper.TABLE_GROUPSTATUS, allColumns, 
					MySQLiteHelper.TABLE_GROUPSTATUS_COLUMN_ID + " = " + insertId, null,null, null, null);
			cursor.moveToFirst();
			StatusObject newStatus = cursorToStatus(cursor);
			cursor.close();
			
//			System.out.println("saved to status database");

			return newStatus;	//in case we use any custom adapter to list all stored group status
		}

		public void deleteAStatusObject(StatusObject StatusObject) {
			long id = StatusObject.getId();
//			System.out.println("A message deleted with id: " + id);
			database.delete(MySQLiteHelper.TABLE_GROUPSTATUS,
					MySQLiteHelper.TABLE_GROUPSTATUS_COLUMN_ID + " = " + id, null);
		}
		
		public List<StatusObject> getAllStatusObjects() {
			List<StatusObject> listOfMessages = new ArrayList<StatusObject>();

			Cursor cursor = database.query(MySQLiteHelper.TABLE_GROUPSTATUS,allColumns, null, null, null, null, null);

			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				StatusObject statusObject = cursorToStatus(cursor);
				listOfMessages.add(statusObject);
				cursor.moveToNext();
			}
			// Make sure to close the cursor
			cursor.close();
			return listOfMessages;
		}

		private StatusObject cursorToStatus(Cursor cursor) {
			StatusObject StatusObject = new StatusObject();
			StatusObject.setId(cursor.getLong(0));
			StatusObject.setUserID(cursor.getString(1));
			StatusObject.setGroup(cursor.getString(2));
			StatusObject.setTimestamp(cursor.getString(3));
			StatusObject.setStatus(cursor.getString(4));
			StatusObject.setGroupStatus(cursor.getString(5));
			StatusObject.setWifiList(cursor.getString(6));
			StatusObject.setNoiseLevel(cursor.getString(7));
			StatusObject.setLocation(cursor.getString(8));
			StatusObject.setAddress(cursor.getString(9));
			return StatusObject;
		}
}
