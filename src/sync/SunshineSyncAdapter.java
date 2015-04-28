package sync;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.format.Time;
import android.util.Log;

import com.icetech.sunshineapp.MainActivity;
import com.icetech.sunshineapp.Utility;
import com.icetech.sunshineapp.data.WeatherContract;
import com.icetech.sunshineapp.data.WeatherContract.LocationEntry;
import com.icetech.sunshineapp.data.WeatherContract.WeatherEntry;
import com.icetech.sunshineapp.R;

public class SunshineSyncAdapter extends AbstractThreadedSyncAdapter {

	public final String LOG_TAG = SunshineSyncAdapter.class.getSimpleName();

	// Interval at which to sync with the weather, in seconds.
	// 60 seconds (1 minute) * 180 = 3 hours
	public static final int SYNC_INTERVAL = 60 * 180;
	public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

	private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24; 
	private static final int WEATHER_NOTIFICATION_ID = 3004;

	private static final String[] NOTIFY_WEATHER_PROJECTION = new String[] {
		WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
		WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
		WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
		WeatherContract.WeatherEntry.COLUMN_SHORT_DESC
	};
	// these indices must match the projection
	private static final int INDEX_WEATHER_ID = 0;
	private static final int INDEX_MAX_TEMP = 1;
	private static final int INDEX_MIN_TEMP = 2;
	private static final int INDEX_SHORT_DESC = 3;


