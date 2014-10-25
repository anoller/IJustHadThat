package com.alecnoller.ijusthadthat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBManager {
	static final String KEY_ROWID = "_id";
	static final String KEY_NAME = "name";
	static final String TAG = "DBManager";
	
	static final String DATABASE_NAME = "PlacesDB";
	static final String DATABASE_TABLE = "places";
	static final int DATABASE_VERSION = 1;
	
	static final String DATABASE_CREATE = 
			"create table places (_id integer primary key autoincrement, " 
			+ "name text not null);";
	
	final Context context;
	
	DatabaseHelper DBHelper;
	SQLiteDatabase db;
	
	public DBManager(Context ctx) {
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			try {
				db.execSQL(DATABASE_CREATE);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS places");
			onCreate(db);
		}
	}
	
	//opens the database
	public DBManager open() throws SQLException {
		db = DBHelper.getWritableDatabase();
		return this;
	}
	
	//closes the database
	public void close() {
		DBHelper.close();
	}
	
	//insert a place into the database
	public long insertPlace(String name) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_NAME, name);
		return db.insert(DATABASE_TABLE, null, initialValues);
	} 
	
	//deletes a particular contact
	public boolean deletePlace(String name) {
		name = name.replace("'", "''");
		return db.delete(DATABASE_TABLE, KEY_NAME + " = " + "'" + name + "'", null) > 0;
	}

	//retrieves all the contacts
	public Cursor getAllPlaces() {
		return db.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_NAME},
				null, null, null, null, null);
	}

}

