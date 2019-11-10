package com.thirdmadman.tgu.activities;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.thirdmadman.tgu.GlobalSettings;
import com.thirdmadman.tgu.R;
import com.thirdmadman.tgu.TimetableXml;
import com.thirdmadman.tgu.WeeklyTimetable;
import com.thirdmadman.tgu.utils.TextParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by third on 08.09.2017.
 */

public class Timetable extends Fragment {
    String currentWeekNumber = "-1";
    public ProgressBar timetableprogressbar;
    public String htmltimetamle;
    public int clicedList = -1;
    ListView lst;
    String[] timetableArrayForWeek = new String[7];
    public String[] pairsinfo = new String[8];
    public String[][] timetableFid = new String[8][8];
    private String[] SpinnerArry;
    public SharedPreferences sPref;
    private SwipeRefreshLayout swiper;
    private Spinner timetabelCooseWeek;
    private Button timetableLoadWeek;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tow, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.timetable_title_name);
        swiper = (SwipeRefreshLayout) view.findViewById(R.id.timetable_swiper);
        swiper.setColorSchemeColors(getResources().getColor(R.color.colorTimetableSwiper1), getResources().getColor(R.color.colorTimetableSwiper2), getResources().getColor(R.color.colorTimetableSwiper3));
        SharedPreferences sPref = getActivity().getPreferences(MODE_PRIVATE);
        lst = (ListView) getActivity().findViewById(R.id.timetale_list_view);
        timetableprogressbar = (ProgressBar) getActivity().findViewById(R.id.timetableProgressBar);
        timetableprogressbar.setVisibility(View.INVISIBLE);
        timetabelCooseWeek = (Spinner) getActivity().findViewById(R.id.timetable_choose_week);
        timetableLoadWeek = (Button) getActivity().findViewById(R.id.timetableGetWeek);

        if (!GlobalSettings.nowWeekNumber.equals("-1")) {
            currentWeekNumber = GlobalSettings.nowWeekNumber;
        }

        lst.setClickable(true);
        //loadSpinner();

        //TimetableXml txmlALL = new TimetableXml(29549,null,getContext(),getView());
        swiper.setRefreshing(false);
        final String savedUserLogin = sPref.getString("user_login", "no data");
        Thread startActivity  = new Thread(new Runnable() {
            public void run() {
                TimetableXml txml = new TimetableXml(Integer.parseInt(savedUserLogin), null, getContext(), getView());
                if (GlobalSettings.authCookies != null) {
                    txml.setAuthCookies(GlobalSettings.authCookies);
                }
                WeeklyTimetable wt1 = null;
                if (currentWeekNumber.equals("-1")) {
                    wt1 = txml.getTimetableForNow("");

                }else {
                    wt1 = txml.getTimetableForNow(currentWeekNumber);
                }
                final WeeklyTimetable wt = wt1;
                final String readTimetableList [][] = txml.readTimetableListFromXml("timetablelist.xml");
                lst.post(new Runnable() {
                    public void run() {
                        loadSpinner(readTimetableList);
                        if (wt != null) {
                            timetableArrayForWeek = wt.getTimetableArrayForWeek();
                            if (timetableArrayForWeek != null) {
                                currentWeekNumber = wt.getWeekNumber();
                                for (int i = 0; i < SpinnerArry.length; i++) {
                                    if (SpinnerArry[i].contains(currentWeekNumber + " неделя")) {
                                        timetabelCooseWeek.setSelection(i);
                                        break;
                                    }
                                }
                                ArrayAdapter<String> arrayadapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, timetableArrayForWeek);
                                lst.setAdapter(arrayadapter);
                                timetableFid = wt.getTimetableIdsArrayForWeek();

                                // Making our notification
                                DateFormat dateFormat = new SimpleDateFormat("dd.MM");
                                Date date = new Date();
                                String notifyName = "Расписание на сегодня";
                                String notifyContent = "";
                                String date1, compare;

                                for (String s : timetableArrayForWeek) {
                                    date1 = s.substring(5, 10);
                                    compare = dateFormat.format(date).toString();
                                    if (date1.equals(compare)) {
                                        notifyContent = s.substring(11, s.length());
                                    }
                                }
                                notifyName += ", " + dateFormat.format(date);

                                NotificationCompat.Builder mBuilder =
                                        new NotificationCompat.Builder(getActivity())
                                                .setSmallIcon(R.drawable.ic_menu_timemable)
                                                .setContentTitle(notifyName)//;
                                                .setContentText("")
                                                .setStyle(new NotificationCompat.BigTextStyle().bigText(notifyContent));
                                Intent resultIntent = new Intent(getActivity(), MyCabinet.class);

                                TaskStackBuilder stackBuilder = TaskStackBuilder.create(getActivity());
                                //stackBuilder.addParentStack(MyCabinet.class);
                                stackBuilder.addNextIntent(resultIntent);
                                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                                mBuilder.setContentIntent(resultPendingIntent);
                                NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);
                                mNotificationManager.notify(555, mBuilder.build());
                            }
                        }
                        else {
                            if (SpinnerArry != null){
                                for (int i = 0; i < SpinnerArry.length; i++) {
                                    if (SpinnerArry[i].contains(currentWeekNumber + " неделя")) {
                                        timetabelCooseWeek.setSelection(i);
                                        break;
                                    }
                                }
                            }
                        }
                        swiper.setRefreshing(false);
                        lst.setClickable(true);
                    }
                });
            }
        });
        startActivity.setPriority(10);
        startActivity.start();

        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                lst.setClickable(false);
                new Thread(new Runnable() {
                    public void run() {
                        TimetableXml txml = new TimetableXml(Integer.parseInt(savedUserLogin), null, getContext(), getView());
                        if (GlobalSettings.authCookies != null) {
                            txml.setAuthCookies(GlobalSettings.authCookies);
                        }
                        txml.downloadTimetableList();
                        final WeeklyTimetable wt = txml.updateTimetableForWeek(currentWeekNumber);
                        lst.post(new Runnable() {
                            public void run() {
                                if (wt != null) {
                                    timetableArrayForWeek = wt.getTimetableArrayForWeek();
                                    if (timetableArrayForWeek != null) {
                                        ArrayAdapter<String> arrayadapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, timetableArrayForWeek);
                                        lst.setAdapter(arrayadapter);
                                    }
                                }
                                swiper.setRefreshing(false);
                                lst.setClickable(true);
                            }
                        });
                    }
                }).start();
            }
        });

        lst.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (GlobalSettings.authCookies != null) {
                    clicedList = position;
                    timetableprogressbar.setVisibility(View.VISIBLE);
                    getInfo gi = new getInfo(timetableArrayForWeek[clicedList]);
                    gi.execute();
                    lst.setClickable(false);
                }
                else {
                    //Toast.makeText(getActivity().getApplicationContext(), "Вы ещё не вошли в аккаунт", Toast.LENGTH_LONG).show();
                }
            }
        });
        timetableLoadWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swiper.setRefreshing(true);
                lst.setClickable(false);
                String selectedItem = timetabelCooseWeek.getSelectedItem().toString();
                if (!TextParser.parseTextBetween("start" + selectedItem, "start", " неделя").equals("")) {
                    currentWeekNumber = TextParser.parseTextBetween("start" + selectedItem, "start", " неделя");
                    GlobalSettings.nowWeekNumber = currentWeekNumber;

                    new Thread(new Runnable() {
                        public void run() {
                            TimetableXml txml = new TimetableXml(Integer.parseInt(savedUserLogin), null, getContext(), getView());
                            if (GlobalSettings.authCookies != null) {
                                txml.setAuthCookies(GlobalSettings.authCookies);
                            }
                            txml.downloadTimetableList();
                            final WeeklyTimetable wt = txml.getTimetableForNow(currentWeekNumber);
                            final String readTimetableList [][] = txml.readTimetableListFromXml("timetablelist.xml");
                            lst.post(new Runnable() {
                                public void run() {
                                    if (wt != null) {
                                        timetableArrayForWeek = wt.getTimetableArrayForWeek();
                                        if (timetableArrayForWeek != null) {
                                            ArrayAdapter<String> arrayadapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, timetableArrayForWeek);
                                            lst.setAdapter(arrayadapter);
                                        }
                                    }
                                    swiper.setRefreshing(false);
                                    lst.setClickable(true);
                                    loadSpinner(readTimetableList);
                                    for (int i = 0; i < SpinnerArry.length; i++) {
                                        if (SpinnerArry[i].contains(currentWeekNumber + " неделя")) {
                                            timetabelCooseWeek.setSelection(i);
                                            break;
                                        }
                                    }
                                }
                            });
                        }
                    }).start();

                }

            }
        });
