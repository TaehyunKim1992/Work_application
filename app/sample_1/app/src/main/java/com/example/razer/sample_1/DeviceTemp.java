package com.example.razer.sample_1;

import android.app.Application;

/**
 * Created by Razer on 2016-04-11.
 */
public class DeviceTemp extends Application {
    private static String DeviceName ="";
    private static int MajorNum =0;
    private static int MinorNum =0;
    private static String UUID ="";
    private static double Distance = 0;
    private static String TEMPEMAIL= "";

    public static String getTEMPEMAIL() {
        return TEMPEMAIL;
    }

    public static void setTEMPEMAIL(String TEMPEMAIL) {
        DeviceTemp.TEMPEMAIL = TEMPEMAIL;
    }

    public static double getDistance() {
        return Distance;
    }

    public static void setDistance(double distance) {
        Distance = distance;
    }

    public static String getDeviceName() {
        return DeviceName;
    }

    public static void setDeviceName(String deviceName) {
        DeviceName = deviceName;
    }

    public static int getMajorNum() {
        return MajorNum;
    }

    public static void setMajorNum(int majorNum) {
        MajorNum = majorNum;
    }

    public static int getMinorNum() {
        return MinorNum;
    }

    public static void setMinorNum(int minorNum) {
        MinorNum = minorNum;
    }

    public static String getUUID() {
        return UUID;
    }

    public static void setUUID(String UUID) {
        DeviceTemp.UUID = UUID;
    }
}
