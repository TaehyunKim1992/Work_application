package com.example.razer.sample_1;

import android.app.Application;

/**
 * Created by Razer on 2016-05-24.
 */
public class UserSetting extends Application{
    private static Boolean beaconnoti = true;
    private static Boolean screenon = true;
    private static Boolean vibrate = true;
    private static Boolean location = true;
    private static Boolean noti_distance = true;
    private static Double user_lat = 0.0;
    private static Double user_long = 0.0;

    public static Double getUser_lat() {
        return user_lat;
    }

    public static void setUser_lat(Double user_lat) {
        UserSetting.user_lat = user_lat;
    }

    public static Double getUser_long() {
        return user_long;
    }

    public static void setUser_long(Double user_long) {
        UserSetting.user_long = user_long;
    }

    public static Boolean getScreenon() {
        return screenon;
    }

    public static void setScreenon(Boolean screenon) {
        UserSetting.screenon = screenon;
    }

    public static Boolean getVibrate() {
        return vibrate;
    }

    public static void setVibrate(Boolean vibrate) {
        UserSetting.vibrate = vibrate;
    }

    public static Boolean getLocation() {
        return location;
    }

    public static void setLocation(Boolean location) {
        UserSetting.location = location;
    }

    public static Boolean getNoti_distance() {
        return noti_distance;
    }

    public static void setNoti_distance(Boolean noti_distance) {
        UserSetting.noti_distance = noti_distance;
    }

    public static Boolean getBeaconnoti() {
        return beaconnoti;
    }
    public static void setBeaconnoti(Boolean beaconnoti) {
        UserSetting.beaconnoti = beaconnoti;
    }
}
