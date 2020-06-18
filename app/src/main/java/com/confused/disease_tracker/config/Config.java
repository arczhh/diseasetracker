package com.confused.disease_tracker.config;

public class Config {
    // Disease
    private static String disease = "โควิด-19";
    private static String defaultStausUpload = "ยังไม่ได้รับการตรวจสอบ";

    // Config LocationService
    private static long collectPoint_refreshTime = 5; // minute
    private static int collectPoint_minDistance = 15;

    // Config DetectorService
    private static double cond1_distance = 0.015;
    private static double cond2_distance = 0.005;
    private static int range_min = 60; // minute
    private static int DetectorService_period_timework = 1; // minute

    // Config DataUpdateService
    private static int DataUpdateService_period_timework = 1; // minute

    // Splash Screen
    private static int SPLASH_TIME_OUT = 4000; // ms

    public static String getDisease() {
        return disease;
    }

    public static String getDefaultStausUpload() {
        return defaultStausUpload;
    }

    public static long getCollectPoint_refreshTime() {
        return collectPoint_refreshTime*60*1000;
    }

    public static int getCollectPoint_minDistance() {
        return collectPoint_minDistance;
    }

    public static double getCond1_distance() {
        return cond1_distance;
    }

    public static double getCond2_distance() {
        return cond2_distance;
    }

    public static int getRange_min() {
        return range_min;
    }

    public static int getDetectorService_period_timework() {
        return DetectorService_period_timework*1000*60;
    }

    public static int getDataUpdateService_period_timework() {
        return DataUpdateService_period_timework*1000*60;
    }

    public static int getSplashTimeOut() {
        return SPLASH_TIME_OUT;
    }

}
