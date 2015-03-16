package com.icetech.sunshineapp.data;

import java.util.Iterator;

import com.icetech.sunshineapp.data.WeatherContract.LocationEntry;
import com.icetech.sunshineapp.data.WeatherContract.WeatherEntry;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.AndroidException;
import android.util.Log;

public class WeatherProvider extends ContentProvider {

	public static final String LOG_TAG = WeatherProvider.class.getSimpleName();

	public WeatherProvider() {
	}
	public static final int WEATHER = 100;
	public static final int WEATHER_WITH_LOCATION = 101;
	public static final int WEATHER_WITH_LOCATION_DATE = 102;
	public static final int LOCATION = 300;
	public static final int LOCATION_ID = 301;

	private WeatherDbHelper mOpenHelper;
	private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;

	static{
		sWeatherByLocationSettingQueryBuilder =  new SQLiteQueryBuilder();
		sWeatherByLocationSettingQueryBuilder.setTables(
				WeatherContract.WeatherEntry.TABLE_NAME + " INNER JOIN " +
						WeatherContract.LocationEntry.TABLE_NAME + 
						" ON " + WeatherContract.WeatherEntry.TABLE_NAME + 
						"." + WeatherContract.WeatherEntry.COLUMN_LOC_KEY + 
						" = " + WeatherContract.LocationEntry.TABLE_NAME + 
						"." + WeatherContract.LocationEntry._ID

				);
	}

	private static final String sLocationSettingSelection = 
			WeatherContract.LocationEntry.TABLE_NAME +
			"." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + "= ? ";

	private static final String sLocationSettingWithStartDateSelection =
			WeatherContract.LocationEntry.TABLE_NAME +
			"." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
			WeatherContract.WeatherEntry.COLUMN_DATE + " >= ?";

	private static final String sLocationSettingWithDaySelection =
			WeatherContract.LocationEntry.TABLE_NAME +
			"." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
			WeatherContract.WeatherEntry.COLUMN_DATE + " = ?";


	private Cursor getWeatherByLocation(Uri uri, String[] projection, String sortOrder){

		String locationSetting = WeatherEntry.getLocationSettingFromUri(uri);
		String startDate = WeatherEntry.getStartDateFromUri(uri);

		String[] selectionArgs;
		String selection;

		if(startDate == null){
			selection = sLocationSettingSelection;
			selectionArgs = new String[] {locationSetting };
		}
		else{
			selection = sLocationSettingWithStartDateSelection;
			selectionArgs = new String[] {locationSetting, startDate };
		}

		return sWeatherByLocationSettingQueryBuilder.query(
				mOpenHelper.getReadableDatabase(),
				projection,
				selection,
				selectionArgs,
				null,
				null,
				sortOrder
				);

	}