	public SunshineSyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);

	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {
		
		Log.d(LOG_TAG, "Starting sync");
		String locationParam = Utility.getPreferedLocation(getContext());


		//These two need to be declared outside the try/catch block so that
		//they can be called in the finally block statement


		HttpURLConnection urlConnection = null;
		BufferedReader reader = null;

		//Will contain the raw json response as a string
		String forecastJsonStr = null;

		String format = "json";
		String unit = "metric";
		int numDays = 14;	


		try {
			//Construct the URL of the OpenWeatherMap Query
			//URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=buea&mode=json&units=metric&cnt=7");
			final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";

			final String QUERY_PARAM = "q";
			final String FORMAT_PARAM = "mode";
			final String UNITS_PARAM = "units";
			final String DAYS_PARAM = "cnt";

			Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
					.appendQueryParameter(QUERY_PARAM, locationParam)
					.appendQueryParameter(FORMAT_PARAM, format)
					.appendQueryParameter(UNITS_PARAM, unit)
					.appendQueryParameter(DAYS_PARAM, Integer.toString(numDays)).build();

			URL url = new URL(builtUri.toString());
			Log.d(LOG_TAG, url.toString());

			Log.v(LOG_TAG, "Built URI" + builtUri.toString());

			//Create the Request to openWeatherMap and create the connection
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.connect();

			//Read the Input Stream into a String
			InputStream inputStream = urlConnection.getInputStream();
			StringBuffer buffer = new StringBuffer();

			if (inputStream == null) {
				//Nothing to do
				return;
			}

			reader = new BufferedReader(new InputStreamReader(inputStream));

			String line;
			while ((line = reader.readLine()) != null) {
				//Since its json, adding a new line isn't necessary (it won't affect parsing)
				//But does make debugging alot easier, if you print out the completed
				//buffer for debugging

				buffer.append(line + '\n');        		

			}

			if(buffer.length() == 0){
				//string is empty, no point in passing
				return;
			}

			forecastJsonStr = buffer.toString();
			Log.v(LOG_TAG, "Forcast JSON String" + forecastJsonStr);


		} catch (IOException e) {
			Log.e(LOG_TAG, "Error", e);
			e.printStackTrace();
			//If the code didn't successfully get the weather data there is no point
			//in passing it
			return;
		}
		finally{
			/* Closing the connection and the Reader */
			if(urlConnection != null){
				urlConnection.disconnect();
			}
			if(reader != null){
				try {
					reader.close();
				} catch (final IOException e) {
					Log.e(LOG_TAG, "Error Closing stream", e);

				}

			}
		}


		//These are the names of the Json objects that need to be extracted

		//Location Information
		final String OWM_CITY = "city";
		final String OWM_CITY_NAME = "name";
		final String OWM_COORD = "coord";

		//Location coordinates
		final String OWM_LATITUDE = "lat";
		final String OWM_LONGITUDE = "lon";

		//Weather information. Each day's forecast info is an element of the "list" array.
		final String OWM_LIST = "list";

		final String OWM_DATETIME = "dt";
		final String OWM_PRESSURE = "pressure";
		final String OWM_HUMIDITY = "humidity";
		final String OWM_WINDSPEED = "speed";
		final String OWM_WIND_DIRECTION = "deg";				


		//All temperatures are children of temp object
		final String OWM_TEMPERATURE = "temp";
		final String OWM_MAX = "max";
		final String OWM_MIN = "min";

		final String OWM_WEATHER = "weather";
		final String OWM_DESCRIPTION = "main";
		final String OWM_WEATHER_ID = "id";

		try {
			JSONObject forecastJson = new JSONObject(forecastJsonStr);
			JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
			JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);

			String cityName = cityJson.getString(OWM_CITY_NAME);
			cityName = cityName.toLowerCase();

			JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
			double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
			double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);

			locationParam = locationParam.toLowerCase();

			long locationId = addLocation(cityName, locationParam, cityLatitude, cityLongitude);

			Vector<ContentValues> cVVector = new Vector<ContentValues>();

			// OWM returns daily forecasts based upon the local time of the city that is being
			// asked for, which means that we need to know the GMT offset to translate this data
			// properly.

			// Since this data is also sent in-order and the first day is always the
			// current day, we're going to take advantage of that to get a nice
			// normalised UTC date for all of our weather.

			Time dayTime = new Time();
			dayTime.setToNow();

			// we start at the day returned by local time. Otherwise this is a mess.
			int julianStartDay =
					Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

			// now we work exclusively in UTC
			dayTime = new Time();


			for (int i = 0; i < weatherArray.length(); i++) {

				//Get the Json object representing the day
				JSONObject dayForecast = weatherArray.getJSONObject(i);

				// Cheating to convert this to UTC time, which is what we want anyhow
				long dateTime = dayTime.setJulianDay(julianStartDay+i);

				Log.d(LOG_TAG, "Weather Date: " + dateTime);

				double humidity = dayForecast.getDouble(OWM_HUMIDITY);
				double pressure = dayForecast.getDouble(OWM_PRESSURE);
				double windspeed = dayForecast.getDouble(OWM_WINDSPEED);
				double windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

				JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
				double high = temperatureObject.getDouble(OWM_MAX);
				double low = temperatureObject.getDouble(OWM_MIN);

				JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
				String description = weatherObject.getString(OWM_DESCRIPTION);

				int weatherId = weatherObject.getInt(OWM_WEATHER_ID);


				ContentValues weathervalues = new ContentValues();

				weathervalues.put(WeatherEntry.COLUMN_LOC_KEY, locationId);
				weathervalues.put(WeatherEntry.COLUMN_DATE, dateTime);
				weathervalues.put(WeatherEntry.COLUMN_HUMIDITY, humidity);
				weathervalues.put(WeatherEntry.COLUMN_PRESSURE, pressure);
				weathervalues.put(WeatherEntry.COLUMN_WIND_SPEED, windspeed);
				weathervalues.put(WeatherEntry.COLUMN_DEGREES, windDirection);
				weathervalues.put(WeatherEntry.COLUMN_MAX_TEMP, high);
				weathervalues.put(WeatherEntry.COLUMN_MIN_TEMP, low);
				weathervalues.put(WeatherEntry.COLUMN_SHORT_DESC, description);
				weathervalues.put(WeatherEntry.COLUMN_WEATHER_ID, weatherId);

				cVVector.add(weathervalues);
			}

			if (cVVector.size() > 0) {
				ContentValues[] cvArray = new ContentValues[cVVector.size()];
				cVVector.toArray(cvArray);
				getContext().getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, cvArray);

				Log.d(LOG_TAG, "FetchweatherTask complete. " + cVVector.size() + " inserted");

				// delete old data so we don't build up an endless history
				getContext().getContentResolver().delete(WeatherContract.WeatherEntry.CONTENT_URI,
						WeatherContract.WeatherEntry.COLUMN_DATE + " <= ?",
						new String[] {Long.toString(dayTime.setJulianDay(julianStartDay-1))});
				
				notifyWeather();
			}



		} catch (JSONException e) {

			e.printStackTrace();
		}

		//this will only happen if there was an error getting or parsing the forecast
		return;



	}

	/**
	 * 
	 * @param locationSetting
	 * @param cityName
	 * @param lat
	 * @param lon
	 * @return
	 */
	private long addLocation(String locationSetting, String cityName, double lat, double lon){

		Log.v(LOG_TAG, "inserting " + cityName + " with coord " + lat + ", " + lon);

		//First check if the location with city name exist in the db
		Cursor cursor =  getContext().getContentResolver().query(WeatherContract.LocationEntry.CONTENT_URI, 
				new String[]{LocationEntry._ID},
				LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
				new String[]{locationSetting}, 
				null);

		if(cursor.moveToFirst()){
			Log.v(LOG_TAG, "found it in the database");
			int locationIndex = cursor.getColumnIndex(LocationEntry._ID);
			return cursor.getLong(locationIndex);
		}else{
			Log.v(LOG_TAG, "Didn't find it in the database, inserting now!");
			ContentValues locationValues = new ContentValues();

			locationValues.put(LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
			locationValues.put(LocationEntry.COLUMN_CITY_NAME, cityName);
			locationValues.put(LocationEntry.COLUMN_COORD_LAT, lat);
			locationValues.put(LocationEntry.COLUMN_COORD_LONG, lon);

			Uri locationInsertUri = getContext().getContentResolver()
					.insert(LocationEntry.CONTENT_URI, locationValues);

			return ContentUris.parseId(locationInsertUri);
		}

	}

	/**
	 * Helper method to have the sync adapter sync immediately
	 * @param context The context used to access the account service
	 */
	public static void syncImmediately(Context context) {
		Bundle bundle = new Bundle();
		bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
		bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		ContentResolver.requestSync(getSyncAccount(context),
				context.getString(R.string.content_authority), bundle);
	}

	/**
	 * Helper method to get the fake account to be used with SyncAdapter, or make a new one
	 * if the fake account doesn't exist yet.  If we make a new account, we call the
	 * onAccountCreated method so we can initialise things.
	 *
	 * @param context The context used to access the account service
	 * @return a fake account.
	 */

	public static Account getSyncAccount(Context context) {
		// Get an instance of the Android account manager
		AccountManager accountManager =
				(AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

		// Create the account type and default account
		Account newAccount = new Account(
				context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

		// If the password doesn't exist, the account doesn't exist
		if ( null == accountManager.getPassword(newAccount) ) {

			/*
			 * Add the account and account type, no password or user data
			 * If successful, return the Account object, otherwise report an error.
			 */
			if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
				return null;
			}
			/*
			 * If you don't set android:syncable="true" in
			 * in your <provider> element in the manifest,
			 * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
			 * here.
			 */

			onAccountCreated(newAccount, context);
		}
		return newAccount;
	}

	private static void onAccountCreated(Account newAccount, Context context) {
		/*
		 * Since we've created an account
		 */
		SunshineSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

		/*
		 * Without calling setSyncAutomatically, our periodic sync will not be enabled.
		 */
		ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

		/*
		 * Finally, let's do a sync to get things started
		 */
		syncImmediately(context);
	}

	/**
	 * Helper method to schedule the sync adapter periodic execution
	 */
	public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
		Account account = getSyncAccount(context);
		String authority = context.getString(R.string.content_authority);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// we can enable inexact timers in our periodic sync
			SyncRequest request = new SyncRequest.Builder().
					syncPeriodic(syncInterval, flexTime).
					setSyncAdapter(account, authority).
					setExtras(new Bundle()).build();
			ContentResolver.requestSync(request);
		} else {
			ContentResolver.addPeriodicSync(account,
					authority, new Bundle(), syncInterval);
		}

	}

	public static void initializeSyncAdapter(Context context) {
		getSyncAccount(context);
	}


	private void notifyWeather() {
		Context context = getContext();

		//checking the last update and notify if it' the first of the day
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);

		boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
				Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));

		if(displayNotifications){

			String lastNotificationKey = context.getString(R.string.pref_last_notification);

			long lastSync = prefs.getLong(lastNotificationKey, 0);

			if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
				// Last sync was more than 1 day ago, let's send a notification with the weather.
				String locationQuery = Utility.getPreferedLocation(context);

				Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationQuery, System.currentTimeMillis());

				// we'll query our contentProvider, as always
				Cursor cursor = context.getContentResolver().query(weatherUri, NOTIFY_WEATHER_PROJECTION, null, null, null);

				if (cursor.moveToFirst()) {
					int weatherId = cursor.getInt(INDEX_WEATHER_ID);
					double high = cursor.getDouble(INDEX_MAX_TEMP);
					double low = cursor.getDouble(INDEX_MIN_TEMP);
					String desc = cursor.getString(INDEX_SHORT_DESC);

					int iconId = Utility.getIconResourceForWeatherCondition(weatherId);
					String title = context.getString(R.string.app_name);

					// Define the text of the forecast.
					String contentText = String.format(context.getString(R.string.format_notification),
							desc,
							Utility.formatTemperature(context, high),
							Utility.formatTemperature(context, low));

					//build your notification here.
					NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getContext())
					.setSmallIcon(iconId)
					.setContentTitle(title)
					.setContentText(contentText);

					//Making something to happend when the user clicks on the notification
					//In our case opening the app will be sufficient
					Intent resultIntent = new Intent(context, MainActivity.class);
					
					TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
					stackBuilder.addNextIntent(resultIntent);
					
					PendingIntent resultingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

					mBuilder.setContentIntent(resultingIntent);

					NotificationManager mNotificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
					 
					// WEATHER_NOTIFICATION_ID allows you to update the notification later on.
                    mNotificationManager.notify(WEATHER_NOTIFICATION_ID, mBuilder.build());
							
                    //refreshing last sync
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong(lastNotificationKey, System.currentTimeMillis());
                    editor.commit();
				}
				
				cursor.close();
			}
		}
	}
}

