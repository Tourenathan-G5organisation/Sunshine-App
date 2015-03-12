package com.icetech.sunshineapp;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.preference.PreferenceManager;



public class MainActivity extends Activity {

	private final String LOG_TAG = MainActivity.class.getSimpleName();
			
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())
                    .commit();
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