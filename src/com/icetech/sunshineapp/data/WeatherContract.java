package com.icetech.sunshineapp.data;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.entity.ByteArrayEntity;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.ParseException;
import android.net.Uri;
import android.provider.BaseColumns;

public class WeatherContract {

	//The "Content Authority" is the name of the entire content provider
	//Its is similar to the relation ship between a domain and its website
	//This should be unique. So its advisable to use the package name of your
	//application, since its guarantee to be unique.
	
	public static final String CONTENT_AUTHORITY = "com.icetech.sunshineapp.data";
	
	//USe the content Authority to create the name of all the URI, which
	//will be used to access the content provider
	
	public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
	
	//possible paths (appended to BASE content URI for possible URI's)
	public static final String PATH_WEATHER = "weather";
	public static final String PATH_LOCATION = "location"; 
	
	
	
	/*Inner class that defines the table contents of the weather table */
	
	public static final class WeatherEntry implements BaseColumns{
		
		public static final String TABLE_NAME = "weather";
		
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
				.appendPath(PATH_WEATHER).build();
		
		public static final String CONTENT_TYPE = 
				"vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;
		
		public static final String CONTENT_ITEM_TYPE = 
				"vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;
		
		
		//Colummn with the foreing key into the location table
		public static final String COLUMN_LOC_KEY = "location_id";
		
		//Date stored as text with format yyyy-MM-dd
		public static final String COLUMN_DATE = "date";
		
		//Weather Id as returned by the API to identify the icon to be used
		public static final String COLUMN_WEATHER_ID = "weather_id";
		
		//Short and long description of the weather as provided by the API,
		//e.g "clear" and "sky is clear"
		public static final String COLUMN_SHORT_DESC = "short_desc";
		
		//Min and max temperature of the day (stored as float)
		public static final String COLUMN_MIN_TEMP = "min";
		
		public static final String COLUMN_MAX_TEMP = "max";
		
		//Humidity is stored as float representing percentage
		public static final String COLUMN_HUMIDITY = "humidity";

		//Pressure is stored as float 
		public static final String COLUMN_PRESSURE = "pressure";

		//wind speed is stored as float 
		public static final String COLUMN_WIND_SPEED = "windspeed";

		//degrees are meteorologica degree. e.g 0 for north, 180 for south, stored as floats 
		public static final String COLUMN_DEGREES = "degrees";
		
		
		public static Uri buildWeatherUri(long id){
			
			return ContentUris.withAppendedId(CONTENT_URI, id);
		}
		
		public static Uri buildWeatherLocation(String locationSetting){
			
			return CONTENT_URI.buildUpon().appendPath(locationSetting).build();
		}
		
		public static Uri buildWeatherLocationWithStartDate(String locationSetting, String startDate){

			return CONTENT_URI.buildUpon().appendPath(locationSetting).
					appendQueryParameter(COLUMN_DATE, startDate).build();
		}
		
		public static Uri buildWeatherLocationWithDate(String locationSetting, String date){

			return CONTENT_URI.buildUpon().appendPath(locationSetting).
					appendPath(date).build();
		}

		public static String getLocationSettingFromUri(Uri uri){
			
			return uri.getPathSegments().get(1);
			
		}

		public static String getDateFromUri(Uri uri){

			return uri.getPathSegments().get(2);

		}

		public static String getStartDateFromUri(Uri uri){

			return uri.getQueryParameter(COLUMN_DATE);

		}

		
	} //end of WeatherEntry Inner class
	
	

	/*Inner class that defines the table contents of the LocationEntry table */
	
	public static final class LocationEntry implements BaseColumns{
	
		//table name
		public static final String TABLE_NAME = "location";
		
		// table content provider URI
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
				.appendPath(PATH_LOCATION).build();
		
		public static final String CONTENT_TYPE = 
				"vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;
		
		public static final String CONTENT_ITEM_TYPE = 
				"vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;
		
		//location setting string is what will be send to the OpenweatherMap
		//as the location setting
		public static final String COLUMN_LOCATION_SETTING = "location_setting";
		
		//Human readable location string provided by the API i.e the City name
		public static final String COLUMN_CITY_NAME = "city_name";
		
		//Inorder to originally pipoint the location on the map
		//when we launche the map intent, we store the longitude and latitude
		//as returned by OpenWeatherMap
		
		public static final String COLUMN_COORD_LAT = "coord_lat";
		
		public static final String COLUMN_COORD_LONG = "coord_long";
		
			
public static Uri buildLocatonUri(long id){
			
			return ContentUris.withAppendedId(CONTENT_URI, id);
		}



	} //end of LocationEntry Inner class
	
	
	public static final String DATE_FORMAT = "yyyyMMdd";
	
	/**
	 *Convert the date to the format rquired by the DB
	 * @param date The Input date
	 * @return a DB friemdly representation of the date using the format define in DATE_FORMAT
	 */
	public static String getDbDateString (Date date) {
		
		//Because the API returns a unix (timestamp) measure in seconds
		//it must be converted into millisecond in order to be  converted into a valid date
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		return sdf.format(date);
	}
	
	public static Date getDateFromDb(String dateString){
		SimpleDateFormat dbDateFormat = new SimpleDateFormat();
		try {
			return dbDateFormat.parse(dateString);
		} catch (java.text.ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
}
