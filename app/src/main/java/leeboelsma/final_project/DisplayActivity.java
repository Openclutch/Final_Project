/*
* File name: DisplayActivity.java
* Author: Edward Ding #040078518
* Course: CST2335
* Assignment: Final Project
* Date: April 27, 2017
* Professor: David Lareau
* Purpose: Secondary activity file for photo app
* Class list: DisplayActivity, ImageAdapter, MyHelper
*/
/**
 * class DisplayActivity
 * @author Eding
 * @version 1
 */
package leeboelsma.final_project;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.FileInputStream;
import java.io.IOException;

public class DisplayActivity extends Activity {

    private SharedPreferences sharedPref;
    private MyHelper myHelper;
    private SQLiteDatabase database;
    private TextView textView;
    private ImageView imageView;
    private EditText editText;
    private Button b1, b2, b3, b4;
    private byte[] bites;
    private Bitmap bm;
    private String comment = "";
    private String position = "";
    private String timestamp = "";
    private Boolean enableDebug = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        setContentView(R.layout.activity_display);
        sharedPref = getSharedPreferences("Prefs", Context.MODE_PRIVATE);
        enableDebug = sharedPref.getBoolean("ToastKey", false);

        Bundle bundle = getIntent().getExtras();
        position = bundle.getString("ButtonIndex");
        timestamp = bundle.getString("TimeStamp");

        myHelper = new MyHelper(this, "CAMERA.db");
        database = myHelper.getWritableDatabase();

        imageView = (ImageView) findViewById(R.id.ed_imageView);
//        imageView.setMinimumWidth(800);
//        imageView.setMinimumHeight(480);

        editText = (EditText) findViewById(R.id.ed_editText);

        textView = (TextView) findViewById(R.id.ed_textView);
        textView.setTextSize(20);

        b1 = (Button) findViewById(R.id.ed_buttonSave);
        b1.setTextSize(10);
        b1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent result = new Intent();

                comment = editText.getText().toString();
                String positionAdd = Long.toString(DatabaseUtils.queryNumEntries(database, "CAMERA"));
                writeDB(positionAdd, comment, timestamp, database, bites);
                deleteFile("tempfile");
                result.putExtra("Command", "SAVE");

