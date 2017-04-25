/*
* File name: MyHelper.java
* Author: Edward Ding #040078518
* Course: CST2335
* Assignment: Final Project
* Date: April 27, 2017
* Professor: David Lareau
* Purpose: Helper database file for photo app
* Class list: MainEdActivity, DisplayActivity, ImageAdapter
*/
/**
 * class MyHelper
 * @author Eding
 * @version 1
 */
package leeboelsma.final_project;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by HP on 4/15/2017.
 */

public class MyHelper extends SQLiteOpenHelper {

    public SQLiteDatabase db;

    MyHelper(Context context, String name){
        super(context, name, null, 1);
    }

    public void onCreate(SQLiteDatabase db) {
        this.db = db;
        db.execSQL("CREATE TABLE CAMERA(ID INTEGER NOT NULL, IMAGE BLOB, COMMENT TEXT, TIME TEXT)");
    }

    public void onUpgrade(SQLiteDatabase db, int
            oldVersion, int newVersion) {
// drop and re-create table or alter table
    }

    public void onUpdate(SQLiteDatabase db, int
            oldVersion, int newVersion) {}

    public SQLiteDatabase getWriteDatabase()
    {
        return db;
    }

}
