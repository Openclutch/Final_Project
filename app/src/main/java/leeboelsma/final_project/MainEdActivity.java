/*
* File name: MainEdActivity.java
* Author: Edward Ding #040078518
* Course: CST2335
* Assignment: Final Project
* Date: April 27, 2017
* Professor: David Lareau
* Purpose: Main activity file for photo app
* Class list: DisplayActivity, ImageAdapter, MyHelper
*/
/**
 * class MainEdActivity
 * @author Eding
 * @version 1
 */
package leeboelsma.final_project;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;


/**
 * Created by HP on 4/15/2017.
 */

public class MainEdActivity extends Activity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int MY_REQUEST_CODE = 5;
    private SharedPreferences sharedPref;
    private ImageAdapter adapter;
    private MyHelper myHelper;
    private SQLiteDatabase database;
    private ArrayList<Bitmap> bitmaps;
    private ImageButton b1;
    private Button b2, b3;
    private String timestamp = "";
    private Boolean enableDebug;
    private MenuItem item;

    // Sharing button things...
    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_ed);
        sharedPref = getSharedPreferences("Prefs", Context.MODE_PRIVATE);

        myHelper = new MyHelper(this, "CAMERA.db");
        database = myHelper.getReadableDatabase();
        this.bitmaps = new ArrayList<Bitmap>();
        dbFillArray(database, bitmaps);
        database.close();

        GridView gridView = (GridView) findViewById(R.id.ed_gridview);
        adapter = new ImageAdapter(this, bitmaps);
        gridView.setAdapter(adapter); // set adapter
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Intent intent = new Intent(MainEdActivity.this, DisplayActivity.class);
                intent.putExtra("ButtonIndex", Integer.toString(position));
                startActivityForResult(intent, MY_REQUEST_CODE);
            }
        });

        b1 = (ImageButton) findViewById(R.id.ed_SnapPicture);
        b1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        b2 = (Button) findViewById(R.id.ed_ReviewPic);
        b2.setTextSize(10);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainEdActivity.this, DisplayActivity.class);
                intent.putExtra("ButtonIndex", "NEW");
                intent.putExtra("TimeStamp", timestamp);
                startActivityForResult(intent, MY_REQUEST_CODE);
            }
        });

        b3 = (Button) findViewById(R.id.ed_DeletePic);
        b3.setTextSize(10);
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFile("tempfile");
                b1.setImageResource(android.R.drawable.ic_menu_camera);
                b2.setVisibility(View.INVISIBLE);
                b3.setVisibility(View.INVISIBLE);
            }
        });

        b1.setImageResource(android.R.drawable.ic_menu_camera);
        b2.setVisibility(View.INVISIBLE);
        b3.setVisibility(View.INVISIBLE);
        deleteFile("tempfile");
    }

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity1: {
                Intent intent = new Intent(this, Weather.class);
                startActivity(intent);
                return true;
            }
            case R.id.activity2: {
                return true;
            }
            case R.id.activity3: {
                return true;
            }
            case R.id.activity4: {
                getToast();
                return true;
            }
            case R.id.settings: {
                getUpload();
                return true;
            }
            case R.id.about: {
                getAbout();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            writeToFile("tempfile", getBytes(imageBitmap));
            b1.setImageBitmap(imageBitmap);
            b2.setVisibility(View.VISIBLE);
            b3.setVisibility(View.VISIBLE);

            timestamp = getTime();

            setToast(enableDebug, "Picture Taken at " + timestamp);

        } else if (requestCode == MY_REQUEST_CODE && resultCode == RESULT_OK) {
            String result = data.getStringExtra("Command");

            setToast(enableDebug, "Action or Delete ID: " + result);

            switch (result) {
                case "NOTHING":
                    break;
                case "CLEAR":
                    b1.setImageResource(android.R.drawable.ic_menu_camera);
                    b2.setVisibility(View.INVISIBLE);
                    b3.setVisibility(View.INVISIBLE);
                    break;
                case "SAVE":
                    b1.setImageResource(android.R.drawable.ic_menu_camera);
                    b2.setVisibility(View.INVISIBLE);
                    b3.setVisibility(View.INVISIBLE);
                    myHelper = new MyHelper(MainEdActivity.this, "CAMERA.db");
                    database = myHelper.getReadableDatabase();
                    int size = (int) DatabaseUtils.queryNumEntries(database, "CAMERA");
                    bitmaps.add(getImage(readDBimage(database, size - 1)));
                    database.close();
                    adapter.notifyDataSetChanged();
                    break;
                default:
                    bitmaps.remove(Integer.parseInt(result));
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    }

    private void getAbout() {
        final Activity activity = this;

        LayoutInflater li = LayoutInflater.from(activity);
        View view = li.inflate(R.layout.about_box_ed, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.ed_about_title);
        builder.setView(view);

        builder.setPositiveButton(android.R.string.ok, null); // a null listener defaults to dismissing the dialog.
        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        dialog.show();

        TextView textView = (TextView) dialog.findViewById(R.id.textView);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void getUpload() {
        final Activity activity = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.ed_load_title);
        builder.setMessage(R.string.ed_load_message);
        builder.setPositiveButton(R.string.ed_load, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                adapter.notifyDataSetInvalidated();
                myHelper = new MyHelper(MainEdActivity.this, "CAMERA.db");
                database = myHelper.getWritableDatabase();
                dbReload(database, getTime());
                dbFillArray(database, bitmaps);
                database.close();

                adapter.notifyDataSetChanged();

                setToast(enableDebug, "Reload Drawable Images");
            }
        });

        builder.setNegativeButton(R.string.ed_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                adapter.notifyDataSetInvalidated();
                myHelper = new MyHelper(MainEdActivity.this, "CAMERA.db");
                database = myHelper.getWritableDatabase();
                database.execSQL("DELETE FROM CAMERA");
                bitmaps.clear();
                database.close();

                adapter.notifyDataSetChanged();

                setToast(enableDebug, "Clear All Pictures");
            }
        });

        builder.setNeutralButton(R.string.ed_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        builder.setOnCancelListener(
                new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                    }
                }
        );

        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        dialog.show();
    }

    private void getToast() {
        final Activity activity = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Debug");
        builder.setMessage("Set Toast Debug Message");
        builder.setPositiveButton(R.string.ed_enable, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("ToastKey", true);
                editor.commit();
                enableDebug = true;
            }
        });

        builder.setNegativeButton(R.string.ed_disable, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("ToastKey", false);
                editor.commit();
                enableDebug = false;
            }
        });

        builder.setOnCancelListener(
                new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                    }
                }
        );

        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        enableDebug = sharedPref.getBoolean("ToastKey", false);
    }

    private void dbReload(SQLiteDatabase database, String time) {
        String sql = "INSERT INTO CAMERA (ID, IMAGE, COMMENT, TIME) VALUES (?, ?, ?, ?)";
        SQLiteStatement insertStmt;
        Bitmap bitmap;
        byte[] b;
        String comment = "";
        String position;
        // references to our images
        Integer[] mThumbIds = {
                R.drawable.photo1, R.drawable.photo2,
                R.drawable.photo3, R.drawable.photo4,
                R.drawable.photo5, R.drawable.photo6,
                R.drawable.photo7, R.drawable.photo8,
                R.drawable.photo9, R.drawable.photo10,
                R.drawable.photo11, R.drawable.photo12,
                R.drawable.photo13, R.drawable.photo14,
                R.drawable.photo15, R.drawable.photo16,
                R.drawable.photo17, R.drawable.photo18,
                R.drawable.photo19, R.drawable.photo20,
                R.drawable.photo21, R.drawable.photo22,
                R.drawable.photo23, R.drawable.photo24
        };

        try {
            database.beginTransaction();

            database.execSQL("DELETE FROM CAMERA");

            int i = 0;
            insertStmt = database.compileStatement(sql);

            for (Integer s : mThumbIds) {
                bitmap = BitmapFactory.decodeResource(this.getResources(), s);
                b = getBytes(bitmap);
                position = Integer.toString(i++);
                insertStmt.clearBindings();
                insertStmt.bindString(1, position);
                insertStmt.bindBlob(2, b);
                insertStmt.bindString(3, comment);
                insertStmt.bindString(4, time);
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

    private void writeToFile(String filename, byte[] bytes) {
        try {

            FileOutputStream out = openFileOutput(filename, MODE_PRIVATE);
            String line;
            for (byte b : bytes) {
                out.write(b);
            }
            out.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    } // writeToFile end

    // convert from byte array to bitmap
    private Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    // convert from bitmap to byte array
    private byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 0, stream);
        return stream.toByteArray();
    }

    private String getTime() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        return String.format("%4d-%02d-%02d %02d:%02d:%02d", year, month, day, hour, minute, second);
    }

    private void dbFillArray(SQLiteDatabase database, ArrayList<Bitmap> bitmaps) {
        String sql = "SELECT IMAGE FROM CAMERA ORDER BY ID ";
        bitmaps.clear();
        Cursor cursor = database.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            byte[] b = cursor.getBlob(cursor.getColumnIndex("IMAGE"));
            bitmaps.add(getImage(b));
        }
    }

    private void setToast(Boolean enableDebug, String message){
        if (enableDebug) {
            Toast.makeText(MainEdActivity.this, message,
                    Toast.LENGTH_SHORT).show();
        }
    }
}
