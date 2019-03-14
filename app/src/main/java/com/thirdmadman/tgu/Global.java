package com.thirdmadman.tgu;
import android.app.Fragment;
import android.os.AsyncTask;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URL;
import java.util.Map;

public class Global {
    public static Map<String, String> authCookies = null;
    public static Map<String, String> supportAuthCookies = null;
    public static String userSiteName= "";
    public static boolean firstLaunch = true;
    public static String savedGetInfo = "";
    public static final Double CurrentVersion = 0.310;
    public static String getInfoTitle = "";
    public static String nowWeekNumber = "-1";
    public static String supportTicketNumber = null;
    public static String parsit (String strSource, String strStart, String strEnd)
    {
        int iPos,iEnd;
        int lenStart  = strStart.length();
        int startPos = 0;
        String strResult = "";
        if (strSource != "" & strStart != "" & strEnd != "")
        {
            iPos = strSource.indexOf(strStart, startPos);
            iEnd = strSource.indexOf(strEnd, iPos + lenStart);

            if (iPos != -1 & iEnd != -1)
            {
                strResult = strSource.substring(iPos + lenStart, iEnd);
            }
        }
        else
        {
            strResult = "";
        }
        return strResult;
    }
}
