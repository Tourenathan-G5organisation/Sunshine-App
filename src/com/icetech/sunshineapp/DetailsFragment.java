package com.icetech.sunshineapp;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.icetech.sunshineapp.data.WeatherContract;


/**
 * A DetailsFragment fragment containing a simple view.
 */

public class DetailsFragment extends Fragment implements LoaderCallbacks<Cursor> {

	private static final int DETAIL_LOADER = 0;

	public static final String DATE_KEY = "date";
	public static final String LOCATION_KEY = "location";

	private static final String LOG_TAG = DetailsFragment.class.getSimpleName();

	private static final String FORCAST_SHARE_HASHTAG = "#SunshineApp";
	private static String mForecastStr;

	private String mLocation;

	String[] column = {WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
			WeatherContract.WeatherEntry.COLUMN_DATE,
			WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
			WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
			WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
			WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
			WeatherContract.WeatherEntry.COLUMN_PRESSURE,
			WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
			WeatherContract.WeatherEntry.COLUMN_DEGREES,
			WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
			WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
	};

	// These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
	// must change.
	public static final int COL_WEATHER_ID = 0;
	public static final int COL_WEATHER_DATE = 1;
	public static final int COL_WEATHER_DESC = 2;
	public static final int COL_WEATHER_MAX_TEMP = 3;
	public static final int COL_WEATHER_MIN_TEMP = 4;
	public static final int COL_WEATHER_HUMIDITY = 5;
	public static final int COL_WEATHER_PRESSURE = 6;
	public static final int COL_WEATHER_WIND_SPEED = 7;
	public static final int COL_WEATHER_DEGREES = 8;
	public static final int COL_WEATHER_CONDITION_ID = 9;

	private ImageView mIconView;
	private TextView mFriendlyDateView;
	private TextView mDateView;
	private TextView mDescriptionView;
	private TextView mHighTempView;
	private TextView mLowTempView;
	private TextView mHumidityView;
	private TextView mWindView;
	private TextView mPressureView;


	public DetailsFragment() {
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.details, menu);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			//open the setting activity
			startActivity(new Intent(getActivity(), SettingsActivity.class));

		}

		if(id == R.id.action_share){

			createShareForecastIntent();
		}

		return super.onOptionsItemSelected(item);

	}

	private void createShareForecastIntent() {

		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastStr + " " + FORCAST_SHARE_HASHTAG);
		if (shareIntent.resolveActivity(getActivity().getPackageManager()) != null) {
			startActivity(shareIntent);
		}
		else{
			Log.d(LOG_TAG, "Share action provider is null");
		}

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		
		super.onActivityCreated(savedInstanceState);
		if (null != savedInstanceState && null != savedInstanceState.getString(LOCATION_KEY) ) {
			mLocation = savedInstanceState.getString(LOCATION_KEY);
		}
		getLoaderManager().initLoader(DETAIL_LOADER, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_details,
				container, false);

		mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
		mDateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
		mFriendlyDateView = (TextView) rootView.findViewById(R.id.detail_day_textview);
		mDescriptionView = (TextView) rootView.findViewById(R.id.detail_forcast_textview);
		mHighTempView = (TextView) rootView.findViewById(R.id.detail_high_textview);
		mLowTempView = (TextView) rootView.findViewById(R.id.detail_low_textview);
		mHumidityView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
		mWindView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
		mPressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);

		return rootView;
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		
		super.onSaveInstanceState(outState);
		if (null != mLocation) {
			outState.putString(LOCATION_KEY, mLocation);
		}

	}

	@Override
	public void onResume() {

		super.onResume();

		if (null != mLocation && !mLocation.equals(Utility.getPreferedLocation(getActivity()).toLowerCase())) {

			getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
		}
		Log.d(LOG_TAG, "location value: " + mLocation);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		long date = getActivity().getIntent().getLongExtra(DATE_KEY, 0);
		//This is called when a new loader needs to be created
		//This fragment only uses one loader, so, don't care about checking the loader id

		if(date != 0){

			mLocation = Utility.getPreferedLocation(getActivity());
			mLocation = mLocation.toLowerCase();

			Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(mLocation, date);

			return new CursorLoader(
					getActivity(), 
					weatherUri, 
					column, 
					null,
					null, 
					null);
		}
		return null;
	}


	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (data.moveToFirst()) {
			final String SUFFIX = "\u00B0";

			// Read weather condition ID from cursor
			int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);

			//Use place holder image
			mIconView.setImageResource(R.drawable.ic_launcher);

			// Read description from cursor and update view        
			String description = data.getString(COL_WEATHER_DESC);
			mDescriptionView.setText(description);

			// Read date from cursor and update views for day of week and date            

			long date = data.getLong(COL_WEATHER_DATE);
			String friendlyDateText = Utility.getDayName(getActivity(), date);
			String dateText = Utility.getFormattedMonthDay(getActivity(), date);
			mFriendlyDateView.setText(friendlyDateText);
			mDateView.setText(dateText);            


			// Read high temperature from cursor and update view
			boolean isMetric = Utility.isMetric(getActivity());

			double high = data.getDouble(COL_WEATHER_MAX_TEMP);
			String highString = Utility.formatTemperature(high, isMetric)+SUFFIX;

			mHighTempView.setText(highString);

			// Read low temperature from cursor and update view
			double low = data.getDouble(COL_WEATHER_MIN_TEMP);
			String lowString = Utility.formatTemperature(low, isMetric)+SUFFIX;
			mLowTempView.setText(lowString);

			// Read humidity from cursor and update view
			float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
			mHumidityView.setText(String.format("Humidity: %1.0f %%", humidity));

			// Read wind speed and direction from cursor and update view
			float windSpeedStr = data.getFloat(COL_WEATHER_WIND_SPEED);
			float windDirStr = data.getFloat(COL_WEATHER_DEGREES);
			mWindView.setText(Utility.getFormattedWind(getActivity(), windSpeedStr, windDirStr));


			// Read pressure from cursor and update view
			float pressure = data.getFloat(COL_WEATHER_PRESSURE);
			mPressureView.setText(String.format("Pressure: %1.0f hPa", pressure));



			//Format the string that will be used for sharing temperature infos
			mForecastStr = String.format("%s - %s - %s/%s", dateText,
					description, highString, lowString);
		}

	}


	@Override
	public void onLoaderReset(Loader<Cursor> loader) {


	}


}
