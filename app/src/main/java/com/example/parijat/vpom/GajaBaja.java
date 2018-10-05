package com.example.parijat.vpom;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Parijat on 17-Nov-17.
 */
public class GajaBaja {
    int _id;
    String _latitude;
    String _longitude;
    String _file_names;
    String _geofence_id;
    public GajaBaja(){}

//    public GajaBaja(int id,String latitude, String longitude){
//        this._id = id;
//        this._latitude = latitude;
//        this._longitude = longitude;
//
//    }

    public GajaBaja(int id, String filenames, String latitude, String longitude, String geofenceid ){
        this._id = id;
        this._latitude = latitude;
        this._longitude = longitude;
        this._file_names = filenames;
        this._geofence_id =geofenceid;
    }

    public GajaBaja(String latitude, String longitude, String filenames, String geofenceid){
        this._file_names = filenames;
        this._latitude = latitude;
        this._longitude = longitude;
        this._geofence_id= geofenceid;
    }

    public int getID(){
        return this._id;
    }

    public void setID(int id){
        this._id = id;
    }

    public String getLatitude(){
        return this._latitude;
    }

    public void setLatitude( String latitude){
        this._latitude = latitude;
    }

    public String getLongitude(){
        return this._longitude;
    }

    public void setLongitude( String longitude){
        this._longitude = longitude;
    }

    public String getFilename(){
        return this._file_names;
    }

    public void setFilename(String file_names){
        this._file_names = file_names;

    }
    public String getGeofenceid(){
        return this._geofence_id;
    }

    public void setGeofenceid(String geofence_id){
        this._geofence_id = geofence_id;
    }


}


