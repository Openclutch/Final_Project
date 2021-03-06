package leeboelsma.final_project;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

public class Weather extends Activity {

    private String TAG = Weather.class.getSimpleName();

    // Sharing button things...
    private ShareActionProvider mShareActionProvider;

    // URL to get Weather in JSON
    private static String url = "https://api.darksky.net/forecast/" +
            "969149eeb2f7dba899c3395808a73c25/" + //Api key
            "45.350226,-75.755861" + //Lat Long coordinates
            "?units=si"; // Set units to Celsius

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate the description of your menu items in the menu
        getMenuInflater().inflate(R.menu.menu, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();

        return true;
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        new GetWeather().execute(); //execute our inner class
    }

    private class GetWeather extends AsyncTask<Object, Object, Object> {

        private Date lastupdated;
        private String summary;
        private double precipProbability;
        private double temp;
        private double humidity;
        private String icon;

        @Override
        protected Void doInBackground(Object... arg0) {
            HttpHandler sh = new HttpHandler();

            // Make request to url and get response
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Object node
                    JSONObject weather_currently = jsonObj.getJSONObject("currently");

                    long unix_time = weather_currently.getLong("time");
                    this.lastupdated = new java.util.Date((long)unix_time*1000); //convert from unix
                    publishProgress(20);

                    this.icon = weather_currently.getString("icon");
                    publishProgress(40);

                    this.precipProbability = weather_currently.getDouble("precipProbability") * 100;
                    publishProgress(60);

                    this.temp = weather_currently.getDouble("apparentTemperature");
                    publishProgress(70);

                    this.humidity = weather_currently.getDouble("humidity") * 100;
                    publishProgress(80);

                    JSONObject weather_summary = jsonObj.getJSONObject("hourly");

                    this.summary = weather_summary.getString("summary");
                    publishProgress(100);


                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                "Json parsing error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                            "Couldn't get json from server. Check LogCat for possible errors!",
                            Toast.LENGTH_LONG).show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Object... value){

            //set the visibility of the progress bar to visible
            ProgressBar fetchProgress = (ProgressBar) (findViewById(R.id.progressBar));
            fetchProgress.setVisibility(View.VISIBLE);

            //set the progressBar progress from the "value" being passed to this
            int v = (Integer) value[0];
            fetchProgress.setProgress(v);

        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);

            //set visibility of the progress bar to invisible
            ProgressBar fetchProgress = (ProgressBar) (findViewById(R.id.progressBar));
            fetchProgress.setVisibility(View.INVISIBLE);

            // Get all activity views
            TextView text_lastupdated = (TextView) findViewById(R.id.weather_lastupdated);
            TextView text_summary = (TextView) findViewById(R.id.weather_summary);
            TextView text_precipProbability = (TextView) findViewById(R.id.weather_precipProbability);
            TextView text_temp = (TextView) findViewById(R.id.weather_temp);
            TextView text_humidity = (TextView) findViewById(R.id.weather_humidity);
            ImageView pic_icon = (ImageView) findViewById(R.id.weather_icon);

            // Set all activity views
            text_lastupdated.append(String.valueOf(this.lastupdated));
            text_summary.append(this.summary);
            text_precipProbability.append(String.valueOf((int) this.precipProbability) + "%");
            text_temp.append(String.valueOf(this.temp) + "C");
            text_humidity.append(String.valueOf((int) this.humidity) + "%");

            // Create hashmap to hold icons and images
            HashMap<String, Integer> icons = new HashMap<>();

            // Fill hashmap
            icons.put("clear-day", R.drawable.clear_day);
            icons.put("clear-night", R.drawable.clear_night);
            icons.put("rain", R.drawable.rain);
            icons.put("snow", R.drawable.snow);
            icons.put("sleet", R.drawable.snow);
            icons.put("wind", R.drawable.wind);
            icons.put("fog", R.drawable.fog);
            icons.put("cloudy", R.drawable.cloudy);
            icons.put("partly-cloudy-day", R.drawable.partly_cloudy_day);
            icons.put("partly-cloudy-night", R.drawable.partly_cloudy_night);

            pic_icon.setImageResource(icons.get(this.icon));

            // Set your sharing settings based on what the weather is
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Current Weather");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "It currently feels like " +
                    temp + "C outside. It will be " + summary);

            setShareIntent(sharingIntent);

        }
    }
}
