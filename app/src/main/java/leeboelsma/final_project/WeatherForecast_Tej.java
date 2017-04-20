package leeboelsma.final_project;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherForecast_Tej extends Activity {

    private Activity self;
    EditText city;
    String  City;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tej);
        self = this;
        //  InputMethodManager imm =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
       //   imm.hideSoftInputFromWindow(city.getWindowToken(),InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // inflate the description of your menu items in the menu
        getMenuInflater().inflate(R.menu.menu_all, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Create an alias for our Activity to use in inner classes.
        final Activity activity = this;

        switch (item.getItemId()) {
            case R.id.activity1: {
                Intent intent = new Intent(this, Weather.class);
                startActivity(intent);
                return true;
            }
            case R.id.activity2: {
                // Tej's Activity
                Intent intent = new Intent(this, WeatherForecast_Tej.class);
                startActivity(intent);
                return true;
            }
            case R.id.activity3: {
                // Alain's Activity
                Intent intent = new Intent(this, alainMainActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.activity4: {
                // Ed's Activity
                Intent intent = new Intent(this, MainEdActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.settings: {
                Intent intent = new Intent(this, Settings.class);
                startActivityForResult(intent, 0);
                return true;
            }
            case R.id.about: {
                return true;
            }
            default:
                return super.onOptionsItemSelected(item); //not sure what this does.
        }
    }

    public void onGo(View view) {
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
        city=(EditText)findViewById(R.id.etcity);

        City=city.getText().toString();
       if (City.length()==0)
        {City="Ottawa";}
        new ForecastQuery().execute();
    }

    private class ForecastQuery extends AsyncTask<String, Integer, String> {
        String current, min, max;
        Bitmap image;

        @Override
        protected String doInBackground(String ...args) {
            String icon_id = null;

            try {
         //      Toast.makeText(getApplicationContext(),"Welcome on Weather site!",Toast.LENGTH_SHORT).show();
                // get data from web server
               // URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q=toronto,ca&APPID=d99666875e0e51521f0040a3d97d0f6a&mode=xml&units=metric");

                URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q="+City+"&APPID=d99666875e0e51521f0040a3d97d0f6a&mode=xml&units=metric");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                InputStream in = http.getInputStream();

                // parse the xml
                XmlPullParser parser = Xml.newPullParser();
                //parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(in, null);

                // looking for: <temperature value="7.98" min="7" max="9" unit="metric"/>
                // also:        <weather number="803" value="broken clouds" icon="04d"/>
                boolean done = false;
                while(!done) {
                    switch(parser.getEventType()) {
                        case XmlPullParser.START_TAG: {
                            String name = parser.getName();
                            switch(name) {
                                case "temperature":
                                    // Note: you don't need a loop after all, just use a null namespace
                                    this.current = parser.getAttributeValue(null, "value");
                                    this.min = parser.getAttributeValue(null, "min");
                                    this.max = parser.getAttributeValue(null, "max");
                                    break;
                                case "weather":
                                    icon_id = parser.getAttributeValue(null, "icon");
                                    break;
                            }
                            break;
                        }
                        case XmlPullParser.END_DOCUMENT: {
                            done = true;
                            break;
                        }
                    }
                    parser.next();
                }
            } catch(Exception e) {
                throw new RuntimeException(e);
            }

            // update progress (this is kinda dumb, since parsing is lightning fast. It would only be non 75 if there was a problem)
            int progress = 0;
            if(this.current != null) progress += 25;
            if(this.min != null) progress += 25;
            if(this.max != null) progress += 25;
            if(progress > 0) publishProgress(progress);

            // download the bitmap image (e.g. http://openweathermap.org/img/w/04d.png)
            String icon_path = "http://openweathermap.org/img/w/" + icon_id + ".png";
            try {
                // get data from web server
                URL url = new URL(icon_path);
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.connect();
                int responseCode = http.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream in = http.getInputStream();
                    this.image = BitmapFactory.decodeStream(in);
                }
            } catch(Exception e) {
                throw new RuntimeException(e);
            }

            // update progress
            if(this.image != null) {
                progress += 25;
                publishProgress(progress);
            }

            return "Job Done";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            ((ProgressBar)self.findViewById(R.id.progress)).setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            ((TextView)self.findViewById(R.id.current)).setText(this.current);
            ((TextView)self.findViewById(R.id.min)).setText(this.min);
            ((TextView)self.findViewById(R.id.max)).setText(this.max);
            ((ImageView)self.findViewById(R.id.image)).setImageBitmap(this.image);
            findViewById(R.id.progress).setVisibility(View.INVISIBLE);
        }
    }
}
