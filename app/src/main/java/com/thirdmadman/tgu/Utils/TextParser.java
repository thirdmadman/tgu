package com.thirdmadman.tgu.Utils;

public class TextParser {
    public static String parseTextBetween(String strSource, String strStart, String strEnd)
    {
        int iPos,iEnd;
        int lenStart  = strStart.length();
        int startPos = 0;
        String strResult = "";
        if (!strSource.equals("")  && !strStart.equals("")  && !strEnd.equals(""))
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
