package com.icetech.sunshineapp;
import java.util.ArrayList;
import java.util.Arrays;

import com.icetech.sunshineapp.R;
import com.icetech.sunshineapp.R.string;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * A placeholder fragment containing a simple view.
 */

public class ForecastFragment extends Fragment {

	public ForecastFragment() {

	}

	static ArrayAdapter<String> listData; //Adapter to be used to render the content of the list view

	static String unitType = "metric" ; //this will be used to check the user choosen unit type
	
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

		listData = new ArrayAdapter<String>(
				//The current context. this fragment parent activity
				getActivity(),
				//The ID of the list item layout
				R.layout.list_item_forcast,
				//the ID of the text view to populate
				R.id.list_item_forcast_textview,
				//Forecast Data
				new ArrayList<String>());

		//Get a reference to the list view and attach adapter to it
		ListView listview = (ListView) rootView.findViewById(R.id.listView_forecast);
		listview.setAdapter(listData);

		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long id) {
				String forecast = listData.getItem(position);
				Toast.makeText(getActivity(), "Clicked item at position: " + Integer.toString(position) + "\n" + forecast   , Toast.LENGTH_LONG).show();

				Intent intent = new Intent(getActivity(), DetailsActivity.class)
				.putExtra(Intent.EXTRA_TEXT, forecast);
				startActivity(intent);// start a new actitvity and passing it some data
			}
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
			listData.clear();
			for (String s : str) {

				listData.add(s); //Add the New data from the server to the list view
			}
		}
	}

	private void updateWeather(){
		FetchWeatherTask weatherTask = new FetchWeatherTask();

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());

		String location = pref.getString(getString(R.string.pref_location_key),
				getString(R.string.pref_location_default));
		
		String unitType = pref.getString(getString(R.string.pref_temperature_key), getString(R.string.pref_unit_metric));
		
		//passing the town name, temperature unit type and the different unit type
		weatherTask.execute(location, unitType, getString(R.string.pref_unit_metric), getString(R.string.pref_unit_imperial)); 
	}
	
		
	
}
