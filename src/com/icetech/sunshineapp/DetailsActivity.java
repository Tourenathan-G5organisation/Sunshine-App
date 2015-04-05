package com.icetech.sunshineapp;

import android.app.Activity;
import android.os.Bundle;

public class DetailsActivity extends Activity {

	private static final String LOG_TAG_ACTIVITY = DetailsActivity.class.getSimpleName();
	private static final String DETAILFRAGMENT_TAG = "DFTAG";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);
		if (savedInstanceState == null) {

			// Create detail fragment and add to the detail activity
			// using fragment transaction
			long date = getIntent().getLongExtra(DetailsFragment.DATE_KEY, 0);

			Bundle bundle = new Bundle();
			bundle.putLong(DetailsFragment.DATE_KEY, date);

			DetailsFragment fragment = new DetailsFragment();
			
			fragment.setArguments(bundle);

			getFragmentManager().beginTransaction()
			.add(R.id.weather_detail_container, fragment, DETAILFRAGMENT_TAG)
			.commit();
		}
	}



}
