package com.confused.disease_tracker.service;


import android.app.Service;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.text.ParseException;
import java.util.Date;

public class DetectorService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static long[] unixTimestamp() throws ParseException {
        long[] timestamp;
        Date date1 = new Date();
        String strDate = java.time.LocalDate.now().minusDays(14)+"T"+date1.getHours()+":"+date1.getMinutes();
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        Date date2 = date.parse(strDate);
        Log.d("Unix", "Time1: " + date1);
        Log.d("Unix", "Time2: " + date2);
        timestamp = new long[]{date1.getTime(), date2.getTime()};
        return timestamp;
    }
}
