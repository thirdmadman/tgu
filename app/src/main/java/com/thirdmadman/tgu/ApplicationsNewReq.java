package com.thirdmadman.tgu;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Map;

/**
 * Created by third on 10.11.2017.
 */

public class ApplicationsNewReq extends AppCompatActivity {

    private ProgressBar applicationGetInfoProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.applications_new_req);

        setTitle(R.string.applications_title_name);

        applicationGetInfoProgressBar = (ProgressBar)findViewById(R.id.application_get_info_progressBar);
/*        if (Global.authCookies == null) {
            loginLayout.setVisibility(View.VISIBLE);
            exitLayout.setVisibility(View.INVISIBLE);
        } else if (Global.authCookies != null) {
            loginLayout.setVisibility(View.INVISIBLE);
            exitLayout.setVisibility(View.VISIBLE);
            userNameText.setText(Global.userSiteName);
        }*/
if (Global.supportTicketNumber != null) {
    ApplicationsGetInfoReq getInfo = new ApplicationsGetInfoReq(Global.supportTicketNumber);
    getInfo.execute();
    applicationGetInfoProgressBar.setVisibility(View.VISIBLE);
}

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    class ApplicationsGetInfoReq extends AsyncTask<Void, Void, Void> {
        protected Map<String, String> applicationsAuthCookies = null;
        protected String ticketId;
        protected String req_number;
        protected String req_date;
        protected String status;
        protected String department;
        protected String executor;
        protected String desc;
        protected String solution;

        public ApplicationsGetInfoReq(Map<String, String> cookies, String ticketId) {
            this.applicationsAuthCookies = cookies;
            this.ticketId = ticketId;
        }

        public ApplicationsGetInfoReq(String ticketId) {
            this.ticketId = ticketId;
        }

        @Override
        protected Void doInBackground(Void... params) {

            Document ticketPage = null;
            Document ticketPageDesc = null;

            Connection.Response ticketResponse = null;
            Connection.Response ticketResponseDesc = null;

            try {
                if (applicationsAuthCookies != null) {
                    ticketResponse = Jsoup.connect("https://support.tltsu.ru/myticket.php?id=" + ticketId)
                            .cookies(Global.supportAuthCookies)
                            .method(Connection.Method.GET)
                            .timeout(3500)
                            .execute();
                }
                else
                {
                    ticketResponse = Jsoup.connect("https://support.tltsu.ru/myticket.php?id=" + ticketId)
                            .method(Connection.Method.GET)
                            .timeout(3500)
                            .execute();
                }
                ticketResponseDesc = Jsoup.connect("https://support.tltsu.ru/view.php?id=" + ticketId)
                        .method(Connection.Method.GET)
                        .timeout(3500)
                        .execute();
                ticketPageDesc = ticketResponseDesc.parse();
                ticketPage = ticketResponse.parse();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                    Elements ttt = ticketPage.select("table#ticketInfo > tbody > tr  table.infoTable");
                    Elements yyy = ttt.get(0).select("table > tbody > tr");
                    Elements uuu = ttt.get(1).select("table > tbody > tr");
                    String s = ttt.get(1).html();
                    req_number = ticketPage.select("table#ticketInfo > tbody > tr > td > h1").text();
                    req_number = Global.parsit(req_number+"<end>","№","<end>");
                    status = yyy.select("tr > td").get(0).text();
                    req_date=yyy.select("tr > td").get(1).text();
                    department = uuu.select("tr > td").get(0).text();
                    executor =uuu.select("tr > td").get(1).text();


                    String parseFrom = "<h2 style=\"margin-bottom:10px;\">Описание:</h2>";
                    String parseTo = "<p style=\"padding-left:45%\">";
                    String parseTo2 = "<h2 style=\"margin-bottom:10px;\"><br>Описание решения:</h2>";
                    Elements iii = ticketPageDesc.select("div#content");
                    String parseWhat = iii.html();
                    if (!Global.parsit(parseWhat,parseFrom,parseTo2).equals("")) {
                        Document ooo = Jsoup.parse(Global.parsit(parseWhat, parseFrom, parseTo2));
                        desc = ooo.text();
                        ooo = Jsoup.parse(Global.parsit(parseWhat, parseTo2, parseTo));
                        solution=ooo.text();
                    }
                    else
                    {
                        Document ooo = Jsoup.parse(Global.parsit(parseWhat, parseFrom, parseTo));
                        desc = ooo.text();
                    }


            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (req_number != null) {

                if (solution != null) {

                }
            }
            else {
                Toast.makeText(getApplicationContext(),"Нет подключния к интеренту" ,Toast.LENGTH_LONG).show();
            }
            applicationGetInfoProgressBar.setVisibility(View.INVISIBLE);
        }
    }

}


