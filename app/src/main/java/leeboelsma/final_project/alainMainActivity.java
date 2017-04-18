package leeboelsma.final_project;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class alainMainActivity extends Activity {
    public static final String ALAIN_PREFERENCES = "alainValues";
    public static final String ALAIN_PREFERENCES_BUSNUM = "Number";
    public static final String ALAIN_PREFERENCES_BUSDIR = "Direction";

    private static final String DB_NAME = "routes.db";
    private EditText routeNumber;
    String daBusNumber;
    private TextView busStop;
    private String direction;
    SQLiteDatabase db;
    ArrayList<String> listOfStops=null;
    ArrayList<String> listOfStopsID = null;
    int stopIndex=0;
    SharedPreferences settings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = this.openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
        setContentView(R.layout.activity_alain_main);
        ProgressBar bar = (ProgressBar) alainMainActivity.this.findViewById(R.id.progressBar);
        bar.setVisibility(View.VISIBLE);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Install database from file if it does not exist:
        if (isTableExists("stops") && isTableExists("stop_times"))
            System.out.println("Yes, this is OK");
        else {
            alainDatabaseLoader helper = new alainDatabaseLoader(this);
            helper.insertStaticData();
            helper.insertStaticData2();
            alainDatabaseLoader.LoadLargeTables lt = helper.new LoadLargeTables();
            lt.execute();
        }

        // Recover after state change:
        settings =
                getSharedPreferences(ALAIN_PREFERENCES, Context.MODE_PRIVATE);

        // load route chooser:
        routeNumber = (EditText) findViewById(R.id.alainBusNumber);

        if (settings.contains(ALAIN_PREFERENCES_BUSNUM)) {
            daBusNumber = settings.getString(ALAIN_PREFERENCES_BUSNUM, "");
            routeNumber.setText(daBusNumber);
            RouteImageQuery wGet = new RouteImageQuery();
            wGet.execute();
            if(settings.contains(ALAIN_PREFERENCES_BUSDIR)){
                int k = Integer.parseInt(settings.getString(ALAIN_PREFERENCES_BUSDIR, ""));
                setSpinnerGenre(k);
            } else{
                setSpinnerGenre(0);
            }
        }


        routeNumber.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if (!routeNumber.getText().toString().equals("")) {
                        stopIndex=0;
                        daBusNumber = routeNumber.getText().toString();
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(ALAIN_PREFERENCES_BUSNUM, daBusNumber);
                        editor.commit();
                        RouteImageQuery wGet = new RouteImageQuery();
                        wGet.execute();
                        setSpinnerGenre(0);
                    }
                    return true;
                }
                return false;
            }
        });


        Button getTimeTable = (Button) findViewById(R.id.alainChooseStopButton);
        getTimeTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!routeNumber.getText().equals("")) {
                    if (!busStop.getText().equals("")) {
                        getTimetableData l = new getTimetableData();
                        l.execute();
                    }
                }
            }
        });


        busStop = (TextView) findViewById(R.id.alainBusStopName);
        busStop.setOnTouchListener(new alainOnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                if (listOfStops != null) {
                    if (stopIndex > 0) {
                        stopIndex--;
                        busStop.setText(listOfStops.get(stopIndex));
                    }
                }
            }

            @Override
            public void onSwipeRight() {
                if (listOfStops != null) {
                    if (stopIndex < listOfStops.size() - 1) {
                        stopIndex++;
                        busStop.setText(listOfStops.get(stopIndex));
                    }
                }
            }
        });
    }


    protected void setStopList(String direction){
        listOfStops = new ArrayList<String>();
        listOfStopsID = new ArrayList<String>();
        String num = routeNumber.getText().toString();
        Cursor res = db.rawQuery("SELECT stop_sequence, stop_name, stops.stop_id FROM stop_times, stops " +
                "WHERE stop_times.trip_id = " +
                "(SELECT trip_id FROM trips WHERE trips.trip_headsign ='" +direction +"' " +
                "AND trips.route_id = (select route_id FROM routes where routes.route_short_name = '"
                + num + "' ) " +
                "limit 1) AND stop_times.stop_id = stops.stop_id", null);
        int j=0;
        while(res.moveToNext()){
            listOfStops.add(res.getString(1));
            listOfStopsID.add(res.getString(2));
        }
        busStop.setText(listOfStops.get(stopIndex));
    }

    class getTimetableData extends AsyncTask<String, Integer, String> {
        Cursor res=null;

        @Override
        protected void onPreExecute() {}

        @Override
        protected String doInBackground(String... params) {
            String stopid = listOfStopsID.get(stopIndex);
            String routen= daBusNumber+"-256";
            res = db.rawQuery("SELECT trip_id, departure_time FROM stop_times WHERE " +
                    " stop_times.stop_id ='"+ stopid + "' AND trip_id IN (SELECT trip_id FROM trips WHERE route_id='"+routen+"' " +
                    "AND trip_headsign='" +direction +"' )ORDER BY departure_time", null);
            return "data retrieved";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
        }

        @Override
        protected void onPostExecute(String result) {
            displayTimesForBus(res);
        }
    }

    protected void displayTimesForBus(Cursor times) {

        // Create an alias for our Activity to use in inner classes.
        final Activity activity = this;

        // Builder: create the builder and setup the title of the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        // inflate our about box layout
        LayoutInflater li = LayoutInflater.from(activity);
        View view = li.inflate(R.layout.alain_stoptime_results, null);
        TextView tv1 = (TextView) view.findViewById(R.id.alain_currentTime1);

        //DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
        DateFormat df = new SimpleDateFormat("h:mm a");
        String time = df.format(Calendar.getInstance().getTime());
        tv1.setText(time);
        TextView tv2 = (TextView) view.findViewById(R.id.alainTimeTablebusStopName1);
        tv2.setText(listOfStops.get(stopIndex));
        TextView tv3 = (TextView) view.findViewById(R.id.alainTimeTablebusStopNumber1);
        tv3.setText(routeNumber.getText());
        TextView tv4 = (TextView) view.findViewById(R.id.alainTimeTablebusStopDirection1);
        tv4.setText(direction);
        StringBuilder sb = new StringBuilder();
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        String allTimes="";
        while(times.moveToNext()){
            String [] dt = times.getString(0).split("-");
            for(int j=1; j<dt.length; j++){
                sb.append(dt[j]);
            }
            String test_dt=sb.toString();
            sb.delete(0,sb.length());
            switch (day) {
                case Calendar.SUNDAY:
                    if(test_dt.equals("JAN17BJANSU17Sunday02"))
                        allTimes+=times.getString(1) + " , ";
                    break;
                case Calendar.SATURDAY:
                    if(test_dt.equals("JAN17BJANSA17Saturday03"))
                        allTimes+=times.getString(1) + " , ";
                    break;
                case Calendar.MONDAY:
                case Calendar.TUESDAY:
                case Calendar.WEDNESDAY:
                case Calendar.THURSDAY:
                case Calendar.FRIDAY:
                    if(test_dt.equals("JAN17BJANDA17Weekday17"))
                        allTimes+=times.getString(1) + " , ";
                    break;
                default:
                    System.exit(0);
            }
        }
        if(allTimes.length()-2>=0) {
            TextView tv5 = (TextView) view.findViewById(R.id.alainBusTimes);
            tv5.setText(allTimes.substring(0, allTimes.length() - 2));
        }
        // set it as the main view
        builder.setView(view);
        // let the builder know that we want a ok button
        builder.setPositiveButton(android.R.string.ok, null); // a null listener defaults to dismissing the dialog.
        builder.show();
    }


    class RouteImageQuery extends AsyncTask<String, Integer, String> {
        //String stopID;
        Bitmap busRouteImg;
        String mapURL;

        @Override
        protected void onPreExecute() {
            String busNumber=routeNumber.getText().toString();
            int n = Integer.parseInt(busNumber);
            if(n<10) busNumber="00" + busNumber;
            else if(n>9 && n <= 99) busNumber="0" + busNumber;
            mapURL = "http://www.octranspo.com/images/files/routes/" + busNumber + "map.gif";
            ProgressBar bar = (ProgressBar) alainMainActivity.this.findViewById(R.id.progressBar);
            bar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection=null;
            try {

                URL url = new URL(mapURL);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    busRouteImg = BitmapFactory.decodeStream(connection.getInputStream());
                } else
                    busRouteImg = null;
            } catch (Exception e) {
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            this.publishProgress(100);
            return "Bus Route Image";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            ProgressBar bar = (ProgressBar) alainMainActivity.this.findViewById(R.id.progressBar);
            bar.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            // I'm running on the main UI thread
            ImageView imv = (ImageView) alainMainActivity.this.findViewById(R.id.alainRouteImage);
            imv.setImageBitmap(busRouteImg);
            ProgressBar bar = (ProgressBar) alainMainActivity.this.findViewById(R.id.progressBar);
            bar.setVisibility(View.INVISIBLE);
            Toast.makeText(alainMainActivity.this, result, Toast.LENGTH_SHORT).show();
        }

        protected void updateImage(int stop){
            // will need to modify image here...
        }

    }


    public void setSpinnerGenre(int iVal) {
        final Spinner spinner = (Spinner) findViewById(R.id.alainBusDirection);


        String num = routeNumber.getText().toString();
        Cursor res = db.rawQuery("SELECT DISTINCT trips.trip_headsign FROM trips, routes" +
                " WHERE routes.route_id=trips.route_id " +
                "AND routes.route_short_name = '" + num + "'", null);

        List<String> list = new ArrayList<String>();
        int j = 0;
        while (res.moveToNext()) {
            list.add(res.getString(0));
        }
        // Creating adapter for spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(iVal);
        spinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View itemSelected,
                                               int selectedItemPosition, long selectedId) {
                        stopIndex=0;
                        setStopList(parent.getItemAtPosition(selectedItemPosition).toString());
                        direction = parent.getItemAtPosition(selectedItemPosition).toString();
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(ALAIN_PREFERENCES_BUSDIR, Integer.toString(selectedItemPosition));
                        editor.commit();
                    }

                    // … Other required overrides
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Do Nothing...
                    }
                });

    }

    public boolean isTableExists(String tableName) {

        Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
        if(cursor!=null) {
            if(cursor.getCount()>0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

}