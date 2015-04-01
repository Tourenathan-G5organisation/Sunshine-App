package com.icetech.sunshineapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link Cursor} to a {@link android.widget.ListView}.
 */

public class ForecastAdapter extends CursorAdapter {


	// Flag to determine if we want to use a separate view for "today".
	private boolean mUseTodayLayout = true;

	public ForecastAdapter(Context context, Cursor c, int flag) {
		super(context, c, flag);

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		return LayoutInflater.from(context).inflate(R.layout.list_item_forcast, parent, false);

	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		//Read weather icon ID from cursor
		int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);

		//USe placeholder image for now
		ImageView iconview = (ImageView) view.findViewById(R.id.list_item_icon);
		iconview.setImageResource(R.drawable.ic_launcher);

		//Read date from cursor
		String dateString = cursor.getString(ForecastFragment.COL_WEATHER_DATE);
		//Find text view and set formatted date on it
		TextView dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
		//USe this for now
		dateView.setText(Utility.formatDate(dateString)); 

		//Read weather forecast from cursor
		String description = cursor.getString(ForecastFragment.COL_WEATHER_SHORT_DESC);
		//Find text view and set weather description on it
		TextView descriptionView = (TextView) view.findViewById(R.id.list_item_forcast_textview);
		descriptionView.setText(description);

		// Read user preference for metric or imperial temperature units
		boolean isMetric = Utility.isMetric(context);

		// Read high temperature from cursor
		double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
		TextView highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
		highTempView.setText(Utility.formatTemperature(high, isMetric)+"\u00B0");
		
		// Read low temperature from cursor
		double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
		TextView lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
		lowTempView.setText(Utility.formatTemperature(low, isMetric)+"\u00B0");

	}

}
