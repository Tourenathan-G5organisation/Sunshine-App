package com.icetech.sunshineapp;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DetailsActivity extends Activity {
	
	private static final String LOG_TAG_ACTIVITY = DetailsActivity.class.getSimpleName();
	
	private static final String FORCAST_SHARE_HASHTAG = "#SunshineApp";
	private static String mForecastStr;
	
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
	 * A placeholder fragment containing a simple view.
	 */
	public static class DetailsFragment extends Fragment {

		//private static final String LOG_TAG_FRAGMENT = DetailsFragment.class.getSimpleName();
				
		
		
		
		public DetailsFragment() {
			setHasOptionsMenu(true);
		}

	
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_details,
					container, false);
			
			// The detail Activity is called via Intent. Inspect the Intent to get the data
						//passe to this activity
						Intent intent = getActivity().getIntent();
						
						if(intent != null  && intent.hasExtra(Intent.EXTRA_TEXT)){
							mForecastStr = intent.getExtras().getString(Intent.EXTRA_TEXT);
							
							((TextView) rootView.findViewById(R.id.details_text)).setText(mForecastStr);
						}
						
			return rootView;
		}
		
		
	}
}
