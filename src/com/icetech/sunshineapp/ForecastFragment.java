package com.icetech.sunshineapp;
import java.util.Date;

import sync.SunshineSyncAdapter;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.icetech.sunshineapp.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */

public class ForecastFragment extends Fragment implements LoaderCallbacks<Cursor> {

	public  final String LOG_TAG = ForecastFragment.class.getSimpleName();

	private static final int FORECAST_LOADER = 0;
	private String mLocation;

	private int mPosition = ListView.INVALID_POSITION;
	private static final String SELECTED_KEY = "selected_position";

	ListView mListview;
	private boolean mUseTodayLayout;
	private long mSelectedDate;

	//For the forecast view, we are showing only a subset of the stored data.
	//Specify the columns needed
	private static final String[] FORECAST_COULMNS = {
		//In this case the id need to be fully qualified with a table name,
		//since the content provider joins the location and weather table in
		// the background (both have an _id column)
		//On one hand that is annoying, on the other hand, you can search the  weather table
		//using the postal code (or city name) in the location table. so the
		//convenience is worth it

		WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID ,
		WeatherContract.WeatherEntry.COLUMN_DATE, 
		WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
		WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
		WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
		WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
		WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
		WeatherContract.LocationEntry.COLUMN_COORD_LAT,
		WeatherContract.LocationEntry.COLUMN_COORD_LONG
	};

	//These are column indices tied to FORECAST_COULUMN. IF FORECAST_COLUMN changes
	//thses must change
	public static final int COL_WEATHER_ID = 0;
	public static final int COL_WEATHER_DATE = 1;
	public static final int COL_WEATHER_SHORT_DESC = 2;
	public static final int COL_WEATHER_MAX_TEMP = 3;
	public static final int COL_WEATHER_MIN_TEMP = 4;
	public static final int COL_LOCATION_SETTING = 5;
	static final int COL_WEATHER_CONDITION_ID = 6;
	public static final int COL_COORD_LAT = 7;
	public static final int COL_COORD_LONG = 8;

	/**
	 * A callback interface that all activities performing this fragment
	 * must implement. This mechanism allows activities to be notified
	 * of an item selection
	 * 
	 */
	public interface Callback{

		/*
		 * DetailFragmentCallback for when an item has being selected
		 */
		public void onItemSelected( long date);
	}
	public ForecastFragment() {

	}



	private static ForecastAdapter listData;

	static String unitType = "metric" ; //this will be used to check the user choosen unit type


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {		
		getLoaderManager().initLoader(FORECAST_LOADER, null, this);
		super.onActivityCreated(savedInstanceState);
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

		case R.id.action_map:			
				//make a call to the map application
				onPreferredLocationInMap();	
			
			break;
			
		/*case R.id.action_refresh:
			updateWeather();

			break;*/

		}
		return super.onOptionsItemSelected(item);
	};



	public void setUseTodayLaout( boolean useTodayLayout) {
		mUseTodayLayout = useTodayLayout;
		if (listData != null) {
			listData.setUseTodayLayout(mUseTodayLayout);
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);

		listData = new ForecastAdapter(getActivity(), null, 0);
		listData.setUseTodayLayout(mUseTodayLayout);


		//Get a reference to the list view and attach adapter to it
		mListview = (ListView) rootView.findViewById(R.id.listView_forecast);
		mListview.setAdapter(listData);

		mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View v, int position,
					long id) {
				ForecastAdapter adapter = (ForecastAdapter) adapterView.getAdapter();
				Cursor cursor = adapter.getCursor();

				if(null != cursor && cursor.moveToPosition(position)){
					mSelectedDate = cursor.getLong(COL_WEATHER_DATE);
					((Callback) getActivity()).onItemSelected(mSelectedDate);
				}

				mPosition = position;

			}
		});

		if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
			// The listview probably hasn't even been populated yet.  Actually perform the
			// swapout in onLoadFinished.
			mPosition = savedInstanceState.getInt(SELECTED_KEY);
		}

		return rootView;
	}



	private void updateWeather(){

		SunshineSyncAdapter.syncImmediately(getActivity());


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

		//Sort order: Ascending by date
		String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

		mLocation = PreferenceManager.getDefaultSharedPreferences(getActivity())
				.getString(getString(R.string.pref_location_key),
						getString(R.string.pref_location_default));

		mLocation = mLocation.toLowerCase();

		Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(mLocation, System.currentTimeMillis());
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

	@SuppressLint("NewApi") @Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		Log.d(LOG_TAG, "Count data: " + data.getCount());
		listData.swapCursor(data); 
		if (mPosition != ListView.INVALID_POSITION) {
			// If we don't need to restart the loader, and there's a desired position to restore
			// to, do so now.
			mListview.setSelection(mPosition);
		}


	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		listData.swapCursor(null);

	}

	// since we read the location when we create the loader, all we need to do is restart things
	public void onLocationChanged( ) {
		updateWeather();
		getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
	}

	//just reload the loader when the temperature unit changes
	public void ontempUnitChanged (){
		getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// When tablets rotate, the currently selected list item needs to be saved.
		// When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
		// so check for that before storing.
		if (mPosition != ListView.INVALID_POSITION) {
			outState.putInt(SELECTED_KEY, mPosition);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onResume() {

		super.onResume();
		if (mPosition == ListView.INVALID_POSITION && ((MainActivity) getActivity()).isTwopane()) {
			((MainActivity) getActivity()).initSecondPane();

		}
	}


	/*This method is use to call the map application to display the 
	user preferred location on the map
	 */

	private void onPreferredLocationInMap(){
		if(null != listData){
			Cursor c = listData.getCursor();

			if( null != c ){
				c.moveToPosition(0);

				String posLat = c.getString(COL_COORD_LAT);
				String posLong = c.getString(COL_COORD_LONG);

				//To get more about the data format for implicit intent
				//visit developer.android.com and search for common intent

				Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(geoLocation);

				if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
					startActivity(intent);
				} else {
					Log.d(LOG_TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
				}
			}
		}





	}

}
