package com.icetech.sunshineapp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import com.icetech.sunshineapp.R;
import com.icetech.sunshineapp.R.string;
import com.icetech.sunshineapp.data.WeatherContract;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

/**
 * A placeholder fragment containing a simple view.
 */

public class ForecastFragment extends Fragment implements LoaderCallbacks<Cursor> {

	public  final String LOG_TAG = ForecastFragment.class.getSimpleName();
	
	private static final int FORECAST_LOADER = 0;
	private String mLocation;
	
	//For the forecast view, we are showing only a subset of the stored data.
	//Specify the coulumns needed
	private static final String[] FORECAST_COULMNS = {
		//In this case the id need to be fully qualified with a table name,
		//since the content provider joins the location and weather table in
		// the background (both have an _id column)
		//On one hand that is annoying, on the other hand, you can search the  weather table
		//using the postal code (or city name) in the location table. so the
		//convinience is worth it

		WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID ,
		WeatherContract.WeatherEntry.COLUMN_DATE, 
		WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
		WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
		WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
		WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
	};
	
	//These are column indices tied to FORECAST_COULUMN. IF FORECAST_COLUMN changes
	//thses must change
	public static final int COL_WEATHER_ID = 0;
	public static final int COL_WEATHER_DATE = 1;
	public static final int COL_SHORT_DESC = 2;
	public static final int COL_MAX_TEMP = 3;
	public static final int COL_MIN_TEMP = 4;
	public static final int COL_LOCATION_SETTING = 5;
	


	public ForecastFragment() {

	}

	//static ArrayAdapter<String> listData; //Adapter to be used to render the content of the list view

	private static SimpleCursorAdapter listData;
	
	static String unitType = "metric" ; //this will be used to check the user choosen unit type

	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {		
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(FORECAST_LOADER, null, this);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Add this line in order  for this fragment to handle menu events
		setHasOptionsMenu(true);

	};

	@Override
	public void onCreateOptionsMenu(android.view.Menu menu, android.view.MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		//menu.clear();

		inflater.inflate(R.menu.forecastfagment, menu);
	};

	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {

		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		Toast.makeText(getActivity(), item.getTitle(), Toast.LENGTH_LONG).show();

		switch (item.getItemId()) {
		case R.id.action_settings:
			//return true;
			Intent intent = new Intent(getActivity(), SettingsActivity.class);
			startActivity(intent);

			break;

		case R.id.action_refresh:
			updateWeather();

			break;

		}
		return super.onOptionsItemSelected(item);
	};


	@Override
	public void onStart() {
		super.onStart();
		updateWeather();
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);

		//Creating an Array of string which will be used as fake data(dummy data) for simulation of the list

		String[] foreCast = {
				"Today - Sunny - 88/63",
				"Tommorrow - Foggy - 70/46",
				"Wed - Cloudy - 72/63",
				"Thurs - Rainny - 64/51",
				"Fri - Fuggy - 70/46",
				"Sat - Sunny - 76/68",
				"Sun - Sunny - 80/68"
		};

		ArrayList<String> weekForeCast = new ArrayList<String>(Arrays.asList(foreCast));

		/*listData = new ArrayAdapter<String>(
				//The current context. this fragment parent activity
				getActivity(),
				//The ID of the list item layout
				R.layout.list_item_forcast,
				//the ID of the text view to populate
				R.id.list_item_forcast_textview,
				//Forecast Data
				new ArrayList<String>());*/
		
		listData = new SimpleCursorAdapter(
				getActivity(),
				R.layout.list_item_forcast,
				null, 
				//These column name
				new String[]{WeatherContract.WeatherEntry.COLUMN_DATE,
			WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
			WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
			WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
			}, 
				new int[]{
			R.id.list_item_date_textview,
			R.id.list_item_forcast_textview,
			R.id.list_item_high_textview,
			R.id.list_item_low_textview
		},
				0
		);
		
		

		//Get a reference to the list view and attach adapter to it
		ListView listview = (ListView) rootView.findViewById(R.id.listView_forecast);
		listview.setAdapter(listData);

		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long id) {
				/*String forecast = listData.getItem(position);
				Toast.makeText(getActivity(), "Clicked item at position: " + Integer.toString(position) + "\n" + forecast   , Toast.LENGTH_LONG).show();

				Intent intent = new Intent(getActivity(), DetailsActivity.class)
				.putExtra(Intent.EXTRA_TEXT, forecast);
				startActivity(intent);// start a new actitvity and passing it some data
*/			}
		});



		return rootView;
	}

	/**
	 * this method is used passe the formated data to the apater which is to render
	 * the content of the list view
	 * @param str is an Array of formated weather data from the server
	 */
	public static void updateListAdapter( String[] str){
		if(str != null){
			//listData.clear();
			for (String s : str) {

				//listData.add(s); //Add the New data from the server to the list view
			}
		}
	}

	private void updateWeather(){
		FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());

		String location = pref.getString(getString(R.string.pref_location_key),
				getString(R.string.pref_location_default));
		
		location = location.toLowerCase();

		String unitType = pref.getString(getString(R.string.pref_temperature_key), getString(R.string.pref_unit_metric));

		//passing the town name, temperature unit type and the different unit type
		weatherTask.execute(location, unitType, getString(R.string.pref_unit_metric), getString(R.string.pref_unit_imperial)); 
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		//This is called when a new loader needs to be created
		//This fragment only uses one loader, so, don't care about checking the loader id
		
		//To only show current and future date, get the string representation of today
		//and filter the query to return weather only for date after or including today
		//only return data after today
		
		String startDate = WeatherContract.getDbDateString(new Date());
		Log.d(LOG_TAG, startDate);
		//Sort order: Acending by date
		String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
		
		mLocation = PreferenceManager.getDefaultSharedPreferences(getActivity())
				.getString(getString(R.string.pref_location_key),
				getString(R.string.pref_location_default));
		
		mLocation = mLocation.toLowerCase();
		Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(mLocation, startDate);
		//Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocation(mLocation);
	
		Log.d(LOG_TAG, " Uri " + weatherForLocationUri.toString());
		
		//Now create and return a CursorLoader that will take care of
		//creating a cursor loader for the data being displayed
		return new CursorLoader(getActivity(), 
				weatherForLocationUri, 
				FORECAST_COULMNS, 
				null, 
				null, 
				sortOrder);
		
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		Log.d(LOG_TAG, "Count data: " + data.getCount());
		listData.swapCursor(data);

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		listData.swapCursor(null);

	}



}
