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


	private static final int VIEW_TYPE_TODAY = 0;
	private static final int VIEW_TYPE_FUTURE_DAY = 1;
	private static final int VIEW_TYPE_COUNT = 2;


	// Flag to determine if we want to use a separate view for "today".
	private boolean mUseTodayLayout;

	public ForecastAdapter(Context context, Cursor c, int flag) {
		super(context, c, flag);

	}


	public static class ViewHolder {
		public final ImageView iconView;
		public final TextView dateView;
		public final TextView descriptionView;
		public final TextView highTempView;
		public final TextView lowTempView;

		public ViewHolder(View view) {
			iconView = (ImageView) view.findViewById(R.id.list_item_icon);
			dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
			descriptionView = (TextView) view.findViewById(R.id.list_item_forcast_textview);
			highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
			lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
		}
	}

	@Override
	public int getItemViewType(int position) {

		return (position == 0 && mUseTodayLayout)? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
	}

	@Override
	public int getViewTypeCount() {

		return VIEW_TYPE_COUNT;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		//Get the view type
		int viewType = cursor.getPosition();
		int layoutId = -1;

		switch (getItemViewType(viewType)) {
		case VIEW_TYPE_TODAY:
			layoutId = R.layout.list_item_forecast_today;
			break;

		case VIEW_TYPE_FUTURE_DAY:
			layoutId = R.layout.list_item_forcast;

			break;
		}

		View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
		ViewHolder viewHolder = new ViewHolder(view);
		view.setTag(viewHolder);

		return view;

	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		//Our viewHolder already contains references to the relevant views, so set
		//appropriate through the view holder references instead of costly findViewById
		ViewHolder viewHolder = (ViewHolder) view.getTag();

		//Read weather icon ID from cursor
		int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);

		int viewType = getItemViewType(cursor.getPosition());
		switch (viewType) {
		case VIEW_TYPE_TODAY: {
			// Get weather icon
			viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
			break;
		}
		case VIEW_TYPE_FUTURE_DAY: {
			// Get weather icon
			viewHolder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(weatherId));
			break;
		}
		}
		//USe placeholder image for now

		//viewHolder.iconView.setImageResource(R.drawable.ic_launcher);

		//Read date from cursor
		//String dateString = cursor.getString(ForecastFragment.COL_WEATHER_DATE);
		long dateInMillis = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);

		//USe this for now
		//viewHolder.dateView.setText(Utility.formatDate(dateString)); 
		// Find TextView and set formatted date on it
		viewHolder.dateView.setText(Utility.getFriendlyDayString(context, dateInMillis));

		//Read weather forecast from cursor
		String description = cursor.getString(ForecastFragment.COL_WEATHER_SHORT_DESC);

		//Find text view and set weather description on it		
		viewHolder.descriptionView.setText(description);

		//For Accessibility, add a content description to the icon
		viewHolder.iconView.setContentDescription(description);
		
		// Read user preference for metric or imperial temperature units
		boolean isMetric = Utility.isMetric(context);

		// Read high temperature from cursor
		double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);

		viewHolder.highTempView.setText(Utility.formatTemperature(high, isMetric)+"\u00B0");

		// Read low temperature from cursor
		double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);

		viewHolder.lowTempView.setText(Utility.formatTemperature(low, isMetric)+"\u00B0");

	}

	
	public void setUseTodayLayout(boolean useTodayLaout) {
		mUseTodayLayout = useTodayLaout;
	}
}
