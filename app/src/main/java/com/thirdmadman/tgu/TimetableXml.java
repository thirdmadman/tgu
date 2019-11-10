package com.thirdmadman.tgu;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.thirdmadman.tgu.utils.TextParser;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by thirdmadman on 30.11.2017.
 * Special class for reading, writing and DOSOMEACTION with parsed html timetable from edu.tltsu.ru
 * this piece of sheet really annoying, and "portal" so buggy
 * let me tell some story...or not? I don't know, really...because for now 12.02.2018 I disappointment so much
 * Its gone more than one month and I'm still not quite ready to do this
 * every time I come back to this code I crying. This difficult to write this app after long delays...(24.03.2018)
 * FUCKFUCKFUCKFUCK WHAT THIS CODE MEANS?! KURVA!!! (16.05.2018)
 * LOL, git added - epic win! (15.03.2019)
 */

public class TimetableXml {

    private int userId; //id of current user from portal
    private Map<String, String> authCookies = null; // authorization cookies of user with userId from portal
    private String xmlPath = "/unknown_user/";
    private Context appContext;
    private View view;

    public TimetableXml(int userId, Map<String, String> authCookies, Context appContext, View view) {
        this.userId = userId;
        this.authCookies = authCookies;
        this.appContext = appContext;
        this.xmlPath = appContext.getFilesDir().getPath() + "/" + userId + "/"; // TODO: make sure that this is right path to files
        if (!new File(xmlPath).exists()) {
            new File(xmlPath).mkdir();
        }
        this.view = view;
    }

    public String getXmlPath() {
        return this.xmlPath;
    }

    public void setAuthCookies(Map<String, String> authCookies) {
        this.authCookies = authCookies;
    }

    public TimetableXml(int userId, Map<String, String> authCookies, String customPath, Context appContext) {
        this.userId = userId;
        this.authCookies = authCookies;
        this.appContext = appContext;
        this.xmlPath = appContext.getFilesDir().getPath() + "/" + customPath + "/"; // TODO: make sure that this is right path to files
    }