/*        timetabelCooseWeek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/


    }


    public void loadSpinner(String [][] weeksList) {
        if (weeksList != null) {
            if (weeksList.length > 0) {
                String[] weeksFinal = new String[weeksList.length];
                for (int i = 0; i < weeksList.length; i++) {
                    if (weeksList[i][0].equals("1")) {
                        weeksFinal[i] = weeksList[i][1] + " неделя c " + weeksList[i][3] + " по " + weeksList[i][4];
                    } else {
                        weeksFinal[i] = weeksList[i][1] + " неделя  (пусто)";
                    }
                }
                SpinnerArry = weeksFinal;
                ArrayAdapter<String> weekCooser = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, weeksFinal);
                timetabelCooseWeek.setAdapter(weekCooser);
            }
        }
    }

    public class MyTask2 extends AsyncTask<Void, Void, Void> {
        private Document page = null;
        private String lasthtml = "";
        int inpWeekNumber = -1;
        private Map<String, String> authCookies = null;

        public MyTask2() {
            this.lasthtml = "";
            //TimetableXml weekGetter = new TimetableXml(12,authCookies, getActivity().getApplicationContext());
            //weekGetter.


        }

        public MyTask2(String lastHtml) {
            this.lasthtml = lastHtml;

        }

        public MyTask2(Map<String, String> authCookies, int weekNumber) {
            this.authCookies = authCookies;
            this.inpWeekNumber = weekNumber;

        }

        public MyTask2(Map<String, String> authCookies) {
            this.authCookies = authCookies;

        }

        @Override
        protected Void doInBackground(Void... params) {
            String weekNumber;
            String action;
            if (lasthtml.length() < 100 && authCookies != null) {
                TimetableXml txml = new TimetableXml(29549, authCookies, getContext(), getView());
                //txml.writeTimetableToXml("2",page.html());
                //WeeklyTimetable wt = txml.getTimetableForNow();
                //months = wt.getTimetableArrayForWeek();

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                super.onPostExecute(result);
                ArrayAdapter<String> arrayadapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, timetableArrayForWeek);
                lst.setAdapter(arrayadapter);
                lst.setClickable(true);
                swiper.setRefreshing(false);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class getInfo extends AsyncTask<Void, Void, Void> {
        Document teacherInfo = null, pageWithInfo = null;
        public String someResult = "";
        public String inputInfo;

        public getInfo(String someinf) {
            this.inputInfo = someinf;
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (int g = 1; g < 7; g++) {
                String timeoflesson = "";
                if (g == 1) {
                    timeoflesson = "[08:30-10:00]";
                } else if (g == 2) {
                    timeoflesson = "[10:15-11:45]";
                } else if (g == 3) {
                    timeoflesson = "[12:45-14:15]";
                } else if (g == 4) {
                    timeoflesson = "[14:30-16:00]";
                } else if (g == 5) {
                    timeoflesson = "[16:15-17:45]";
                } else if (g == 6) {
                    timeoflesson = "[18:00-19:30]";
                } else if (g == 7) {
                    timeoflesson = "[]";
                }
                if (timetableFid[clicedList][g] != null) {
                    try {

                        pageWithInfo = Jsoup.connect("http://edu.tltsu.ru/core/portal/kernel/php/getFile.php" +
                                "?PHPSESSID=o8u5hqev7f0fsagrgc048lat90" +
                                "&eval=face.coreBoxLoad%28%224%22%29%3B" +
                                "&url=http%3A//edu.tltsu.ru/edu/dialogs/w_lecture_info.php%3Fid%3D" + timetableFid[clicedList][g] + "%26usr%3Dundefined" +
                                "&el=core_window_info_4" +
                                "&0.4213821527514703" +
                                "&burl=http%3A//edu.tltsu.ru/edu/timetable.php")
                                //.cookies(GlobalSettings.authCookies)
                                .get();
                        Elements getInfoElements = pageWithInfo.select("body > p");
                        String[] timetableCellElements = new String[getInfoElements.size() - 1];
                        for (int i = 0; i < getInfoElements.size() - 1; i++) {
                            timetableCellElements[i] = getInfoElements.get(i).text().replace("\\n", "").replace("Учебный курс: ", "").replace("Аудитория: ", "").replace("Преподаватель: ", "").replace("Аудиторное з", "З");
                            if (i == 4) {
                                String teacherId = getInfoElements.get(i).select("p").html();
                                if (teacherId.length() > 50) {
                                    teacherId = teacherId.replace("\"+String.fromCharCode(34)+\"", "").substring(42, 50);
                                    teacherId = TextParser.parseTextBetween(teacherId, "(", ")");
                                    if (!teacherId.equals("")) {
                                        teacherInfo = Jsoup.connect("http://edu.tltsu.ru/contacts/dlg/user_info.php?user_id=" + teacherId)
                                                .get();
                                        Elements getTeacherInfoElements = teacherInfo.select("tbody > tr > td[style*=vertical-align: top] > h1");
                                        timetableCellElements[i] = getTeacherInfoElements.text();
                                    }
                                }
                            }
                            if (i == 0) {
                                timetableCellElements[i] = TextParser.parseTextBetween(timetableCellElements[i], "(", ")");
                            }
                        }


                        if (g == 1) {
                            //someResult = timetableCellElements[0] + timetableCellElements[3] + timetableCellElements[4]+"\n";
                            someResult = g + ". " + timetableCellElements[0] + "\n" + timeoflesson + "\n" + timetableCellElements[1] + "\n" + timetableCellElements[3] + "\n" + timetableCellElements[4] + "\n" + "end";

                        } else {
                            //someResult += timetableCellElements[0] + timetableCellElements[3] + timetableCellElements[4]+"\n";
                            someResult += g + ". " + timetableCellElements[0] + "\n" + timeoflesson + "\n" + timetableCellElements[1] + "\n" + timetableCellElements[3] + "\n" + timetableCellElements[4] + "\n" + "end";
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        someResult += g + ". " + "Данные не получены" + "end";
                    }
                } else {
                    if (TextParser.parseTextBetween(inputInfo, "\n" + g + " ", "\n" + (g + 1) + " ").substring(5).length() > 6) {
                        someResult += g + ". " + TextParser.parseTextBetween(inputInfo, "\n" + g + " ", "\n" + (g + 1) + " ").substring(5) + "\n" + timeoflesson + "end";

                    } else {
                        someResult += g + ". " + "---" + "end";
                    }
                }
            }
            GlobalSettings.savedGetInfo = someResult;
            GlobalSettings.getInfoTitle = TextParser.parseTextBetween("strt" + inputInfo, "strt", "\n1 ");
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //Toast.makeText(getActivity().getApplicationContext(),someResult ,Toast.LENGTH_LONG).show();
            if (!someResult.equals("")) {
                Intent intent = new Intent(getActivity(), TimetableGetInfo.class);
                startActivity(intent);
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "Нет подключния к интеренту", Toast.LENGTH_LONG).show();
            }
            timetableprogressbar.setVisibility(View.INVISIBLE);
        }

    }
}