package com.icetech.sunshineapp;

import android.app.Activity;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.icetech.sunshineapp.data.WeatherContract;

public class DetailsActivity extends Activity {

	private static final String LOG_TAG_ACTIVITY = DetailsActivity.class.getSimpleName();

	private static final String FORCAST_SHARE_HASHTAG = "#SunshineApp";
	private static String mForecastStr;


	private static final int DETAIL_LOADER = 0;

	public static final String DATE_KEY = "date";
	public static final String LOCATION_KEY = "location";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
			.add(R.id.container, new DetailsFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.details, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			//open the setting activity
			startActivity(new Intent(this, SettingsActivity.class));
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
		if (shareIntent.resolveActivity(getPackageManager()) != null) {
			startActivity(shareIntent);
		}
		else{
			Log.d(LOG_TAG_ACTIVITY, "Share action provider is null");
		}

	}



	/**
	 * A DetailsFragment fragment containing a simple view.
	 */
	public static class DetailsFragment extends Fragment implements LoaderCallbacks<Cursor> {

		//private static final String LOG_TAG_FRAGMENT = DetailsFragment.class.getSimpleName();
		private String mLocation;



		public DetailsFragment() {
			setHasOptionsMenu(true);
		}


		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
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

			// The detail Activity is called via Intent. Inspect the Intent to get the data
			//passe to this activity
//			Intent intent = getActivity().getIntent();
//
//			if(intent != null  && intent.hasExtra(Intent.EXTRA_TEXT)){
//				mForecastStr = intent.getExtras().getString(Intent.EXTRA_TEXT);
//
//				((TextView) rootView.findViewById(R.id.details_text)).setText(mForecastStr);
//			}

			return rootView;
		}


		@Override
		public void onSaveInstanceState(Bundle outState) {
			// TODO Auto-generated method stub
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
			Log.d(LOG_TAG_ACTIVITY, "location value: " + mLocation);
		}

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			String dateString = getActivity().getIntent().getStringExtra(DATE_KEY);
			//This is called when a new loader needs to be created
			//This fragment only uses one loader, so, don't care about checking the loader id
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

			mLocation = Utility.getPreferedLocation(getActivity());
			mLocation = mLocation.toLowerCase();
			
			Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(mLocation, dateString);

			return new CursorLoader(
					getActivity(), 
					weatherUri, 
					column, 
					null,
					null, 
					null);
		}


		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			if (data.moveToFirst()) {

				String description = 
						data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC));
				String dateText = 
						data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE));

				double high = 
						data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP));

				double low = 
						data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP));

				boolean isMetric = Utility.isMetric(getActivity());

				TextView dateView = (TextView) getView().findViewById(R.id.detail_date_textview);
				TextView forecastView = (TextView) getView().findViewById(R.id.detail_forcast_textview);
				TextView highView = (TextView) getView().findViewById(R.id.detail_high_textview);
				TextView lowView = (TextView) getView().findViewById(R.id.detail_low_textview);


				dateView.setText(Utility.formatDate(dateText));
				forecastView.setText(description);
				highView.setText(Utility.formatTemperature(high, isMetric)+"\u00B0");
				lowView.setText(Utility.formatTemperature(low, isMetric)+"\u00B0");

				//Format the string that will be used for sharing temperature infos
				mForecastStr = String.format("%s - %s - %s/%s", dateView.getText(),
						forecastView.getText(), highView.getText(), lowView.getText());
			}

		}


		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			// TODO Auto-generated method stub

		}


	}
}
