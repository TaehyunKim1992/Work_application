package com.example.razer.sample_1;

import android.app.Application;

/**
 * Created by Razer on 2016-04-12.
 */
public class UserInfo extends Application {
    private static String UserName ="";
    private static String MailAddress = "";
    private static String Pwd = "";
    private static int checklo = 0;

    public static String getPwd() {
        return Pwd;
    }

    public static void setPwd(String pwd) {
        Pwd = pwd;
    }

    public static String getUserName() {
        return UserName;
    }

    public static void setUserName(String userName) {
        UserName = userName;
    }

    public static String getMailAddress() {
        return MailAddress;
    }

    public static void setMailAddress(String mailAddress) {
        MailAddress = mailAddress;
    }

    public static int getChecklo() {
        return checklo;
    }

    public static void setChecklo(int checklo) {
        UserInfo.checklo = checklo;
    }
}
