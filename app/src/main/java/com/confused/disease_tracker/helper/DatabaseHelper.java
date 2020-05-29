package com.confused.disease_tracker.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "disease_tracker";
    private static final String TAB1 = "userlocation";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE "+TAB1+"(" +
                "LID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "LAT DOUBLE," +
                "LNG DOUBLE"+
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TAB1);
        onCreate(sqLiteDatabase);
    }

    public boolean insertUserLocation(double lat, double lng){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
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
     SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
     Cursor res = sqLiteDatabase.rawQuery("SELECT * FROM "+TAB1,null);
     return res;
    }

}
