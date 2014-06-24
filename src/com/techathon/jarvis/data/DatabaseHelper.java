package com.techathon.jarvis.data;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	// static variables
	private  static int DATABASE_VERSION = 1;
	
	private static String DATABASE_NAME = "LOGS";
	
	private static String TABLE_NAME = "LocationLogs";
	
	private static String KEY_LOCALITY = "locality";
	private static String KEY_CITY = "city";
	private static String KEY_LAT = "latitude";
	private static String KEY_LONG = "longitude";
	private static String KEY_DATE = "date";
	private static String KEY_TIME = "time";
	private static String KEY_SPEED = "speed";
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);		
	}

	
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_TABLE = "CREATE TABLE "+ TABLE_NAME + " ("+
				"id INTEGER PRIMARY KEY AUTOINCREMENT, "+
				"locality TEXT,"+
				"city TEXT,"+
				"latitude REAL,"+
				"longitude REAL,"+
				"date TEXT,"+
				"time TEXT,"+
				"speed TEXT)";
		db.execSQL(CREATE_TABLE);
	}

	public void addLogs(LocationData locData){
    	SQLiteDatabase database = getWritableDatabase();
    	
    	ContentValues values = new ContentValues();
    	values.put(KEY_LOCALITY, locData.str_locality);
    	values.put(KEY_CITY, locData.str_city);
    	values.put(KEY_LAT, locData.latitude);
    	values.put(KEY_LONG, locData.longitude);
    	values.put(KEY_DATE, locData.date);
    	values.put(KEY_TIME, locData.time);
    	values.put(KEY_SPEED, locData.speed);
    	
    	database.insert(TABLE_NAME, null, values);
    	database.close();
    }
	
	
	public ArrayList<LocationData> getAllLogs(){
		ArrayList<LocationData> logsList =  new ArrayList<LocationData>();
		
		String selectQuery =  "SELECT * FROM "+ TABLE_NAME ;
		SQLiteDatabase db =  getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		
		// looping thru all the logs
		if(cursor.moveToFirst()){
			
			do{
				String locality =  cursor.getString(1);
				String city =  cursor.getString(2);
				double latitude = cursor.getDouble(3);
				double longitude= cursor.getDouble(4);
				String date= cursor.getString(5);
				String time = cursor.getString(6);
				float speed = cursor.getFloat(7);
				LocationData location = new LocationData(locality,city, latitude, longitude, date, time, speed);
				logsList.add(location);
			}while(cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return logsList;
	
			
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF "+TABLE_NAME+" EXISTS");
		onCreate(db);
	}

}