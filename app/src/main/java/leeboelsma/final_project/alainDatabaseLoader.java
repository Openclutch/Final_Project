package leeboelsma.final_project;


import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Al's Machine on 2017-04-12.
 */

public class alainDatabaseLoader extends SQLiteOpenHelper{
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 7006;
    public static final String DATABASE_NAME = "routes.db";

    public static final String [] route_stop_map = {}; // TO DO -> Add columns for table of stops steps along route image
    AssetManager am ;
    Context context;

    public alainDatabaseLoader(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        am=context.getAssets();
        this.context=context;
    }

    public void insertStaticData() {
        SQLiteDatabase db = this.getWritableDatabase();
        int i = 1;
        String sql;
        BufferedReader reader = null;
        InputStream is;
        try {
            is = am.open("calendar.txt");
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            // insert lines into database:


            String line;
            String[] values;
            db.beginTransaction();
            sql = "INSERT INTO calendar (service_id, monday, tuesday, wednesday, " +
                    "thursday, friday, saturday, sunday, start_date, end_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            SQLiteStatement statement = db.compileStatement(sql);
            while ((line = reader.readLine()) != null) {
                values = line.split(",");
                statement.clearBindings();
                for (int j = 0; j < values.length; j++) {
                    if (values[j].equals("")) values[j] = "NULL";
                    statement.bindString(j + 1, values[j]);
                    statement.executeInsert();
                }
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (IOException e) {
            System.err.println("Couldn\' t read file..." + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }
    }


    public void insertStaticData2() {
        SQLiteDatabase db=this.getWritableDatabase();
        int i = 1;
        String sql;
        BufferedReader reader = null;
        InputStream is;
        try {
            String line;
            String[] values;

            is = am.open("routes.txt");
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            // insert lines into database:
            db.beginTransaction();
            sql = "INSERT INTO routes (route_id, route_short_name, route_long_name, route_desc, route_type, route_url) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            SQLiteStatement statement2 = db.compileStatement(sql);
            statement2 = db.compileStatement(sql);
            while ((line = reader.readLine()) != null) {
                values = line.split(",");
                statement2.clearBindings();
                for (int j = 0; j < values.length; j++) {
                    if (values[j].equals("")) values[j] = "NULL";
                    statement2.bindString(j + 1, values[j]);
                    statement2.executeInsert();
                }
            }
            db.setTransactionSuccessful();
            db.endTransaction();

        } catch (IOException e) {
            System.err.println("Couldn\' t read file..." + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }
    }



    private void insertStaticData3() {
        SQLiteDatabase db=this.getWritableDatabase();
        int i = 1;
        String sql;
        BufferedReader reader = null;
        InputStream is;
        db.beginTransaction();
        try {
            String line;
            String[] values;

            is = am.open("stop_times.txt");
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            // insert lines into database:

            sql = "INSERT INTO stop_times (trip_id, arrival_time, departure_time, stop_id, stop_sequence, pickup_type, drop_off_type) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            SQLiteStatement statement2 = db.compileStatement(sql);
            statement2 = db.compileStatement(sql);
            while ((line = reader.readLine()) != null) {
                values = line.split(",");
                ContentValues ivalues = new ContentValues();
                ivalues.put("trip_id", values[0]);
                ivalues.put("arrival_time",values[1]);
                ivalues.put("departure_time",values[2]);
                ivalues.put("stop_id",values[3]);
                ivalues.put("stop_sequence",values[4]);
                ivalues.put("pickup_type",values[5]);
                ivalues.put("drop_off_type",values[6]);
                db.insert("stop_times", null, ivalues);
                //db.insertWithOnConflict("stop_times", null, ivalues, SQLiteDatabase.CONFLICT_IGNORE);

            }
            db.setTransactionSuccessful();

        } catch (IOException e) {
            System.err.println("Couldn\' t read file..." + e.getMessage());
        } finally {
            db.endTransaction();
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }
    }

    private void insertStaticData4() {
        SQLiteDatabase db=this.getWritableDatabase();
        int i = 1;
        String sql;
        BufferedReader reader = null;
        InputStream is;
        db.beginTransaction();
        try {
            String line;
            String[] values;

            is = am.open("stops.txt");
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            // insert lines into database:

            sql = "INSERT INTO stops (stop_id,stop_code,stop_name,stop_desc,stop_lat,stop_lon," +
                            "zone_id,stop_url,location_type) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            SQLiteStatement statement2 = db.compileStatement(sql);
            statement2 = db.compileStatement(sql);
            while ((line = reader.readLine()) != null) {
                values = line.split(",");
                ContentValues ivalues = new ContentValues();
                ivalues.put("stop_id", values[0]);
                ivalues.put("stop_code",values[1]);
                ivalues.put("stop_name",values[2]);
                ivalues.put("stop_desc",values[3]);
                ivalues.put("stop_lat",values[4]);
                ivalues.put("stop_lon",values[5]);
                ivalues.put("zone_id",values[6]);
                ivalues.put("stop_url",values[7]);
                ivalues.put("location_type",values[8]);
                db.insert("stops", null, ivalues);
                //db.insertWithOnConflict("stop_times", null, ivalues, SQLiteDatabase.CONFLICT_IGNORE);

            }
            db.setTransactionSuccessful();

        } catch (IOException e) {
            System.err.println("Couldn\' t read file..." + e.getMessage());
        } finally {
            db.endTransaction();
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }
    }


    private void insertStaticData5() {
        SQLiteDatabase db=this.getWritableDatabase();
        int i = 1;
        String sql;
        BufferedReader reader = null;
        InputStream is;
        db.beginTransaction();
        try {
            String line;
            String[] values;

            is = am.open("trips.txt");
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            // insert lines into database:

            sql = "INSERT INTO trips (route_id, service_id, trip_id, trip_headsign, direction_id, block_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            SQLiteStatement statement2 = db.compileStatement(sql);
            statement2 = db.compileStatement(sql);
            while ((line = reader.readLine()) != null) {
                values = line.split(",");
                ContentValues ivalues = new ContentValues();
                ivalues.put("route_id", values[0]);
                ivalues.put("service_id",values[1]);
                ivalues.put("trip_id",values[2]);
                ivalues.put("trip_headsign",values[3]);
                ivalues.put("direction_id",values[4]);
                ivalues.put("block_id",values[5]);
                db.insert("trips", null, ivalues);
            }
            db.setTransactionSuccessful();

        } catch (IOException e) {
            System.err.println("Couldn\' t read file..." + e.getMessage());
        } finally {
            db.endTransaction();
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS calendar");
        db.execSQL( "create table calendar (service_id TEXT, " +
                                "monday TEXT," +
                                "tuesday TEXT," +
                                "wednesday TEXT," +
                                "thursday TEXT," +
                                "friday TEXT," +
                                "saturday TEXT," +
                                "sunday TEXT," +
                                "start_date TEXT," +
                                "end_date TEXT);");


        db.execSQL("DROP TABLE IF EXISTS routes");
        db.execSQL("create table routes (route_id TEXT," +
                                "route_short_name TEXT," +
                                "route_long_name TEXT," +
                                "route_desc TEXT," +
                                "route_type TEXT," +
                                "route_url TEXT)");

        db.execSQL("DROP TABLE IF EXISTS stop_times");
        db.execSQL("create table stop_times (trip_id TEXT," +
                        "arrival_time TEXT," +
                        "departure_time TEXT," +
                        "stop_id TEXT," +
                        "stop_sequence TEXT," +
                        "pickup_type TEXT," +
                        "drop_off_type TEXT)");

        db.execSQL("DROP TABLE IF EXISTS stops");
        db.execSQL("create table stops (stop_id TEXT," +
                        "stop_code TEXT," +
                        "stop_name TEXT," +
                        "stop_desc TEXT," +
                        "stop_lat TEXT," +
                        "stop_lon TEXT," +
                        "zone_id TEXT," +
                        "stop_url TEXT," +
                        "location_type TEXT)");


        db.execSQL("DROP TABLE IF EXISTS trips");
        db.execSQL("create table trips (route_id TEXT," +
                        "service_id TEXT," +
                        "trip_id TEXT," +
                        "trip_headsign TEXT," +
                        "direction_id TEXT," +
                        "block_id TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS  calendar");
        db.execSQL("DROP TABLE IF EXISTS  routes");
        db.execSQL("DROP TABLE IF EXISTS  stop_times");
        db.execSQL("DROP TABLE IF EXISTS  stops");
        db.execSQL("DROP TABLE IF EXISTS  trips");
        onCreate(db);
    }

    class LoadLargeTables extends AsyncTask<String, Integer, String> {


        @Override
        protected void onPreExecute() {
            Toast.makeText(context, "This is going to take a while...", Toast.LENGTH_LONG).show();

        }

        @Override
        protected String doInBackground(String... params) {
            insertStaticData3();
            this.publishProgress(1);
            insertStaticData4();
            this.publishProgress(2);
            insertStaticData5();
            return "Loaded large data tables...";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            if((int) (progress[0]) == 1)
                Toast.makeText(context, "Loaded stop_times...", Toast.LENGTH_SHORT).show();
            if((int) (progress[0]) == 2)
                Toast.makeText(context, "Loaded stops...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(String result) {
            // I'm running on the main UI thread
            Toast.makeText(context, "Completed loading large data tables", Toast.LENGTH_SHORT).show();
        }

    }




}


