package com.confused.disease_tracker.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "disease_tracker";
    private static final String TAB1 = "userlocation";
    private static final String TAB2 = "hospital";
    private static final String TAB3 = "version";
    private SQLiteDatabase sqLiteDatabase;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.sqLiteDatabase = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE "+TAB1+" (" +
                "LID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "LAT DOUBLE," +
                "LNG DOUBLE"+
                ")");
        sqLiteDatabase.execSQL("CREATE TABLE "+TAB2+"(" +
                "HOSPITALID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "HOSPITALNAME VARCHAR2(80),"+
                "LAT DOUBLE," +
                "LNG DOUBLE,"+
                "DISEASE VARCHAR2(1000)"+
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TAB1);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TAB2);
        onCreate(sqLiteDatabase);
    }

    // Tab1
    public boolean insertUserLocation(double lat, double lng){
        ContentValues contentValues = new ContentValues();
        contentValues.put("LAT", lat);
        contentValues.put("LNG", lng);
        long result = sqLiteDatabase.insert(TAB1, null, contentValues);
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    public Cursor getUserLocationData(){
     Cursor res = sqLiteDatabase.rawQuery("SELECT * FROM "+TAB1,null);
     return res;
    }

    public Cursor getUserLastLocationData(){
        Cursor res = sqLiteDatabase.rawQuery("SELECT * FROM "+TAB1+" ORDER BY LID DESC LIMIT 1",null);
        return res;
    }


    // Tab2
    public boolean insertHospital(String hospitalname, double lat, double lng, String disease){
        ContentValues contentValues = new ContentValues();
        contentValues.put("HOSPITALNAME", hospitalname);
        contentValues.put("LAT", lat);
        contentValues.put("LNG", lng);
        contentValues.put("DISEASE", disease);
        long result = sqLiteDatabase.insert(TAB2, null, contentValues);
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    public Cursor getHospitalData(){
        Cursor res = sqLiteDatabase.rawQuery("SELECT * FROM "+TAB2,null);
        return res;
    }


}
