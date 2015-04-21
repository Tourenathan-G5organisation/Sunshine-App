package service;

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

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.Time;
import android.util.Log;

import com.icetech.sunshineapp.data.WeatherContract;
import com.icetech.sunshineapp.data.WeatherContract.LocationEntry;
import com.icetech.sunshineapp.data.WeatherContract.WeatherEntry;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * 
 */
public class SunshineService extends IntentService {
	
	private static final String LOG_TAG = SunshineService.class.getSimpleName();
	public static final String LOCATION_QUERY_EXTRA = "lqe";

	
	public SunshineService() {
		super("SunshineService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent != null) {
			

			//we are passing the name of the town
			String locationParam = intent.getStringExtra(LOCATION_QUERY_EXTRA);


			/* Now making the http request to get the weather data from OpenWeatherMap */

			//These two need to be declared outside the try/catch block so that
			//they can be called in the finally block statement


			HttpURLConnection urlConnection = null;
			BufferedReader reader = null;

			//Will contain the raw json response as a string
			String forecastJsonStr = null;

			String format = "json";
			String unit = "metric";
			int numDays = 14;	


			 //params[0] is the town name
			//unitType = params[1]; //params[1] is the unit type prefered by the user
			//unitMetric = params[2];  //params[2] is the string constant for metric
			//unitImperial = params[3]; //params[3] is the string constant for imperial

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

			//Weather information. Each day's forcast info is an element of the "list" array.
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
				// normalized UTC date for all of our weather.

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
					getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, cvArray);

					Log.d(LOG_TAG, "FetchweatherTask complete. " + cVVector.size() + " inserted");

				}



			} catch (JSONException e) {
				
				e.printStackTrace();
			}

			//this will only happend if there was an error getting or parsing the forecast
			return;

		
		}
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
		Cursor cursor =  getContentResolver().query(WeatherContract.LocationEntry.CONTENT_URI, 
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

			Uri locationInsertUri = getContentResolver()
					.insert(LocationEntry.CONTENT_URI, locationValues);

			return ContentUris.parseId(locationInsertUri);
		}

	}
	
	public static class AlarmReciever extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			Intent sendIntent = new Intent(context, SunshineService.class);

			sendIntent.putExtra(SunshineService.LOCATION_QUERY_EXTRA,
					intent.getStringExtra(LOCATION_QUERY_EXTRA));
			context.startService(sendIntent);
			
		}
		
	}
	
	
}
