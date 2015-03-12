package com.icetech.sunshineapp.data;

import com.icetech.sunshineapp.data.WeatherContract.LocationEntry;
import com.icetech.sunshineapp.data.WeatherContract.WeatherEntry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class WeatherDbHelper extends SQLiteOpenHelper {

	//if you change the Database Schema, you must increment the version
	
	private static final int DATABASE_VERSION = 1;
	
	public static final String DATABASE_NAME = "weather.db";
	
	public WeatherDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		
		// TODO Auto-generated constructor stub		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// Create a table to hold locations. A location consist of postal code
		//and a city name e.g Buea
		
		final String SQL_CREATE_WEATHER_TABLE = 
				"CREATE TABLE " + WeatherEntry.TABLE_NAME + " (" + 
						WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

						//id of the location entry associated with this weather data

						WeatherEntry.COLUMN_LOC_KEY + " INTEGER NOT NULL, " +

						WeatherEntry.COLUMN_DATETEXT + " TEXT NOT NULL, " +

						WeatherEntry.COLUMN_SHORT_DESC + " TEXT NOT NULL, " +

						WeatherEntry.COLUMN_WEATHER_ID + " TEXT NOT NULL, " +

						WeatherEntry.COLUMN_MIN_TEMP + " REAL NOT NULL, " +
						WeatherEntry.COLUMN_MAX_TEMP + " REAL NOT NULL, " +

						WeatherEntry.COLUMN_HUMIDITY + " REAL NOT NULL, " +
						WeatherEntry.COLUMN_PRESSURE + " TEXT NOT NULL, " +
						WeatherEntry.COLUMN_WIND_SPEED + " TEXT NOT NULL, " +
						WeatherEntry.COLUMN_DEGREES + " TEXT NOT NULL, " +
						
						//Setup location Column as foreign key to the location table
						
						" FOREIGN KEY (" + WeatherEntry.COLUMN_LOC_KEY + ") REFERENCES " +
						LocationEntry.TABLE_NAME + " (" + LocationEntry._ID + "), " +
						
						//To ensure the application has just one weather entry per day, per location
						//Its created unique constraint with replace
						" UNIQUE (" + WeatherEntry.COLUMN_DATETEXT + ", " +
						WeatherEntry.COLUMN_LOC_KEY +  ") ON CONFLICT REPLACE);";
		
		
		final String SQL_CREATE_LOCATION_TABLE = 
				"CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
						LocationEntry._ID + " INTEGER PRIMARY KEY, " + 
						LocationEntry.COLUMN_CITY_NAME + " TEXT NOT NULL, " +
						LocationEntry.COLUMN_LOCATION_SETTING + " TEXT NOT NULL, " +
						LocationEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " + 
						LocationEntry.COLUMN_COORD_LONG + " REAL NOT NULL, " +
						" UNIQUE (" + LocationEntry.COLUMN_CITY_NAME + ") ON CONFLICT IGNORE" +
						" );";

						
		db.execSQL(SQL_CREATE_LOCATION_TABLE);
		
		db.execSQL(SQL_CREATE_WEATHER_TABLE);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		db.execSQL(" DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
		db.execSQL(" DROP TABLE IF EXISTS " + WeatherEntry.TABLE_NAME);
		
	}

}