                database.close();
                setResult(Activity.RESULT_OK, result);
                finish();
            }
        });

        b2 = (Button) findViewById(R.id.ed_buttonUpdate);
        b2.setTextSize(10);
        b2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent result = new Intent();

                comment = editText.getText().toString();
                updateDB(position, comment);
                result.putExtra("Command", "NOTHING");

                database.close();
                setResult(Activity.RESULT_OK, result);
                finish();
            }
        });

        b3 = (Button) findViewById(R.id.ed_buttonCancel);
        b3.setTextSize(10);
        b3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent result = new Intent();

                result.putExtra("Command", "NOTHING");

                database.close();
                setResult(Activity.RESULT_OK, result);
                finish();
            }
        });

        b4 = (Button) findViewById(R.id.ed_buttonDelete);
        b4.setTextSize(10);
        b4.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent result = new Intent();

                if (position.equalsIgnoreCase("NEW")) {
                    deleteFile("tempfile");
                    result.putExtra("Command", "CLEAR");
                } else {
                    removeDBimage(database, Integer.parseInt(position));
                    result.putExtra("Command", position);
                }

                database.close();
                setResult(Activity.RESULT_OK, result);
                finish();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (position.equalsIgnoreCase("NEW")) {
            bites = readFromFile("tempfile");
            bm = getImage(bites);
            imageView.setImageBitmap(bm);
            editText.setText("");
            b1.setVisibility(View.VISIBLE);
            b2.setVisibility(View.INVISIBLE);
            b3.setVisibility(View.INVISIBLE);
            b4.setVisibility(View.VISIBLE);
        } else {

            bites = readDBimage(database, Integer.parseInt(position));

            bm = getImage(bites);
            imageView.setImageBitmap(bm);
            String comment = readDBmessage(database, Integer.parseInt(position));
            editText.setText(comment);
            b1.setVisibility(View.INVISIBLE);
            b2.setVisibility(View.VISIBLE);
            b3.setVisibility(View.VISIBLE);
            b4.setVisibility(View.VISIBLE);

            timestamp = readDBtime(database, Integer.parseInt(position));
        }

        setToast(enableDebug, "Button Index to Edit: " + position);
        textView.setText(timestamp);
    }

    // convert from byte array to bitmap
    private Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    private byte[] readFromFile(String filename) {
        byte[] buffer = new byte[4096];
        try {
            FileInputStream input = openFileInput(filename);
            input.read(buffer);
        } catch (IOException e) {
            Log.e("Exception", "File read failed: " + e.toString());
        }
        return buffer;
    } // readFromFile end

    private void writeDB(String position, String comment, String time, SQLiteDatabase database, byte[] b) {
        String sql = "INSERT INTO CAMERA (ID, IMAGE, COMMENT, TIME) VALUES (?, ?, ?, ?)";

        try {
            database.beginTransaction();
            SQLiteStatement insertStmt = database.compileStatement(sql);
            insertStmt.clearBindings();
            insertStmt.bindString(1, position);
            insertStmt.bindBlob(2, b);
            insertStmt.bindString(3, comment);
            insertStmt.bindString(4, time);
            insertStmt.executeInsert();
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.w("Exception", e);
        } finally {
            database.endTransaction();
        }
    }

    private void updateDB(String position, String comment) {
        String sql = "UPDATE CAMERA SET COMMENT = ? WHERE ID = ?";

        try {
            database.beginTransaction();
            SQLiteStatement insertStmt = database.compileStatement(sql);
            insertStmt.clearBindings();
            insertStmt.bindString(1, comment);
            insertStmt.bindString(2, position);
            insertStmt.executeInsert();
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.w("Exception", e);
        } finally {
            database.endTransaction();
        }
    }

    private void removeDBimage(SQLiteDatabase database, int position) {
        String sql = "UPDATE CAMERA SET ID = ? WHERE ID = ?";

        try {
            database.beginTransaction();
            database.execSQL("DELETE FROM CAMERA WHERE ID = " + Integer.toString(position));
            int size = (int) DatabaseUtils.queryNumEntries(database, "CAMERA");

            SQLiteStatement insertStmt = database.compileStatement(sql);
            for (int i = position; i < size; i++) {
                insertStmt.clearBindings();
                insertStmt.bindString(1, Integer.toString(i));
                insertStmt.bindString(2, Integer.toString(i + 1));
                insertStmt.executeInsert();
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.w("Exception", e);
        } finally {
            database.endTransaction();
        }
    }

    private byte[] readDBimage(SQLiteDatabase database, int position) {
        String sql = "SELECT IMAGE FROM CAMERA WHERE ID = " + position;
        byte[] b = new byte[1024];
        Cursor cursor = database.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            b = cursor.getBlob(cursor.getColumnIndex("IMAGE"));
        }
        return b;
    }

    private String readDBmessage(SQLiteDatabase database, int position) {
        String sql = "SELECT COMMENT FROM CAMERA WHERE ID = " + position;
        String s = "";
        Cursor cursor = database.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            s = cursor.getString(cursor.getColumnIndex("COMMENT"));
        }
        return s;
    }

    private String readDBtime(SQLiteDatabase database, int position) {
        String sql = "SELECT TIME FROM CAMERA WHERE ID = " + position;
        String s = "";
        Cursor cursor = database.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            s = cursor.getString(cursor.getColumnIndex("TIME"));
        }
        return s;
    }

    private void setToast(Boolean enableDebug, String message){
        if (enableDebug) {
            Toast.makeText(DisplayActivity.this, message,
                    Toast.LENGTH_SHORT).show();
        }
    }

}
