package com.icetech.sunshineapp;

import java.text.DateFormat;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.icetech.sunshineapp.data.WeatherContract;

public class Utility {

	/**
	 * Get the user location setting
	 * @param context from which the method was called
	 * @return location set by the user
	 */
	public static String getPreferedLocation(Context context){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

		String location = pref.getString(context.getString(R.string.pref_location_key),
				context.getString(R.string.pref_location_default));
		
		return location;
	}
	
	/**
	 * Check if the temperature unit setting is Metric or Imperial
	 * @param context
	 * @return boolean, true if Metric; false otherwise
	 */
	public static boolean isMetric(Context context) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

		String tempUnit = pref.getString(context.getString(R.string.pref_temperature_key),
				context.getString(R.string.pref_unit_metric));
		
		return tempUnit.equalsIgnoreCase(context.getString(R.string.pref_unit_metric));
		
	}
	
	//Format the temperaure with repect to the units set by the user
	public static String formatTemperature(double temperature, boolean isMetric) {
		double temp;
		if(isMetric){
			temp = 9*temperature/5 + 32;
		}
		else {
			temp = temperature;
		}
		return String.format("%.0f", temp);
	}
	
	/**
	 * Produces a formated date from a date string
	 * @param dateText the input date string
	 * @return the Date object
	 */
	 static String formatDate(String dateText){
		Date date = WeatherContract.getDateFromDb(dateText);
		return DateFormat.getInstance().format(date);
		
	}
}
