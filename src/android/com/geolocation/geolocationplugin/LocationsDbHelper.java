package com.geolocation.geolocationplugin;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class LocationsDbHelper extends SQLiteOpenHelper {

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + LocationsDbContract.Locations.TABLE_NAME + " (" +
                    LocationsDbContract.Locations._ID + " INTEGER PRIMARY KEY," +
                    LocationsDbContract.Locations.COLUMN_NAME_LONGITUDE + " TEXT," +
                    LocationsDbContract.Locations.COLUMN_NAME_LATITUDE + " TEXT," +
                    LocationsDbContract.Locations.COLUMN_NAME_DISTANCE + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + LocationsDbContract.Locations.TABLE_NAME;

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Locations.db";

    public LocationsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    public void onClear(SQLiteDatabase db){
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}