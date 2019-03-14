package com.thirdmadman.tgu;
/**
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Created by third on 08.09.2017.


public class TimetableOld extends Fragment {
    //public MultiAutoCompleteTextView Textvr;
    public WebView web;
    public Button forceUpdate;
    public String htmltimetamle;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tow, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //super.onViewCreated(view, savedInstanceState);
        //super.onCreate(savedInstanceState);
        //Textvr = (MultiAutoCompleteTextView) view.findViewById(R.id.multiAutoCompleteTextView);
        //Textvr.setText(Global.authCookies.toString());
        web = (WebView) view.findViewById(R.id.webTeimetable);
        forceUpdate = (Button) view.findViewById(R.id.force_udate);
        SharedPreferences sPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String lasthtml = sPref.getString("last_html", "no data");

        if (lasthtml.length() >= 100 | Global.authCookies != null) {
            try {
                MyTask2 mt = new MyTask2();
                mt.execute();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Context context = getActivity().getApplicationContext();
            CharSequence text = "Сначала войдите в акаунт!";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
        forceUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Global.authCookies != null) {
                    try {
                        SharedPreferences sPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor ed = sPref.edit();
                        ed.putString("last_html", "no data");
                        ed.commit();
                        MyTask2 mt = new MyTask2();
                        mt.execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Context context = getActivity().getApplicationContext();
                    CharSequence text = "Сначала войдите в акаунт!";
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }
        });
    }

    public class MyTask2 extends AsyncTask<Void, Void, Void> {
        String title, ccookes;//Тут храним значение заголовка сайта
        Document page = null;
        Elements newsHeadlines = null;

        @Override
        protected Void doInBackground(Void... params) {
            SharedPreferences sPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            String lasthtml = sPref.getString("last_html", "no data");
            if (lasthtml.length() < 100) {

                try {
                    page = Jsoup.connect("http://edu.tltsu.ru/edu/timetable.php")
                            .cookies(Global.authCookies)
                            .get();
                    //Document doc = Jsoup.connect("http://en.wikipedia.org/").get();
                    //title = doc.select("#mp-itn b a").text();
                    sPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor ed = sPref.edit();
                    ed.putString("last_html", page.html().toString());
                    ed.commit();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                page = Jsoup.parse(lasthtml);
            }
            if (page != null) {
                //title = page.select("tr > td.status_box:not(#core_time_place) > p").text();
                //title = page.select("a").text();
                //title = page.select("div.weekbmcontainer > div.weekbmpage[style*=display:block] > table > tbody ").html();
                //title = page.select("div.weekbmcontainer > div.weekbmpage[style*=display:block]").html();
                Elements timetabel_td = null;
                Elements timetabel_tr = page.select("div.weekbmcontainer > div.weekbmpage[style*=display:block] > table > tbody tr");
                String[][] timatable = new String[8][timetabel_tr.size() - 1];
                for (int i = 0; i < timetabel_tr.size(); i++) {
                    timetabel_td = timetabel_tr.get(i).select("td");
                    for (int o = 0; o < timetabel_td.size(); o++) {
                        title += timetabel_td.get(o).text();
                        timatable[i][o] = timetabel_td.get(o).text();
                    }
                }
                htmltimetamle = "";
                String chashCell = "";
                for (int i = 1; i < 8; i++) {
                    htmltimetamle += "<div class=\"date\"><ul>";
                    for (int y = 0; y < 8; y++) {
                        if (y == 0) {
                            htmltimetamle += "<li class=\"title\">" + timatable[y][i] + "</li>";
                        } else {
                            if (timatable[y][i] != null) {

                                chashCell = timatable[y][i];
                                if (chashCell.equals("Выходной день")) {
                                    chashCell = "";
                                }
                            } else {
                                chashCell = "";
                            }

                            htmltimetamle += "<li><strong>" + timatable[y][0] + "</strong>" + chashCell + "</li>";
                        }

                    }
                    htmltimetamle += "<ul></div>";
                }

                //title = title.replace("style=\"width:4em\"","");
                //title = title.replace("<img src=\"/core/portal/kernel/images/spacer.gif\" style=\"width:4em;height:1px;\">","");
                title = timatable[1][0];
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                super.onPostExecute(result);
                //Textvr.setText(title.toString());
                //Textvr.setText(htmltimetamle);
                String yourhtmlpage = "<html><head>" +
                        "<style type=\"text/css\">" +
                        ".date {    max-width: 100%; border: 1px solid; border-radius: 15px; margin: 20px; padding: 20px 0px;}" +
                        " ul {list-style-type: none; padding-left: 10px; display: block; width: 300px; margin: auto;}" +
                        " li:nth-child(2n) {background-color:#f5f5f5;}" +
                        "strong {padding-right:10px} .title {font-size: 18px; padding:5px;}</style></head>" + htmltimetamle;
                web.loadDataWithBaseURL(null, yourhtmlpage, "text/html", "UTF-8", null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
*/