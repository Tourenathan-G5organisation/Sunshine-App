package com.icetech.sunshineapp;

import android.app.Activity;
import android.os.Bundle;

public class DetailsActivity extends Activity {

	private static final String LOG_TAG_ACTIVITY = DetailsActivity.class.getSimpleName();

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
			.add(R.id.container, new DetailsFragment()).commit();
		}
	}

	

}
