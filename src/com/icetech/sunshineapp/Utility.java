package com.icetech.sunshineapp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.Time;

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
	 * Get the user temperature setting
	 * @param context from which the method was called
	 * @return temperature unit set by the user
	 */
	public static String getPreferedTempUnit(Context context){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		String tempUnit = pref.getString(context.getString(R.string.pref_temperature_key),
				context.getString(R.string.pref_unit_metric));
		
		return tempUnit;
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

	//Format the temperature with respect to the units set by the user
	public static String formatTemperature(Context context, double temperature) {
		
		
		if (!isMetric(context)) {
            temperature = (temperature * 1.8) + 32;
        }
		
		return String.format("%.0f", temperature);
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

	// Format used for storing dates in the database.  ALso used for converting those strings
	// back into date objects for comparison/processing.
	public static final String DATE_FORMAT = "yyyyMMdd";

	/**
	 * Converts db date format to the format "Month day", e.g "June 24".
	 * @param context Context to use for resource localisation
	 * @param dateInMillis The db formatted date string, expected to be of the form specified
	 *                in Utility.DATE_FORMAT
	 * @return The day in the form of a string formatted "December 6"
	 */
	public static String getFormattedMonthDay(Context context, long dateInMillis ) {
		Time time = new Time();
		time.setToNow();
		SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
		SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
		String monthDayString = monthDayFormat.format(dateInMillis);
		return monthDayString;
	}


	/**
	 * Given a day, returns just the name to use for that day.
	 * E.g "today", "tomorrow", "Wednesday".
	 *
	 * @param context Context to use for resource localisation
	 * @param dateInMillis The date in milliseconds
	 * @return
	 */

	public static String getDayName(Context context, long dateInMillis) {
		// If the date is today, return the localised version of "Today" instead of the actual
		// day name.

		Time t = new Time();
		t.setToNow();
		int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
		int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);

		if (julianDay == currentJulianDay) {
			return context.getString(R.string.today);
		} else if ( julianDay == currentJulianDay +1 ) {
			return context.getString(R.string.tomorrow);
		} else {
			Time time = new Time();
			time.setToNow();
			// Otherwise, the format is just the day of the week (e.g "Wednesday".
			SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
			return dayFormat.format(dateInMillis);
		}
	}

	/**
	 * Helper method to convert the database representation of the date into something to display
	 * to users.  As classy and polished a user experience as "20140102" is, we can do better.
	 *
	 * @param context Context to use for resource localisation
	 * @param dateInMillis The date in milliseconds
	 * @return a user-friendly representation of the date.
	 */

	public static String getFriendlyDayString(Context context, long dateInMillis) {
		// The day string for forecast uses the following logic:
		// For today: "Today, June 8"
		// For tomorrow:  "Tomorrow"
		// For the next 5 days: "Wednesday" (just the day name)
		// For all days after that: "Mon Jun 8"

		Time time = new Time();
		time.setToNow();
		long currentTime = System.currentTimeMillis();
		int julianDay = Time.getJulianDay(dateInMillis, time.gmtoff);
		int currentJulianDay = Time.getJulianDay(currentTime, time.gmtoff);

		// If the date we're building the String for is today's date, the format
		// is "Today, June 24"
		if (julianDay == currentJulianDay) {
			String today = context.getString(R.string.today);
			//int formatId = R.string.format_full_friendly_date;
			return String.format("%s, %s",
					today,
					getFormattedMonthDay(context, dateInMillis));

		} else if ( julianDay < currentJulianDay + 7 ) {
			// If the input date is less than a week in the future, just return the day name.
			return getDayName(context, dateInMillis);
		} else {
			// Otherwise, use the form "Mon Jun 3"
			SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
			return shortenedDateFormat.format(dateInMillis);
		}
	}
	
	/**
	 * 
	 * @param context
	 * @param windSpeed
	 * @param degrees
	 * @return formated and friendly wind speed and direction
	 */
	  public static String getFormattedWind(Context context, float windSpeed, float degrees) {
	        String windFormat;
	        if (Utility.isMetric(context)) {
	            windFormat = "Wind: %1$1.0f km/h %2$s";
	        } else {
	            windFormat = "Wind: %1$1.0f mph %2$s";
	            windSpeed = .621371192237334f * windSpeed;
	        }

	        // From wind direction in degrees, determine compass direction as a string (e.g NW)
	        // You know what's fun, writing really long if/else statements with tons of possible
	        // conditions.  Seriously, try it!
	        String direction = "Unknown";
	        if (degrees >= 337.5 || degrees < 22.5) {
	            direction = "N";
	        } else if (degrees >= 22.5 && degrees < 67.5) {
	            direction = "NE";
	        } else if (degrees >= 67.5 && degrees < 112.5) {
	            direction = "E";
	        } else if (degrees >= 112.5 && degrees < 157.5) {
	            direction = "SE";
	        } else if (degrees >= 157.5 && degrees < 202.5) {
	            direction = "S";
	        } else if (degrees >= 202.5 && degrees < 247.5) {
	            direction = "SW";
	        } else if (degrees >= 247.5 && degrees < 292.5) {
	            direction = "W";
	        } else if (degrees >= 292.5 && degrees < 337.5) {
	            direction = "NW";
	        }
	        return String.format(windFormat, windSpeed, direction);
	    }
	  
	    /**
	     * Helper method to provide the icon resource id according to the weather condition id returned
	     * by the OpenWeatherMap call.
	     * @param weatherId from OpenWeatherMap API response
	     * @return resource id for the corresponding icon. -1 if no relation is found.
	     */
	    public static int getIconResourceForWeatherCondition(int weatherId) {
	        // Based on weather code data found at:
	        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
	        if (weatherId >= 200 && weatherId <= 232) {
	            return R.drawable.ic_storm;
	        } else if (weatherId >= 300 && weatherId <= 321) {
	            return R.drawable.ic_light_rain;
	        } else if (weatherId >= 500 && weatherId <= 504) {
	            return R.drawable.ic_rain;
	        } else if (weatherId == 511) {
	            return R.drawable.ic_snow;
	        } else if (weatherId >= 520 && weatherId <= 531) {
	            return R.drawable.ic_rain;
	        } else if (weatherId >= 600 && weatherId <= 622) {
	            return R.drawable.ic_snow;
	        } else if (weatherId >= 701 && weatherId <= 761) {
	            return R.drawable.ic_fog;
	        } else if (weatherId == 761 || weatherId == 781) {
	            return R.drawable.ic_storm;
	        } else if (weatherId == 800) {
	            return R.drawable.ic_clear;
	        } else if (weatherId == 801) {
	            return R.drawable.ic_light_clouds;
	        } else if (weatherId >= 802 && weatherId <= 804) {
	            return R.drawable.ic_cloudy;
	        }
	        return -1;
	    }
	    
	    
	    /**
	     * Helper method to provide the art resource id according to the weather condition id returned
	     * by the OpenWeatherMap call.
	     * @param weatherId from OpenWeatherMap API response
	     * @return resource id for the corresponding icon. -1 if no relation is found.
	     */
	    public static int getArtResourceForWeatherCondition(int weatherId) {
	        // Based on weather code data found at:
	        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
	        if (weatherId >= 200 && weatherId <= 232) {
	            return R.drawable.art_storm;
	        } else if (weatherId >= 300 && weatherId <= 321) {
	            return R.drawable.art_light_rain;
	        } else if (weatherId >= 500 && weatherId <= 504) {
	            return R.drawable.art_rain;
	        } else if (weatherId == 511) {
	            return R.drawable.art_snow;
	        } else if (weatherId >= 520 && weatherId <= 531) {
	            return R.drawable.art_rain;
	        } else if (weatherId >= 600 && weatherId <= 622) {
	            return R.drawable.art_snow;
	        } else if (weatherId >= 701 && weatherId <= 761) {
	            return R.drawable.art_fog;
	        } else if (weatherId == 761 || weatherId == 781) {
	            return R.drawable.art_storm;
	        } else if (weatherId == 800) {
	            return R.drawable.art_clear;
	        } else if (weatherId == 801) {
	            return R.drawable.art_light_clouds;
	        } else if (weatherId >= 802 && weatherId <= 804) {
	            return R.drawable.art_clouds;
	        }
	        return -1;
	    }
}
