package com.confused.disease_tracker.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.confused.disease_tracker.Setting;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import java.util.TimeZone;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "disease_tracker";
    private static final String TAB1 = "userlocation";
    private static final String TAB2 = "hospital";
    private static final String TAB3 = "patient";
    private static final String TAB4 = "patientlocation";
    private static final String TAB5 = "alert_history";
    private SQLiteDatabase sqLiteDatabase;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.sqLiteDatabase = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE "+TAB1+" (" +
                "LID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "USERID VARCHAR(40),"+
                "LAT DOUBLE," +
                "LNG DOUBLE,"+
                "TIMESTAMP VARCHAR2(50),"+
                "isMajor INT(1)"+
                ")");
        sqLiteDatabase.execSQL("CREATE TABLE "+TAB2+"(" +
                "HOSPITALID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "HOSPITALNAME VARCHAR2(80),"+
                "LAT DOUBLE," +
                "LNG DOUBLE,"+
                "DISEASE VARCHAR2(1000)"+
                ")");
        sqLiteDatabase.execSQL("CREATE TABLE "+TAB3+"(" +
                "PID VARCHAR2(40), " +
                "NAME VARCHAR2(80),"+
                "DISEASE VARCHAR2(80)," +
                "STATUS VARCHAR2(80)"+
                ")");
        sqLiteDatabase.execSQL("CREATE TABLE "+TAB4+"(" +
                "PID VARCHAR2(40)," +
                "LID VARCHAR2(40), " +
                "LAT DOUBLE," +
                "LNG DOUBLE,"+
                "TIMESTAMP VARCHAR2(50)"+
                ")");
        sqLiteDatabase.execSQL("CREATE TABLE "+TAB5+"(" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "UID VARCHAR2(40)," +
                "PID VARCHAR2(40)," +
                "USR_LAT DOUBLE," +
                "USR_LNG DOUBLE,"+
                "PAT_LAT DOUBLE," +
                "PAT_LNG DOUBLE,"+
                "TIMESTAMP VARCHAR2(50),"+
                "ALERT VARCHAR2(200),"+
                "RISK INT(1),"+
                "IS_READ INT(1)"+
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TAB1);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TAB2);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TAB3);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TAB4);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TAB5);
        onCreate(sqLiteDatabase);
    }

    // Tab1
    public boolean insertUserLocation(String userid, double lat, double lng, int isMajor){
        LocalTime localTimeInBangkok = LocalTime.now(ZoneId.of("Asia/Bangkok"));
        LocalDate localDateInBangkok = LocalDate.now(ZoneId.of("Asia/Bangkok"));
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        ContentValues contentValues = new ContentValues();
        contentValues.put("USERID", userid);
        contentValues.put("LAT", lat);
        contentValues.put("LNG", lng);
        contentValues.put("TIMESTAMP", localDateInBangkok.format(dateFormat)+"T"+localTimeInBangkok.format(timeFormat));
        contentValues.put("isMajor", isMajor);
        long result = sqLiteDatabase.insert(TAB1, null, contentValues);
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    public Cursor getUserLocationData(String USERID){
     Cursor res = sqLiteDatabase.rawQuery("SELECT * FROM "+TAB1+" WHERE isMajor=1 AND USERID = '"+USERID+"' ORDER BY USERID, LID",null);
     return res;
    }

    public Cursor getUserLastLocationData(String USERID){
        Cursor res = sqLiteDatabase.rawQuery("SELECT * FROM "+TAB1+" WHERE USERID = '"+USERID+"' ORDER BY LID  DESC LIMIT 1",null);
        return res;
    }

    public Cursor getUserLastMajorLocationData(String USERID){
        Cursor res = sqLiteDatabase.rawQuery("SELECT * FROM "+TAB1+" WHERE USERID = '"+USERID+"' AND isMajor = 1 ORDER BY LID  DESC LIMIT 1",null);
        return res;
    }

    public Cursor getUserLocationTrack(String USERID){
        Cursor res = sqLiteDatabase.rawQuery("SELECT * FROM "+TAB1+" WHERE USERID = '"+USERID+"'",null);
        return res;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Cursor getUserLocationDataByDate(String USERID){
        String d1 = String.valueOf(LocalDateTime.now().plusDays(1)).split("T")[0];
        String d2 = String.valueOf(LocalDateTime.now().minusDays(15)).split("T")[0];
        Cursor res = sqLiteDatabase.rawQuery("SELECT * FROM "+TAB1+" WHERE USERID = '"+USERID+"' AND TIMESTAMP BETWEEN '"+d2+"' AND '"+d1+"' ORDER BY USERID, LID",null);
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

    public void dropHospital(){
        sqLiteDatabase.execSQL("DELETE FROM "+TAB2);
    }

    // Tab3
    public boolean insertPatient(String pid, String name, String disease, String status){
        ContentValues contentValues = new ContentValues();
        contentValues.put("PID", pid);
        contentValues.put("NAME", name);
        contentValues.put("DISEASE", disease);
        contentValues.put("STATUS", status);
        long result = sqLiteDatabase.insert(TAB3, null, contentValues);
        if(result == -1){
            Log.d("Patient/Insert",pid+", "+name+", "+disease+", "+status+": false");
            return false;
        }else{
            Log.d("Patient/Insert",pid+", "+name+", "+disease+", "+status+": true");
            return true;
        }
    }

    public Cursor getPatientData(){
        Cursor res = sqLiteDatabase.rawQuery("SELECT * FROM "+TAB3,null);
        return res;
    }

    public void dropPatient(){
        sqLiteDatabase.execSQL("DELETE FROM "+TAB3);
    }

    // Tab4
    public boolean insertPatientLocation(String pid, String lid, double lat, double lng, String timestamp){
        ContentValues contentValues = new ContentValues();
        contentValues.put("PID", pid);
        contentValues.put("LID", lid);
        contentValues.put("LAT", lat);
        contentValues.put("LNG", lng);
        contentValues.put("TIMESTAMP", timestamp);
        long result = sqLiteDatabase.insert(TAB4, null, contentValues);
        if(result == -1){
            return false;
        }else{
            Log.d("PatientLoc/Insert",pid+", "+lid+", "+lat+", "+lng+", "+timestamp+" : false");
            return true;
        }
    }

    public Cursor getPatientLocationData(String pid){
        String d1 = String.valueOf(LocalDateTime.now().plusDays(1)).split("T")[0];
        String d2 = String.valueOf(LocalDateTime.now().minusDays(15)).split("T")[0];
        Cursor res = sqLiteDatabase.rawQuery("SELECT * FROM "+TAB4+" WHERE PID = '"+pid+"' AND TIMESTAMP BETWEEN '"+d2+"' AND '"+d1+"' ORDER BY PID, LID",null);
        return res;
    }

    public void dropPatientLocation(){
        sqLiteDatabase.execSQL("DELETE FROM "+TAB4);
    }

    // Tab5
    public boolean insertAlertHistory(String uid, String pid, double usr_lat, double usr_lng, double pat_lat, double pat_lng, String timestamp, String alert, int risk){
        Log.d("AlertHistory/Insert",uid+", "+pid+", "+usr_lat+", "+usr_lng+", "+pat_lat+", "+pat_lng+", "+timestamp+", "+alert+", "+risk);
        ContentValues contentValues = new ContentValues();
        contentValues.put("UID", uid);
        contentValues.put("PID", pid);
        contentValues.put("USR_LAT", usr_lat);
        contentValues.put("USR_LNG", usr_lng);
        contentValues.put("PAT_LAT", pat_lat);
        contentValues.put("PAT_LNG", pat_lng);
        contentValues.put("TIMESTAMP", timestamp);
        contentValues.put("ALERT", alert);
        contentValues.put("RISK", risk);
        contentValues.put("IS_READ", 0);
        long result = sqLiteDatabase.insert(TAB5, null, contentValues);
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    public Cursor getAlertHistory(String uid){
        Cursor res = sqLiteDatabase.rawQuery("SELECT * FROM "+TAB5+" WHERE UID = '"+uid+"'",null);
        return res;
    }

    public Cursor getAlertHistory(String uid, String pid, double usr_lat, double usr_lng, double pat_lat, double pat_lng ){
        Cursor res = sqLiteDatabase.rawQuery("SELECT * FROM "+TAB5+" WHERE UID = '"+uid+"' AND PID = '"+pid+"' AND USR_LAT = "+usr_lat+" AND USR_LNG = "+usr_lng+" AND PAT_LAT = "+pat_lat+" AND PAT_LNG = "+pat_lng ,null);
        return res;
    }

    public Cursor getAlertHistory(int id){
        Cursor res = sqLiteDatabase.rawQuery("SELECT * FROM "+TAB5+" WHERE ID = "+id,null);
        return res;
    }

}
