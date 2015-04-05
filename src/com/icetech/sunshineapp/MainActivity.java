package com.icetech.sunshineapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;



public class MainActivity extends Activity {

	private final String LOG_TAG = MainActivity.class.getSimpleName();

	private String mLocation;

	private boolean mTwoPane = false;
	
	private static final String DETAILFRAGMENT_TAG = "DFTAG";
	private final String FORECASTFRAGMENT_TAG = "FFTAG";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mLocation = Utility.getPreferedLocation(this);
		mLocation = mLocation.toLowerCase();

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (findViewById(R.id.weather_detail_container) != null) {
			// The detail container will be present only in the large
			// screen layout (res/layout-sw600dp), if this view is present
			// then the activity should be in a two pane mode
			mTwoPane = true;

			// In two pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a fragment 
			// transaction

			if (savedInstanceState == null) {
				getFragmentManager().beginTransaction()
				.replace(R.id.weather_detail_container, new DetailsFragment(), DETAILFRAGMENT_TAG)
				.commit();
			}
		}
		else {
			mTwoPane = false;
		}
		
		//        if (savedInstanceState == null) {
		//            getFragmentManager().beginTransaction()
		//                    .add(R.id.container, new ForecastFragment(), FORECASTFRAGMENT_TAG)
		//                    .commit();
		//        }
	}

	@Override
	protected void onResume() {

		super.onResume();
		String location = Utility.getPreferedLocation( this );
		location = location.toLowerCase();
		// update the location in our second pane using the fragment manager
		if (location != null && !location.equals(mLocation)) {

			ForecastFragment fF = (ForecastFragment)getFragmentManager().findFragmentByTag(FORECASTFRAGMENT_TAG);
			if ( null != fF) {
				fF.onLocationChanged();
			}
			
			DetailsFragment dF = (DetailsFragment) getFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
			if ( null != dF) {
				//dF.onLocationChanged();
			}
			mLocation = location;
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_map) {
			//make a call to the map application

			onPreferredLocationInMap();        	
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	/*Ths method is use to call the map application to display the 
	user prefered location on the map
	 */

	private void onPreferredLocationInMap(){

		SharedPreferences sharePref = PreferenceManager.getDefaultSharedPreferences(this);

		String location = sharePref.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));

		//To get more about the data format for implicit intent
		//visit developer.android.com and search for common intent

		Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
				.appendQueryParameter("q", location).build();

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(geoLocation);

		if (intent.resolveActivity(getPackageManager()) != null) {
			startActivity(intent);
		} else {
			Log.v(LOG_TAG, "could not call " + location + " map" );
		}



	}
}
