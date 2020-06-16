package com.confused.disease_tracker.helper;

import android.os.Build;

import androidx.annotation.RequiresApi;

import org.threeten.bp.LocalDateTime;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.Date;

public class AlgorithmHelper {
    public static double calDistance(double lat1, double lon1, double lat2, double lon2) {
        // The math module contains a function
        // named toRadians which converts from
        // degrees to radians.

        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // Haversine formula
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2),2);
        double c = 2 * Math.asin(Math.sqrt(a));

        // Radius of earth in kilometers. Use 3956
        // for miles
        double r = 6371;
        double result = c* r;
        // calculate the result
        return(result);
    }

    // Main of function
    public static LocalDateTime[] time(int range, LocalDateTime date) {
        int[] time = {range/60, (range-(range/60)*60)};
        // Compute to getting range r1 to r2;
        LocalDateTime r1 = realTime(date.getHour()-time[0], date.getMinute()-time[1], date);
        LocalDateTime r2 = realTime(date.getHour()+time[0], date.getMinute()+time[1], date);
        LocalDateTime[] timeRange = {r1, r2};
        return timeRange;
    }

    public static LocalDateTime realTime(int hr, int min, LocalDateTime date) {
        DecimalFormat formatter = new DecimalFormat("00");
        LocalDateTime d = date;
        if(min >= 60) {
            hr = hr+min/60;
            min = min-(min/60)*60;
        }else if(min < 0){
            min = min * -1;
            hr = hr-1;
        }
        if(hr >= 24) {
            hr = hr - 24;
            d = date.plusDays(1);
        }else if(hr < 0) {
            hr = 24-(hr * -1);
            d = date.minusDays(1);
        }
        return LocalDateTime.parse(String.valueOf(d).substring(0,10)+"T"+formatter.format(hr)+":"+formatter.format(min));
    }

    public static boolean isInRange(LocalDateTime user, LocalDateTime patient, int min) {
        LocalDateTime t1date = time(min, patient)[0];
        LocalDateTime t2date = time(min, patient)[1];
        //System.out.println("Is "+user+" BETWEEEN "+t1date+" AND "+t2date+" => "+(user.isAfter(t1date) && user.isBefore(t2date)));
        return user.isAfter(t1date) && user.isBefore(t2date);
    }

    public static int[] getRandomIntegerBetweenRange(double min, double max){
        double x = (int)(Math.random()*((max-min)+1))+min;
        double y = (int)(Math.random()*((max-min)+1))+min;
        double z = (int)(Math.random()*((max-min)+1))+min;
        int[] series = {(int) x,(int) y,(int) z};
        return series;
    }


}
