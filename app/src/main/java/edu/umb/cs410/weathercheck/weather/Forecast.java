package edu.umb.cs410.weathercheck.weather;

import edu.umb.cs410.weathercheck.R;

public class Forecast {

    private Current mCurrent;
    private edu.umb.cs410.weathercheck.weather.Hour[] mHourlyForecast;


    public Current getCurrent() {
        return mCurrent;
    }

    public void setCurrent(Current current) {
        mCurrent = current;
    }

    public edu.umb.cs410.weathercheck.weather.Hour[] getHourlyForecast() {
        return mHourlyForecast;
    }

    public void setHourlyForecast(edu.umb.cs410.weathercheck.weather.Hour[] hourlyForecast) {
        mHourlyForecast = hourlyForecast;
    }



    public static int getIconId (String iconString){

        int iconId = R.drawable.clear_day;


        if (iconString.equals("clear-day")) {
            iconId = R.drawable.clear_day;
        }
        else if (iconString.equals("clear-night")) {
            iconId = R.drawable.clear_night;
        }
        else if (iconString.equals("rain")) {
            iconId = R.drawable.rain;
        }
        else if (iconString.equals("snow")) {
            iconId = R.drawable.snow;
        }
        else if (iconString.equals("sleet")) {
            iconId = R.drawable.sleet;
        }
        else if (iconString.equals("wind")) {
            iconId = R.drawable.wind;
        }
        else if (iconString.equals("fog")) {
            iconId = R.drawable.fog;
        }
        else if (iconString.equals("cloudy")) {
            iconId = R.drawable.cloudy;
        }
        else if (iconString.equals("partly-cloudy-day")) {
            iconId = R.drawable.partly_cloudy;
        }
        else if (iconString.equals("partly-cloudy-night")) {
            iconId = R.drawable.cloudy_night;
        }

        return iconId;
    }
}
