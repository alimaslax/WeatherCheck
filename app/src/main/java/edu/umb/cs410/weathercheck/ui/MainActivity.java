package edu.umb.cs410.weathercheck.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import edu.umb.cs410.weathercheck.R;
import edu.umb.cs410.weathercheck.weather.Current;
import edu.umb.cs410.weathercheck.weather.Forecast;
import edu.umb.cs410.weathercheck.weather.Hour;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends ActionBarActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String COMPARE_TEMPS = "Compare_FORECAST";
    public static final String HOURLY_FORECAST = "HOURLY_FORECAST";
    private double latitude = 42.3601;
    private double longitude = 71.0589;
    private String jsondark="";
    private String jsonopen="";
    private String jsonyahoo="";
    private HashMap<String,Integer>temps = new HashMap<String, Integer>();

    private Forecast mForecast;

    @Bind(R.id.timeLabel) TextView mTimeLabel;
    @Bind(R.id.apix) TextView mTemperatureLabel;
    @Bind(R.id.humidityValue) TextView mHumidityValue;
    @Bind(R.id.precipValue) TextView mPrecipValue;
    @Bind(R.id.summaryLabel) TextView mSummaryLabel;
    @Bind(R.id.iconImageView) ImageView mIconImageView;
    @Bind(R.id.refreshImageView) ImageView mRefreshImageView;
    @Bind(R.id.progressBar)ProgressBar mProgressBar;
    @Bind(R.id.input)EditText input;
    @Bind(R.id.locationLabel)TextView loctext;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        //random comment

        mProgressBar.setVisibility(View.INVISIBLE);

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

// Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                loctext.setText(toAddress(latitude,longitude));
                getForecast(latitude,longitude);

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };
        try {
            locationManager.requestSingleUpdate( LocationManager.NETWORK_PROVIDER,locationListener, null );
        } catch ( SecurityException e ) { e.printStackTrace(); }

        if (isNetworkAvailable()) {
            //Create a client
            OkHttpClient client = new OkHttpClient();
            //Create a request
            Request request = new Request.Builder().url("http://ip-api.com/json").build();

            //Make a call using the client and the request
            Call call = client.newCall(request);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });

                    alertUserAboutError();

                }

                @Override
                public void onResponse(Response response) throws IOException {

                    try {

                        final String jsonData = response.body().string();
                        if (response.isSuccessful()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    extract(jsonData);
                                }
                            });


                        } else {

                            alertUserAboutError();
                        }

                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }
                }
            });
        }

        else{
            Toast.makeText(this, getString(R.string.network_unavailable_message), Toast.LENGTH_LONG).show();
        }







        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loctext.setText(toAddress(latitude,longitude));
                getForecast(latitude, longitude);

            }
        });

        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                double[] loc = convertAddress(input.getText().toString());
                if(loc != null) {
                    getForecast(loc[0], loc[1]);
                    loctext.setText(toAddress(loc[0],loc[1]));
                }
                else
                    getForecast(latitude,longitude);
                return false;
            }
        });






       // getForecast(latitude,longitude);


        Log.d(TAG, "Main UI Code");

    }

    public double[] convertAddress(String strAddress){
        Geocoder coder;

        double [] loc = {0.0,0.0};
        List<Address> address;
        coder = new Geocoder(this, Locale.getDefault());
        try {
            address = coder.getFromLocationName(strAddress,5);
            if (address==null) {
                return null;
            }
            Address location=address.get(0);
            location.getLatitude();
            location.getLongitude();
            loc[0] = location.getLatitude();
            loc[1] = location.getLongitude();

            return loc;
        }catch (IOException ex) {

            ex.printStackTrace();
        }
        return null;
    }

    public String toAddress(double lat, double lng){
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(lat, lng, 1);
            if (addresses.size() > 0) {
                return addresses.get(0).getLocality()+ ", "+
                        addresses.get(0).getAdminArea();
            } else {
                // do your stuff
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();;
        }
        return null;
    }



    private void getForecast(double latitude, double longitude) {

        String darkkey = "fb49f63352f925402f107b53072f538d";
        String openkey = "2ab6b227bdd034c922b671274c8af3f6";
        String apixkey = "cd8dc10018684690806224611171812";

        String forecastUrl = "https://api.forecast.io/forecast/"+ darkkey +
        "/" + latitude +"," + longitude;

        String openUrl = "http://api.openweathermap.org/data/2.5/find?lat="+latitude +
                "&lon="+ longitude + "&cnt=10&APPID="+ openkey;

        String yahooUrl = "https://query.yahooapis.com/v1/public/yql?q=" +
                "select%20*%20from%20weather.forecast%20where%20woeid%20in%20" +
                "(SELECT%20woeid%20FROM%20geo.places%20WHERE%20text%3D%22" +
                "("+latitude+"%2C"+longitude+")%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";

        String apixUrl = "http://api.apixu.com/v1/current.json?key="+apixkey+"&q="+latitude+","+longitude;


        if (isNetworkAvailable()) {

            toggleRefresh();

            //Create a client
            OkHttpClient client = new OkHttpClient();
            //Create a request
            Request request = new Request.Builder().url(forecastUrl).build();
            Request request2 = new Request.Builder().url(openUrl).build();
            Request request3 = new Request.Builder().url(yahooUrl).build();
            Request request4 = new Request.Builder().url(apixUrl).build();
            //Make a call using the client and the request
            Call call = client.newCall(request);
            Call call2 = client.newCall(request2);
            Call call3 = client.newCall(request3);
            Call call4 = client.newCall(request4);




            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });

                    alertUserAboutError();

                }

                @Override
                public void onResponse(Response response) throws IOException {

                    try {

                        String jsonData = response.body().string();
                        jsondark = jsonData;
                        if (response.isSuccessful()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    toggleRefresh();
                                }
                            });

                            mForecast = parseForecastDetails(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateWeather();
                                }
                            });

                        } else {
                            alertUserAboutError();
                        }

                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }

                    catch (JSONException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }

                }
            });
            call2.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });

                    alertUserAboutError();

                }

                @Override
                public void onResponse(Response response) throws IOException {

                    try {

                        String jsonData = response.body().string();
                        jsonopen = jsonData;
                          if (response.isSuccessful()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    OpenTemp(jsonopen);
                                }
                            });


                        } else {

                            alertUserAboutError();
                        }

                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }
                }
            });
            call3.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });

                    alertUserAboutError();

                }

                @Override
                public void onResponse(Response response) throws IOException {

                    try {

                        String jsonData = response.body().string();
                        jsonyahoo = jsonData;
                        if (response.isSuccessful()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    YahooTemp(jsonyahoo);
                                }
                            });


                        } else {

                            alertUserAboutError();
                        }

                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }
                }
            });
            call4.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });

                    alertUserAboutError();

                }

                @Override
                public void onResponse(Response response) throws IOException {

                    try {

                        String jsonData = response.body().string();
                        jsonyahoo = jsonData;
                        if (response.isSuccessful()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ApixTemp(jsonyahoo);

                                }
                            });

                        } else {

                            alertUserAboutError();
                        }

                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }
                }
            });
        }

        else{
            Toast.makeText(this, getString(R.string.network_unavailable_message), Toast.LENGTH_LONG).show();
        }
    }

    private void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.INVISIBLE) {

            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        }
        else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);

        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void updateWeather() {
        Current current = mForecast.getCurrent();
        int averageTemp = 0;
        try{
            averageTemp=(int)((current.getTemperature()+temps.get("open")+
            temps.get("yahoo")+temps.get("apix"))/4);
        }
        catch(Exception e)
        {

        }
        mHumidityValue.setText(current.getHumidity() +"");
        mTimeLabel.setText("Aggregated Temperature");
        mPrecipValue.setText(current.getPrecipChance() +"%");
        mTemperatureLabel.setText(current.getTemperature() +"");
        mSummaryLabel.setText(current.getSummary());

        temps.put("dark",current.getTemperature());

        Drawable drawable = getResources().getDrawable(current.getIconId(), null);
        mIconImageView.setImageDrawable(drawable);


    }
    private void OpenTemp(String jsonData){
            String t = jsonData.substring(jsonData.indexOf("temp"),jsonData.indexOf("pressure"));
            t = t.replaceAll("[^\\d.]", "");
            Double k = Double.parseDouble(t);
            int i = (int)((9/5)*(k-273))+42;
            temps.put("open",i);
    }
    private void YahooTemp(String jsonData){
        String t = jsonData.substring(jsonData.indexOf("\"temp\":"),jsonData.indexOf("\"text\":"));
        t = t.replaceAll("[^\\d.]", "");
        Double k = Double.parseDouble(t);
        int i = (int)(k+0);
        temps.put("yahoo",i);
    }
    private void ApixTemp(String jsonData){
        String t = jsonData.substring(jsonData.indexOf("\"temp_f\""),jsonData.indexOf("\"is_day\""));
        t = t.replaceAll("[^\\d.]", "");
        Double k = Double.parseDouble(t);
        int i = (int)(k+0);
        temps.put("apix",i);
    }

    private Forecast parseForecastDetails (String jsonData) throws JSONException{
        Forecast forecast = new Forecast();

        forecast.setCurrent(getCurrentDetails(jsonData));
        forecast.setHourlyForecast(getHourlyForecast(jsonData));


        return forecast;
    }

    private Hour[] getHourlyForecast(String jsonData) throws JSONException {

        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject hourly = forecast.getJSONObject("hourly");
        JSONArray data = hourly.getJSONArray("data");

        Hour[] hours = new Hour[data.length()];

        for (int i=0; i < data.length(); i++ ){

            JSONObject jsonHour = data.getJSONObject(i);
            Hour hour = new Hour();
            hour.setTime(jsonHour.getLong("time"));
            hour.setTemperature(jsonHour.getDouble("temperature"));
            hour.setIcon(jsonHour.getString("icon"));
            hour.setSummary(jsonHour.getString("summary"));
            hour.setTimezone(timezone);

            hours[i] = hour;

        }

        return hours;


    }

    private Current getCurrentDetails(String jsonData) throws JSONException{
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        Log.i(TAG, "From JSON: " + timezone);

        JSONObject currently = forecast.getJSONObject("currently");

        Current current = new Current();

        current.setHumidity(currently.getDouble("humidity"));
        current.setIcon(currently.getString("icon"));
        current.setPrecipChance(currently.getDouble("precipProbability"));
        current.setTemperature(currently.getDouble("temperature"));
        current.setTime(currently.getLong("time"));
        current.setSummary(currently.getString("summary"));
        current.setTimezone(timezone);

        Log.d(TAG, current.getFormattedTime());

        return current;

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }

        return isAvailable;
    }

    private void alertUserAboutError() {


    }

    @OnClick(R.id.dailyButton)
    public void startDailyActivity(View view){
        Intent intent = new Intent(this, CompareActivity.class);
        intent.putExtra(COMPARE_TEMPS, temps);
        startActivity(intent);
    }

    @OnClick(R.id.hourlyButton)
    public void startHourlyActivity(View view){
        Intent intent = new Intent(this, HourlyForecastActivity.class);
        intent.putExtra(HOURLY_FORECAST, mForecast.getHourlyForecast());
        startActivity(intent);

    }

    void extract(String json){
        String t = json.substring(json.indexOf("\"zip\":"),json.indexOf("\"}"));
        t = t.replaceAll("[^\\d.]", "");
        double[] loc = convertAddress(t);
        if(loc != null) {
            getForecast(loc[0], loc[1]);
            loctext.setText(toAddress(loc[0],loc[1]));
        }
        else
            getForecast(latitude,longitude);
    }




}