    public WeeklyTimetable getTimetableForNow(String arg) {

        WeeklyTimetable wt = null;

        int avWeeks = getAvailableWeeksNumber();
        if (avWeeks != 0) {
            String[][] avWeeksArray = getAvailableWeeks();
            if (avWeeksArray != null) {
                if (arg.equals("")) {
                    Date date = new Date();
                    DateFormat dateFormatDayName = new SimpleDateFormat("EEEE", Locale.US);
                    DateFormat dateCompare = new SimpleDateFormat("dd.MM");

                    int NowDayNumber = getDayNumber(dateFormatDayName.format(date));

                    Calendar instance = Calendar.getInstance();
                    instance.setTime(date); //устанавливаем дату, с которой будет производить операции
                    instance.add(Calendar.DAY_OF_MONTH, -NowDayNumber + 1);
                    Date newDate = instance.getTime();
                    String compDate = dateCompare.format(newDate);
                    for (int i = 0; i < avWeeks; i++) {
                        if (avWeeksArray[i][3].equals(compDate)) {
                            wt = readTimetablefromXml(avWeeksArray[i][5]);
                        }
                    }
                } else if (!arg.equals("")) {
                    for (int i = 0; i < avWeeks; i++) {
                        if (avWeeksArray[i][1].equals(arg)) {
                            wt = readTimetablefromXml(avWeeksArray[i][5]);
                            break;
                        }
                    }
                    if (authCookies != null && wt == null) {
                        wt = downloadTimetableForWeek(arg);
                    } else {
                        //Snackbar.make(view, "Вы ещё не вошли в аккаунт", Snackbar.LENGTH_LONG).show();
                    }
                }
                if (wt == null) {
                    if (authCookies != null) {
                        //wt = downloadTimetableForNow();
                    } else {
                        //Snackbar.make(view, "Вы ещё не вошли в аккаунт", Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        } else {
            if (authCookies != null) {
                writeTimetableListToXml(downloadTimetableList());
                if (!arg.equals("")) {
                    wt = downloadTimetableForWeek(arg);
                }
            } else if (authCookies == null && (arg == null || arg.equals(""))) {
                Snackbar.make(view, "Вы ещё не вошли в аккаунт", Snackbar.LENGTH_LONG).show();
            }
        }

        return wt;
    }

    public WeeklyTimetable updateTimetableForWeek(String arg) {
        WeeklyTimetable wt = null;
        if (authCookies != null && wt == null) {
            wt = downloadTimetableForWeek(arg);
        } else {
            Snackbar.make(view, "Вы ещё не вошли в аккаунт", Snackbar.LENGTH_LONG).show();
        }
        return wt;
    }

    public String[][] downloadTimetableList() {
        String[][] out = null;
        if (authCookies != null) {
            org.jsoup.nodes.Document page = downloadTimetableHtml("");
            if (page != null) {
                if (page.head().html().length() > 200) {
                    String pageHead = page.head().html();
                    String weeksjs = pageHead.substring(pageHead.indexOf("bo.className = \"weekbmbutton-on\";") + 33, pageHead.indexOf("<link rel=\"stylesheet\" href=\"/core/portal/kernel/css/mainMenu.css\" type=\"text/css\">"));
                    String weeksjsArray[] = weeksjs.split("showWeek");
                    String weeksList[][] = new String[2][weeksjsArray.length - 1];
                    for (int i = 1; i < weeksjsArray.length; i++) {
                        weeksjsArray[i] = TextParser.parseTextBetween("START" + weeksjsArray[i], "START(", ");");
                        weeksList[0][i - 1] = TextParser.parseTextBetween("START" + weeksjsArray[i], "START", ",");
                        weeksList[1][i - 1] = TextParser.parseTextBetween(weeksjsArray[i] + "END", ",", "END");
                    }
                    out = weeksList;
                } else {
                    Snackbar.make(view, "Данные не загружены, повторите попытку позже", Snackbar.LENGTH_LONG).show();
                }

            } else {
                //Snackbar.make(view, "Веротянее всего нет доступа к интеренту", Snackbar.LENGTH_LONG).show();
            }
        } else {
            //Snackbar.make(view, "Вы ещё не вошли в аккаунт", Snackbar.LENGTH_LONG).show();
        }
        return out;
    }

    private org.jsoup.nodes.Document downloadTimetableHtml(String args) {
        org.jsoup.nodes.Document page = null;
        try {
            page = Jsoup.connect("http://edu.tltsu.ru/edu/timetable.php" + args)
                    .cookies(authCookies)
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.print(e);
        }
        return page;
    }

    private WeeklyTimetable downloadTimetableForNow() {
        org.jsoup.nodes.Document page = downloadTimetableHtml("");
        WeeklyTimetable outData = null;
        String[][] timetableFid = new String[8][8];
        String[][] timetable = new String[8][8];
        //==========================
        if (page != null) {
            if (page.html().length() > 400) {
                Elements timetabel_td = null;
                Elements timetabel_tr = page.select("div.weekbmcontainer > div.weekbmpage[style*=display:block] > table > tbody tr");
                for (int i = 0; i < timetabel_tr.size(); i++) {
                    timetabel_td = timetabel_tr.get(i).select("td");
                    for (int o = 1; o < timetabel_td.size(); o++) {
                        timetable[o - 1][i] = timetabel_td.get(o).text();
                        if (!timetabel_td.get(o).id().equals("")) {
                            timetableFid[o - 1][i] = timetabel_td.get(o).id();
                        } else {
                            timetableFid[o - 1][i] = "";
                        }
                    }
                }
            }
            outData = new WeeklyTimetable(timetable, timetableFid);
            writeTimetableToXml(page);
        }
        return outData;
    }

    private WeeklyTimetable downloadTimetableForWeek(String week) {
        String weeksList[][] = readTimetableListFromXml("timetablelist.xml");
        String args = "";
        for (String[] strings : weeksList) {
            if (strings[1].equals(week)) {
                args = "?week=" + week + "&w_id=" + strings[2];
                break;
            }
        }
        org.jsoup.nodes.Document page = downloadTimetableHtml(args);
        WeeklyTimetable outData = null;
        String[][] timetableFid = new String[8][8];
        String[][] timetable = new String[8][8];
        //==========================
        if (page != null) {
            if (page.html().length() > 400) {
                Elements timetabel_td = null;
                Elements timetabel_tr = page.select("div.weekbmcontainer > div.weekbmpage[style*=display:block] > table > tbody tr");
                for (int i = 0; i < timetabel_tr.size(); i++) {
                    timetabel_td = timetabel_tr.get(i).select("td");
                    for (int o = 1; o < timetabel_td.size(); o++) {
                        timetable[o - 1][i] = timetabel_td.get(o).text();
                        if (!timetabel_td.get(o).id().equals("")) {
                            timetableFid[o - 1][i] = timetabel_td.get(o).id();
                        } else {
                            timetableFid[o - 1][i] = "";
                        }
                    }
                }
            }
            outData = new WeeklyTimetable(timetable, timetableFid);
            writeTimetableToXml(page);
        }
        return outData;
    }

    public String[][] getAvailableWeeks() {
        String[][] AvailableWeeks = null;
        if (new File(xmlPath + "timetablelist.xml").exists()) {
            try {
                final File xmlFile = new File(xmlPath + "timetablelist.xml");
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(xmlFile);
                NodeList nodeList = doc.getElementsByTagName("weeklytimetabel");
                if (nodeList.getLength() >= 1) {
                    AvailableWeeks = new String[getAvailableWeeksNumber()][7];
                    int z = 0;
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node node = nodeList.item(i);
                        if (Integer.parseInt(node.getAttributes().getNamedItem("available").getNodeValue()) == 1) {
                            //int week_number = Integer.parseInt(node.getAttributes().getNamedItem("week_number").getNodeValue());
                            AvailableWeeks[z][0] = node.getAttributes().getNamedItem("available").getNodeValue();
                            AvailableWeeks[z][1] = node.getAttributes().getNamedItem("week_number").getNodeValue();
                            AvailableWeeks[z][2] = node.getAttributes().getNamedItem("week_id").getNodeValue();
                            AvailableWeeks[z][3] = node.getAttributes().getNamedItem("start_date").getNodeValue();
                            AvailableWeeks[z][4] = node.getAttributes().getNamedItem("end_date").getNodeValue();
                            AvailableWeeks[z][5] = node.getAttributes().getNamedItem("file_name").getNodeValue();
                            AvailableWeeks[z][6] = node.getAttributes().getNamedItem("last_update").getNodeValue();
                            z++;
                        }
                    }
                    return AvailableWeeks;
                }
            } catch (ParserConfigurationException | SAXException
                    | IOException ex) {
            }
        } else {
            if (authCookies != null) {
                writeTimetableListToXml(downloadTimetableList());
                Snackbar.make(view, "Список недель пуст", Snackbar.LENGTH_LONG).show();
            }
            return null;
        }
        return AvailableWeeks;
    }

    private int getAvailableWeeksNumber() {
        int retutnNumber = 0;
        if (new File(xmlPath + "timetablelist.xml").exists()) {
            try {
                final File xmlFile = new File(xmlPath + "timetablelist.xml");
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(xmlFile);
                NodeList nodeList = doc.getElementsByTagName("timetablelist");
                Node timetablelist = nodeList.item(0);
                retutnNumber = Integer.parseInt(timetablelist.getAttributes().getNamedItem("weeksavailable").getNodeValue());
            } catch (ParserConfigurationException | SAXException
                    | IOException ex) {
            }
        } else {
            return retutnNumber;
        }
        return retutnNumber;
    }

    private int getDayNumber(String dayName) {
        int DayNumber = 0;
        String[] d = {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (int i = 1; i < 8; i++) {
            if (d[i].equals(dayName)) {
                DayNumber = i;
                break;
            }
        }
        return DayNumber;
    }

    public int writeTimetableToXml(org.jsoup.nodes.Document html) {
        //int dd = getAvalibleWeeksNumber(); //TODO:Del after debug of method!!!
        //String[][] zz= getAvalibleWeeks(); //
        //readTimetablefromXml(this.userId+"_2.xml");
        String[][] timetableFid = new String[8][8];
        String[][] timetable = new String[8][8];
        BufferedWriter bw = null;
        FileWriter fw = null;
        String weekNumber = "";
        int returnCode = 0;
        //==========================
        org.jsoup.nodes.Document page = html;
        if (page != null) {
            if (page.html().length() > 400) {

                Elements timetabel_td = null;
                Elements timetabel_tr = page.select("div.weekbmcontainer > div.weekbmpage[style*=display:block] > table > tbody tr");

                for (int i = 0; i < timetabel_tr.size() - 1; i++) {
                    timetabel_td = timetabel_tr.get(i).select("td");
                    for (int o = 0; o < timetabel_td.size(); o++) {
                        timetable[i][o] = timetabel_td.get(o).text();
                        if (!timetabel_td.get(o).id().equals("")) {
                            timetableFid[i][o] = timetabel_td.get(o).id();
                        } else {
                            timetableFid[i][o] = "";
                        }
                    }
                    //This fix is highly ugly
                    if (timetable[i][7] == null) {
                        timetable[i][7] = "";
                        timetableFid[i][7] = "";
                    }
                }
            }
            //==========================
            try {
                File timetablelistFile = new File(xmlPath);
                if (!timetablelistFile.exists()) {
                    timetablelistFile.mkdir();
                } else {
                    weekNumber = page.select("a.weekbmbutton-on").text().replaceAll("\\s+", "");
                    //weekNumber = weekNumber.replaceAll(" ","");
                    int weekIdFind1 = page.head().html().indexOf("showWeek(" + weekNumber + ",");

                    String weekId = page.head().html().substring(weekIdFind1, weekIdFind1 + 25);
                    weekId = TextParser.parseTextBetween(weekId, "showWeek(" + weekNumber + ",", ");");
                    String content = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><weeklytimetable>";
                    String[] d = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
                    for (int i = 1; i < 8; i++) {
                        for (int o = 0; o < 8; o++) {
                            if (o == 0) {
                                content += "<" + d[i - 1] + " name=\"" + timetable[o][i] + "\">";
                            } else {
                                if (timetable[o][i] == null) {
                                    String gg = "gg";
                                }
                                content += "<lesson number=\"" + o + "\" id=\"" + timetableFid[o][i] + "\">" + timetable[o][i] + "</lesson>";
                            }
                        }
                        content += "</" + d[i - 1] + ">";
                    }
                    content += "</weeklytimetable>";
                    String fileName = xmlPath + this.userId + "_" + weekNumber + "_" + weekId + ".xml";
                    fw = new FileWriter(fileName);
                    bw = new BufferedWriter(fw);
                    bw.write(content);

/*                    org.jsoup.nodes.Document nowWeekPage = downloadTimetableHtml("");
                    String nowWeekNumber = nowWeekPage.select("a.weekbmbutton-on").text();
                    String coreTime = nowWeekPage.select("td#core_time_place").text();
                    coreTime = coreTime.substring(0, coreTime.indexOf(" "));
                    SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy");
                    format.setLenient(false);
                    Date date = format.parse(coreTime);
                    DateFormat dateFormatDayName = new SimpleDateFormat("EEEE", Locale.US);
                    DateFormat dateOut = new SimpleDateFormat("dd.MM.yyyy");

                    int NowDayNumber = getDayNumber(dateFormatDayName.format(date));


                    Calendar instance = Calendar.getInstance();
                    instance.setTime(date); //устанавливаем дату, с которой будет производить операции
                    instance.add(Calendar.DAY_OF_MONTH, -NowDayNumber +1);
                    int difference = Integer.parseInt(nowWeekNumber) - Integer.parseInt(weekNumber);
                    if (difference > 0) {
                        instance.add(Calendar.WEEK_OF_YEAR, difference);
                    }
                    Date startDate = instance.getTime();
                    instance.add(Calendar.DAY_OF_MONTH, 6);
                    Date endDate = instance.getTime();
                    writeTimetableListToXml(weekNumber,weekId, dateOut.format(startDate), dateOut.format(endDate));*/
                    //all this complicated code... maybe just del it or not?
                    DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                    Date date = new Date();
                    String currentDate = dateFormat.format(date);

                    writeTimetableWeekToXml("1", weekNumber, weekId, timetable[0][1].substring(4, timetable[0][1].length()), timetable[0][7].substring(4, timetable[0][7].length()), fileName, currentDate);
                    returnCode = 1;
                }
            } catch (IOException e) {
                e.printStackTrace();
                returnCode = 0;
            } finally {
                try {
                    if (bw != null)
                        bw.close();
                    if (fw != null)
                        fw.close();

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return returnCode;
    }

    public int writeTimetableWeekToXml(String available, String weekNumber, String weekId, String startDate, String endDate, String fileName, String last_update) {
        int returnCode = 0;
        if (!available.equals("") && !weekNumber.equals("") && !startDate.equals("") && !endDate.equals("") && !fileName.equals("")) {
            File timetablelistDir = new File(xmlPath);
            if (!timetablelistDir.exists()) {
                timetablelistDir.mkdir();
            }
            File timetableXmlList = new File(xmlPath + "timetablelist.xml");
            if (timetableXmlList.exists()) {
                String[][] timeTableList = readTimetableListFromXml("timetablelist.xml");
                if (timeTableList != null) {
                    if (timeTableList.length > 0) {
                        for (int i = 0; i < timeTableList.length; i++) {
                            if (timeTableList[i][1].equals(weekNumber) && timeTableList[i][2].equals(weekId)) {
                                timeTableList[i][0] = available;
                                timeTableList[i][3] = startDate;
                                timeTableList[i][4] = endDate;
                                timeTableList[i][5] = fileName;
                                timeTableList[i][6] = last_update;
                                writeTimetableListToXml(timeTableList);
                            }
                        }
                    } else {
                        returnCode = 2;
                    }
                } else {
                    returnCode = 2;
                }
            } else {
                returnCode = 1;
            }
        }
        return returnCode;
    }

    //created 04.03.2018 // still in constriction
    //need do make updateTimetableList with readTimetableListfromXml and inserting new info in list with updating function writeTimetableListToXml
    public int writeTimetableListToXml(String[][] weeksList) {
        BufferedWriter bw = null;
        FileWriter fw = null;
        if (weeksList != null) {
            try {
                File timetablelistDir = new File(xmlPath);
                if (!timetablelistDir.exists()) {
                    timetablelistDir.mkdir();
                }
                //File timetableXmlList = new File(xmlPath + "timetablelist.xml");
                String content = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
                //Date date = new Date();
                //DateFormat dateOut = new SimpleDateFormat("dd.MM.yyyy");
                //String dateNow = dateOut.format(date);
                int weeksAvailable = 0;

                if (weeksList.length == 2 && weeksList[0].length > 2) {
                    content += "<timetablelist weeksnuber=\"" + weeksList[0].length + "\" weeksavailable=\"0\">";
                    for (int i = 0; i < weeksList[0].length; i++) {
                        content += "<weeklytimetabel available=\"0\" week_number=\"" + weeksList[0][i] + "\" week_id=\"" + weeksList[1][i] + "\" start_date=\"0\" end_date=\"0\" file_name=\"0\" last_update=\"0\"></weeklytimetabel>";
                    }
                } else {
                    for (int i = 0; i < weeksList.length; i++) {
                        if (weeksList[i][0].equals("1")) {
                            weeksAvailable++;
                        }
                    }
                    content += "<timetablelist weeksnuber=\"" + weeksList.length + "\" weeksavailable=\"" + weeksAvailable + "\">";
                    for (int i = 0; i < weeksList.length; i++) {
                        content += "<weeklytimetabel available=\"" + weeksList[i][0] + "\" week_number=\"" + weeksList[i][1] + "\" week_id=\"" + weeksList[i][2] + "\" start_date=\"" + weeksList[i][3] + "\" end_date=\"" + weeksList[i][4] + "\" file_name=\"" + weeksList[i][5] + "\" last_update=\"" + weeksList[i][6] + "\"></weeklytimetabel>";
                    }
                }
                content += "</timetablelist>";
                fw = new FileWriter(xmlPath + "timetablelist.xml");
                bw = new BufferedWriter(fw);
                bw.write(content);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bw != null)
                        bw.close();
                    if (fw != null)
                        fw.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return 0;
    }

    private int updateTimetableList(String[][] weeksList) {
        String readWeeksList[][] = readTimetableListFromXml("timetablelist.xml");
        int arryLenth = 0;
        if (readWeeksList[0].length > weeksList[0].length) {
            arryLenth = readWeeksList[0].length;
        } else {
            arryLenth = weeksList[0].length;
        }

        String outWeeksList[][] = new String[7][arryLenth];
        for (int i = 0; i < outWeeksList[0].length; i++) {
            if (weeksList.length > 2) {

            } else if (weeksList.length == 2) {

            }
        }
        return 0;
    }

    static String readFile(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        String ls = System.getProperty("line.separator");
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }
        // delete the last new line separator
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        reader.close();

        String content = stringBuilder.toString();
        return content;
    }

    public String[][] readTimetableListFromXml(String fileName) {
        String[][] AvailableWeeks = null;
        if (new File(xmlPath + fileName).exists()) {
            try {
                final File xmlFile = new File(xmlPath + fileName);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document docRoot = db.parse(xmlFile);
                NodeList weeksList = docRoot.getChildNodes().item(0).getChildNodes();
                if (weeksList.getLength() >= 1) {
                    AvailableWeeks = new String[weeksList.getLength()][7];
                    for (int i = 0; i < weeksList.getLength(); i++) {
                        Node week = weeksList.item(i);
                        AvailableWeeks[i][0] = week.getAttributes().getNamedItem("available").getNodeValue();
                        AvailableWeeks[i][1] = week.getAttributes().getNamedItem("week_number").getNodeValue();
                        AvailableWeeks[i][2] = week.getAttributes().getNamedItem("week_id").getNodeValue();
                        AvailableWeeks[i][3] = week.getAttributes().getNamedItem("start_date").getNodeValue();
                        AvailableWeeks[i][4] = week.getAttributes().getNamedItem("end_date").getNodeValue();
                        AvailableWeeks[i][5] = week.getAttributes().getNamedItem("file_name").getNodeValue();
                        AvailableWeeks[i][6] = week.getAttributes().getNamedItem("last_update").getNodeValue();
                    }
                    return AvailableWeeks;
                }
            } catch (ParserConfigurationException | SAXException
                    | IOException ex) {
            }
        } else {
            return AvailableWeeks;
        }
        return AvailableWeeks;
    }

    private WeeklyTimetable readTimetablefromXml(String fileName) {
        WeeklyTimetable returnI = null;
        String[][] timetabel = new String[8][8];
        String[][] timetableIds = new String[8][8];
        try {
            File inputFile = new File(fileName);
            if (inputFile.exists()) {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(inputFile);
                //doc.getDocumentElement().normalize();
                Node root = doc.getDocumentElement();
                NodeList nodeList = root.getChildNodes();
                int dd = nodeList.getLength();
                if (nodeList.getLength() > 0) {
                    timetabel = new String[nodeList.getLength()][8];
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node day = nodeList.item(i);
                        NodeList lesssons = day.getChildNodes();

                        for (int o = 0; o < lesssons.getLength() + 1; o++) {
                            if (o == 0) {
                                timetabel[i][0] = day.getAttributes().getNamedItem("name").getNodeValue();
                            } else {
                                Node lessson = lesssons.item(o - 1);
                                String wp = lessson.getAttributes().getNamedItem("number").getNodeValue();
                                String lessonVolue = lessson.getTextContent();
                                timetableIds[i][o] = lessson.getAttributes().getNamedItem("id").getNodeValue();
                                timetabel[i][o] = lessson.getTextContent();
                            }
                        }
                    }
                }
                returnI = new WeeklyTimetable(timetabel, timetableIds);
                String cutFilename = fileName.substring(fileName.indexOf("_") + 1);
                returnI.setWeekNumber(cutFilename.substring(0, cutFilename.indexOf("_")));
                returnI.setWeekId(cutFilename.substring(cutFilename.indexOf("_") + 1, cutFilename.indexOf(".")));
            }
        } catch (ParserConfigurationException | SAXException
                | IOException ex) {
            System.out.print(ex);
        }
        return returnI;
    }

    private int updateWeeksList(WeeklyTimetable timetable) {
        return 0;
    }

    public int deletTimetableForWeek(String weekNumber) {

        return 0;
    }

    public int deletTimetableAll() {
        File file = new File(xmlPath + "timetablelist.xml");
        if (file.exists()) {
            if (file.delete()) {
                System.out.println(file.getName() + " is deleted!");
            }
        } else {
            System.out.println("Delete operation is failed.");
        }
        return 0;
    }

    public String getErrorDescByCode(int errorCode) {

        return null;
    }
}
