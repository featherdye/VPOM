package com.example.parijat.vpom;



import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//import com.google.android.gms.maps.model.LatLng;
//import com.tinmegali.mylocation.GajaBaja;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "latlngSoundy";
    private static final String TABLE_CONTACTS = "FilenamesByLocation";
    private static final String KEY_ID = "id";
    private static final  String  KEY_LATITUDE = "latitude";
    private static final  String  KEY_LONGITUDE = "longitude";
    private static final String KEY_FILE_NAMES = "filenames";
    private static final String KEY_GEOFENCE_ID="GEOFENCEID";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //3rd argument to be passed is CursorFactory instance
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                +KEY_LATITUDE+" TEXT,"
                + KEY_LONGITUDE+" TEXT,"
                + KEY_FILE_NAMES + " TEXT, "
                +KEY_GEOFENCE_ID+" TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

        // Create tables again
        onCreate(db);
    }

    // code to add the new contact
    void addContact(GajaBaja gajaBaja) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LATITUDE, gajaBaja.getLatitude()); // Contact latitude
        values.put(KEY_LONGITUDE, gajaBaja.getLongitude());// contact longitude
        values.put(KEY_FILE_NAMES, gajaBaja.getFilename());// Contact filenames
        values.put(KEY_GEOFENCE_ID, gajaBaja.getGeofenceid());// Contact geofenceid
        // Inserting Row
        db.insert(TABLE_CONTACTS, null, values);
        //2nd argument is String containing nullColumnHack
        db.close(); // Closing database connection
    }


    String readbygeoid(String geoid){
        SQLiteDatabase db = this.getReadableDatabase();
        String str=null;
        String query = "select * from " + TABLE_CONTACTS + " where "+ KEY_GEOFENCE_ID + " = '" + geoid + "'";
        SQLiteDatabase sql = this.getReadableDatabase();
        Cursor cur = sql.rawQuery(query, null);
        Log.d("are re re",Integer.toString(cur.getColumnIndex(KEY_FILE_NAMES)));
        Log.d("Number of rows", Integer.toString(cur.getCount()));
        if (cur.getCount()> 0 && cur.moveToFirst()) {
            str = cur.getString(cur.getColumnIndex(KEY_FILE_NAMES));
            //Log.d("read the db and found",str);
        }
        return str;
    }


    // code to get all contacts in a list view
    public List<GajaBaja> getAllContacts() {
        List<GajaBaja> gajabajaList = new ArrayList<GajaBaja>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                GajaBaja gajabaja  = new GajaBaja();
                gajabaja.setID(Integer.parseInt(cursor.getString(0)));
                gajabaja.setLatitude(cursor.getString(1));
                gajabaja.setLongitude(cursor.getString(2));
                gajabaja.setFilename(cursor.getString(3));
                gajabaja.setGeofenceid(cursor.getString(4));
                // Adding contact to list
                gajabajaList.add(gajabaja);
            } while (cursor.moveToNext());
        }

        // return contact list
        return gajabajaList;
    }

    // code to update the single contact
    public int updateFilenames(GajaBaja gajabaja) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FILE_NAMES, gajabaja.getFilename());
        // updating row
        return db.update(TABLE_CONTACTS, values, KEY_GEOFENCE_ID + " = ?",
                new String[] { String.valueOf(gajabaja.getGeofenceid()) });
    }

    public boolean updatebygeoid(String geoid, String filename) {
        Log.d("now", "updatebygeoid: "+filename+"///////"+geoid);
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FILE_NAMES, filename);
        //values.put(KEY_GEOFENCE_ID, geoid);
        //add arguments for where clause
        String[] args = new String[]{geoid};

        int i = db.update(TABLE_CONTACTS, values, KEY_GEOFENCE_ID+"=?", args);

        return i>0;
    }


    // Deleting single contact
    public void deleteContact(GajaBaja gajabaja) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, KEY_ID + " = ?",
                new String[] { String.valueOf(gajabaja.getID()) });
        db.close();
    }

    // Getting contacts Count
    public int getContactsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_CONTACTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

}