	private Cursor getWeatherByLocatioAndDate(Uri uri, String[] projection, String sortOrder){
		String locationSetting = WeatherEntry.getLocationSettingFromUri(uri);
		String Date = WeatherEntry.getDateFromUri(uri);

		String[] selectionArgs = new String[] {locationSetting, Date};
		String selection = sLocationSettingWithDaySelection;

		return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
				projection,
				selection,
				selectionArgs,
				null,
				null,
				sortOrder
				);
	}

	//The Uri matcher used by this content provider
	//
	private static final UriMatcher sUriMatcher = buildUriMatcher();

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final int match = sUriMatcher.match(uri);
		int affectedNumOfRows;
		switch (match) {
		case WEATHER:

			affectedNumOfRows = db.delete(WeatherEntry.TABLE_NAME, selection, selectionArgs);

			break;

		case LOCATION:
			affectedNumOfRows = db.delete(LocationEntry.TABLE_NAME, selection, selectionArgs);

			break;

		default:
			throw new UnsupportedOperationException("Unkwon Uri " + uri);
		}
		//because a null deletes all rows
		if(null == selection || 0 !=  affectedNumOfRows){
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return affectedNumOfRows;
	}

	@Override
	public String getType(Uri uri) {
		// Implement this to handle requests for the MIME type of the data
		// at the given URI.
		final int match = sUriMatcher.match(uri);
		switch (match) {
		case WEATHER_WITH_LOCATION_DATE:
			return WeatherContract.WeatherEntry.CONTENT_ITEM_TYPE;

		case WEATHER_WITH_LOCATION:
			return WeatherContract.WeatherEntry.CONTENT_TYPE;

		case WEATHER:
			return WeatherContract.WeatherEntry.CONTENT_TYPE;

		case LOCATION:
			return WeatherContract.WeatherEntry.CONTENT_TYPE;

		case LOCATION_ID:
			return WeatherContract.WeatherEntry.CONTENT_ITEM_TYPE;

		default:
			throw new UnsupportedOperationException("Unknown Uri " + uri);

		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		final int match = sUriMatcher.match(uri);
		Uri returnUri;
		long _id;
		switch (match) {
		case WEATHER:
			_id = db.insert(WeatherEntry.TABLE_NAME, null, values);
			if(_id > 0){
				returnUri = WeatherContract.WeatherEntry.buildWeatherUri(_id);
			}
			else throw new android.database.SQLException("Fail to insert row into " + uri.toString());
			break;

		case LOCATION:
			_id = db.insert(LocationEntry.TABLE_NAME, null, values);
			if(_id > 0){
				returnUri = WeatherContract.LocationEntry.buildLocatonUri(_id);
			}
			else throw new android.database.SQLException("Fail to insert row into " + uri.toString());

			break;

		default:
			throw new UnsupportedOperationException("Unkwon Uri " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return returnUri;
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new WeatherDbHelper(getContext());
		return true; //returns true for successful creation of the content provider
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		//Implement this to handle query requests from clients.

		//here's is the switch statement that, given a URL, will determine
		//what kind of request and query the database accordingly
		Cursor retCursor = null;
		Log.d(LOG_TAG, Integer.toString(sUriMatcher.match(uri)));
		switch (sUriMatcher.match(uri)) {

		//"weather/*/*"
		case WEATHER_WITH_LOCATION_DATE:
			retCursor = getWeatherByLocatioAndDate(uri, projection, sortOrder);
			break;

			//"weather/*"
		case WEATHER_WITH_LOCATION:
			retCursor = getWeatherByLocation(uri, projection, sortOrder);
			break;

			//"weather"
		case WEATHER:
			retCursor = mOpenHelper.getReadableDatabase().query(
					WeatherEntry.TABLE_NAME, 
					projection, 
					selection, 
					selectionArgs, 
					null, 
					null, 
					sortOrder
					);

			break;

			//"location/*"
		case LOCATION_ID:
			retCursor = mOpenHelper.getReadableDatabase().query(
					LocationEntry.TABLE_NAME, 
					projection, 
					WeatherContract.LocationEntry._ID + " = " + ContentUris.parseId(uri), 
					null, 
					null, 
					null, 
					sortOrder
					); 
			break;

			//"location"
		case LOCATION:
			retCursor = mOpenHelper.getReadableDatabase().query(
					LocationEntry.TABLE_NAME, 
					projection, 
					selection, 
					selectionArgs, 
					null, 
					null, 
					sortOrder
					);
			break;

		default:
			throw new UnsupportedOperationException("Unknown Uri " + uri);
		}
		//This register a content observer to watch for changes that happens
		//in this uri and any of its decerdent
		retCursor.setNotificationUri(getContext().getContentResolver(), uri);

		return retCursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		final int match = sUriMatcher.match(uri);

		int affectedNumOfRows;
		switch (match) {
		case WEATHER:
			affectedNumOfRows = db.update(WeatherEntry.TABLE_NAME, values, selection, selectionArgs);

			break;

		case LOCATION:
			affectedNumOfRows = db.update(LocationEntry.TABLE_NAME, values, selection, selectionArgs);

			break;

		default:
			throw new UnsupportedOperationException("Unkwon Uri " + uri);
		}
		if( 0 != affectedNumOfRows){
			getContext().getContentResolver().notifyChange(uri, null);
		}

		return affectedNumOfRows;
	}




	//
	private static UriMatcher buildUriMatcher(){

		//All paths added to the URI matcher have a corresponding code to
		//return when the URI is found. The code passed to the contructor
		//represent the code to be return for the corresponding URI.
		//IT's common to use NO_MATCH as the code for this case

		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		final String authority = WeatherContract.CONTENT_AUTHORITY;

		//for each type of URI you want to add, create a corresponding code
		matcher.addURI(authority, WeatherContract.PATH_WEATHER, WEATHER);
		matcher.addURI(authority, WeatherContract.PATH_WEATHER + "/*", WEATHER_WITH_LOCATION);
		matcher.addURI(authority, WeatherContract.PATH_WEATHER+ "/*/*", WEATHER_WITH_LOCATION_DATE);

		matcher.addURI(authority, WeatherContract.PATH_LOCATION, LOCATION);
		matcher.addURI(authority, WeatherContract.PATH_LOCATION + "/#", LOCATION_ID);

		return matcher;
	}
	
	//bulk insert value to the weather table
	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		final SQLiteDatabase  db = mOpenHelper.getReadableDatabase(); 
		
		final int match = sUriMatcher.match(uri);
		switch (match) {
		case WEATHER:
			db.beginTransaction();
			int returnCount = 0;
			try{
				for (ContentValues contentValues : values) {
					long _id = db.insert(WeatherEntry.TABLE_NAME, null, contentValues);
					if(-1 != _id){
						returnCount++;
					}
				}
				db.setTransactionSuccessful();
			}
			finally{
				db.endTransaction();
			}
			getContext().getContentResolver().notifyChange(uri, null);
			return returnCount;

		default:
			return super.bulkInsert(uri, values);
		}
		
	}
}
