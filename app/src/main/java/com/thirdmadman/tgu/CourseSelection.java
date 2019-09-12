package com.thirdmadman.tgu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Created by third on 08.09.2017.
 */

public class CourseSelection extends Fragment {
    protected ListView coursesList;
    protected ProgressBar courceSelProgrbar;
    protected int clicedList = -1;
    protected String[] corsesList;
    private String[][] courseIds;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cource_selection, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        super.onCreate(savedInstanceState);

        coursesList = (ListView) getActivity().findViewById(R.id.courseSelectionListView);
        courceSelProgrbar = (ProgressBar) getActivity().findViewById(R.id.courseSelectionProgressBar);
        final GetCourseSelection getSelection = new GetCourseSelection();
        if (GlobalSettings.authCookies != null) {
            try {

                getSelection.execute();
                courceSelProgrbar.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Сначала войдите в акаунт!", Toast.LENGTH_LONG);
            toast.show();
        }


        coursesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TextView tv= (TextView) view;
                //Toast.makeText(getActivity().getApplicationContext(),"Тут подробно " + position ,Toast.LENGTH_LONG).show();
                //clicedList = position;
                //if (GlobalSettings.authCookies != null)
                //{
                    courceSelProgrbar.setVisibility(View.VISIBLE);
                    GetCourseMarks getMarks = new GetCourseMarks(Integer.parseInt(courseIds[position][1]));
                    getMarks.execute();
                //}
            }
        });
    }



    public class GetCourseSelection extends AsyncTask<Void, Void, Void> {
       private Document page = null;


        @Override
        protected Void doInBackground(Void... params) {
            SharedPreferences sPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            String assignments_html = sPref.getString("assignments_html", "no data");
            if (assignments_html.length() < 100) {

                try {
                    page = Jsoup.connect("http://edu.tltsu.ru/edu/assignments.php?semester=5849") // ?semester=5849 - костыль TODO: создать алгорит выбора семестора
                            .cookies(GlobalSettings.authCookies)
                            .get();
                    sPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor ed = sPref.edit();
                    ed.putString("assignments_html", page.html().toString());
                    ed.apply();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                page = Jsoup.parse(assignments_html);
            }
            if (page != null) {

                Elements courses_listOptions = page.select("select#courses_list > option");
                courseIds = new String[courses_listOptions.size()-2][2];
                corsesList = new String[courses_listOptions.size()-2];
                for (int i = 1; i < courses_listOptions.size()-1; i++) {
                    courseIds[i-1][1] = courses_listOptions.get(i).attr("value").toString();
                    courseIds[i-1][0] = courses_listOptions.get(i).text();
                    corsesList[i-1] = courses_listOptions.get(i).text();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                super.onPostExecute(result);
               ArrayAdapter<String> arrayadapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,corsesList);
               coursesList.setAdapter(arrayadapter);
               courceSelProgrbar.setVisibility(View.INVISIBLE);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class GetCourseMarks extends AsyncTask<Void, Void, Void> {
        protected String someResult = "";
        protected int courseId;
        private Document page = null;

        public GetCourseMarks(int courseId) {
            this.courseId = courseId;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try
            {
                Connection.Response response =  Jsoup.connect("http://edu.tltsu.ru/edu/assignments.php?course_id=" + courseId) // ?semester=5849 - костыль TODO: создать алгорит выбора семестора
                        .ignoreContentType(true)
                        .cookies(GlobalSettings.authCookies)
                        .userAgent("\"User-Agent\", \"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11\"")
                        .referrer("http://edu.tltsu.ru/edu/assignments.php")
                        .execute();
                page = response.parse();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            if (page != null) {
                Elements marksOfUser_children = page.select("td:has(alert)");
                Elements marksOfUser = marksOfUser_children.parents();
                marksOfUser = marksOfUser.select("td:has(achtung)");
                /*
                courseIds = new String[courses_listOptions.size()-2][2];
                corsesList = new String[courses_listOptions.size()-2];
                for (int i = 1; i < courses_listOptions.size()-1; i++) {
                    courseIds[i-1][1] = courses_listOptions.get(i).attr("value").toString();
                    courseIds[i-1][0] = courses_listOptions.get(i).text();
                    corsesList[i-1] = courses_listOptions.get(i).text();
                }
                */
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            if(!someResult.equals("")) {
                Intent intent = new Intent(getActivity(), TimetableGetInfo.class);
                courceSelProgrbar.setVisibility(View.INVISIBLE);
                //startActivity(intent);
            }
            else {
                Toast.makeText(getActivity().getApplicationContext(),"Нет подключния к интеренту" ,Toast.LENGTH_LONG).show();
                courceSelProgrbar.setVisibility(View.INVISIBLE);
            }
        }

    }

}