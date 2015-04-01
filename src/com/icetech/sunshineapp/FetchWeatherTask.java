package com.icetech.sunshineapp;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.acl.Owner;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.icetech.sunshineapp.data.WeatherContract;
import com.icetech.sunshineapp.data.WeatherContract.LocationEntry;
import com.icetech.sunshineapp.data.WeatherContract.WeatherEntry;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.test.UiThreadTest;
import android.util.Log;



public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

	private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
	Context mContext;
	
	public FetchWeatherTask(Context context){
		mContext = context;
	}
	
	String unitType; //this will determine which unit type to show the temperature (metric or Imperial)
	String unitMetric;
	String unitImperial;
	/**
	 * Use this method to change the date into readable format
	 * @param time is the time in seconds which will be use to produce
	 * the readable date fornat
	 * @return is the readable date format obatined in the form Tue, Jan 01
	 */
	private String getReadableDateString(Long time) {
		//because the API return a Unix timestamp measure in seconds
		// the time must be change in milliseconds before being used
		// and converted to a valid date
		Date date = new Date(time * 1000);		
		SimpleDateFormat format = new SimpleDateFormat("EEE, MMM dd");
		return format.format(date).toString();		
	}
	
	/**
	 * prepare the weather high low for presentation
	 */
	
	private String formatHighLows(double high, double low) {
		//Data is fetched in celsius by default
		//IF user prefers to see in fahrenheit convert values here
		//We do this rather than fetching in fahrenheit so that user can
		//change this option without us having to refetch the data once
		//We start storing the values in a databse
		
		
		//for presentaion assume the user does not care about  
		//tenths of degress
		
		if(unitType.equals(unitImperial)){
			high = (high * 1.8) + 32;
			low = (low * 1.8) + 32;
		}
		else if (!unitType.equals(unitMetric)) {
			Log.d(LOG_TAG, "Unit type not found " + unitType);
			
		}		
		
		long roundedHigh = Math.round(high);
		long roundedLow	= Math.round(low);
		String highLowStr = roundedHigh + "/" + roundedLow;
		return highLowStr;
	}
	
	private String[] getWeatherFromJson(String forecastJsonStr, int numDays)
			throws JSONException {
		
		//These are the names of the Json objects that need to be extracted
		final String OWN_LIST = "list";
		final String OWN_WEATHER = "weather";
		final String OWN_TEMPERATURE = "temp";
		final String OWN_MAX = "max";
		final String OWN_MIN = "min";
		final String OWN_DATETIME = "dt";
		final String OWN_DESCRIBTION = "main";
				
		JSONObject forecastJson = new JSONObject(forecastJsonStr);
		JSONArray weatherArray = forecastJson.getJSONArray(OWN_LIST);
		
		String[] resultStr = new String[numDays];
		for (int i = 0; i < resultStr.length; i++) {
			
			// for now, we use the format "Day, description, high/low"
			String day;
			String description;
			String highLow;
			
			//Get the Json object representing the day
			JSONObject dayForecast = weatherArray.getJSONObject(i);
			
			//The date and Time is returned as Long, we need to convert it into something
			//human readable. Since most people won't know that "1400356800" is this Saturday
			long dateTime = dayForecast.getLong(OWN_DATETIME);
			day = getReadableDateString(dateTime); //change this time in readable format
			
			//Description whic is one element long is in a child called weather
			JSONObject weatherObject = dayForecast.getJSONArray(OWN_WEATHER).getJSONObject(0);
			description = weatherObject.getString(OWN_DESCRIBTION);
			
			
			//Temperature are in a child object called Temp. try not to name Variables
			//"temp" when working with temperature. Its confuse everybody
			JSONObject temperatureObject = dayForecast.getJSONObject(OWN_TEMPERATURE);
			double high = temperatureObject.getDouble(OWN_MAX);
			double low = temperatureObject.getDouble(OWN_MIN);
			
			highLow = formatHighLows(high, low);
			
			resultStr[i] = day + " - " + description + " - " + highLow;
		}
		
		for (String str : resultStr) {
			Log.v(LOG_TAG, "Forcast entry" + str);
		}
		
		return resultStr; // return formated weather description for the precise num of days
		
		
	}
	
	
	
	@Override
	protected String[] doInBackground(String... params) {
 //we are passing the name of the town
		
		//if there is no city name, then there is no need to check for weather infos
		if(params.length == 0){
			return null;
		}
		
		
		
		/* Now making the http request to get the weather data from OpenWeather */

		//These two need to be declared outside the try/catch block so that
				//they can be called in the finally block statement

		
				HttpURLConnection urlConnection = null;
				BufferedReader reader = null;

				//Will contain the raw json response as a string
				String forecastJsonStr = null;
				
				String format = "json";
				String unit = "metric";
				int numDays = 14;	
				
				
				String locationParam = params[0]; //params[0] is the town name
				unitType = params[1]; //params[1] is the unit type prefered by the user
				unitMetric = params[2];  //params[2] is the string constant for metric
				unitImperial = params[3]; //params[3] is the string constant for imperial
				
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
						return null;
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
						return null;
					}

					forecastJsonStr = buffer.toString();
					Log.v(LOG_TAG, "Forcast JSON String" + forecastJsonStr);
													
					
				} catch (IOException e) {
					Log.e(LOG_TAG, "Error", e);
					e.printStackTrace();
					//If the code didn't successfully get the weather data there is no point
					//in passing it
					return null;
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
				
				/*try {
					
					return getWeatherFromJson(forecastJsonStr, numDays);
					
				} catch (JSONException e) {
					Log.e(LOG_TAG, e.getMessage(), e);
					e.printStackTrace();
				}*/
		
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
					
					for (int i = 0; i < weatherArray.length(); i++) {
						
						//Get the Json object representing the day
						JSONObject dayForecast = weatherArray.getJSONObject(i);
						
						long dateTime = dayForecast.getLong(OWM_DATETIME);
						Date date = new Date(dateTime * 1000);//converting to milliseconds before changing
						
						Log.d(LOG_TAG, "Weather Date: " + WeatherContract.getDbDateString(date));
						
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
						weathervalues.put(WeatherEntry.COLUMN_DATE, WeatherContract.getDbDateString(date));
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
							mContext.getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, cvArray);
							
							Log.d(LOG_TAG, "FetchweatherTask complete. " + cVVector.size() + " inserted");
							
						}
						
					
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//this will only happend if there was an error getting or parsing the forecast
		return null;
	
	}
	
	
	
	/*// This method is used to update the UI Thread with information obtained from doInBackground method
		@Override
		protected void onPostExecute(String[] result) {
			if(result != null){
			ForecastFragment.updateListAdapter(result); // passes the formated data to this method to update the
			                                                        //list view with valid data from the server
			}
			//super.onPostExecute(result);
		}
*/
	
	
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
		Cursor cursor =  mContext.getContentResolver().query(WeatherContract.LocationEntry.CONTENT_URI, 
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
			
			Uri locationInsertUri = mContext.getContentResolver()
					.insert(LocationEntry.CONTENT_URI, locationValues);
			
			return ContentUris.parseId(locationInsertUri);
		}
		
	}
}
