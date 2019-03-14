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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

import javax.microedition.khronos.opengles.GL;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by third on 10.11.2017.
 */

public class Applications extends Fragment {

    SharedPreferences sPref;
    private TextView applicationsUserName;
    private TextView applicationsResolvedTickets;
    private TextView applicationsTickets;
    private TextView applicationsNoactive;
    private TextView applicationsFindReqNumber;
    private Button applicationsMakeReqButton;
    private Button applicationsFindReq;
    private Button applicationsFindReqCabinet;
    private EditText applications_ticket_number;
    private RelativeLayout applicationsCabinetLayout;
    private RelativeLayout applicationsAnonLayout;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.applications, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.applications_title_name);
        applicationsUserName = (TextView) view.findViewById(R.id.applications_user_name);
        applicationsTickets = (TextView) view.findViewById(R.id.applications_tickets_number);
        applicationsResolvedTickets = (TextView) view.findViewById(R.id.applications_resolved_tickets_number);
        applicationsNoactive = (TextView) view.findViewById(R.id.applications_noactive);
        applicationsFindReq = (Button) view.findViewById(R.id.applicationsFindReq);
        applications_ticket_number = (EditText) view.findViewById(R.id.applications_ticket_number);
        applicationsFindReqCabinet = (Button) view.findViewById(R.id.applications_find_req);
        applicationsFindReqNumber = (EditText) view.findViewById(R.id.applications_find_req_number);
        applicationsCabinetLayout = (RelativeLayout) view.findViewById(R.id.applications_cabinet_layout);
        applicationsAnonLayout = (RelativeLayout) view.findViewById(R.id.applications_anon_layout);
/*        if (Global.authCookies == null) {
            loginLayout.setVisibility(View.VISIBLE);
            exitLayout.setVisibility(View.INVISIBLE);
        } else if (Global.authCookies != null) {
            loginLayout.setVisibility(View.INVISIBLE);
            exitLayout.setVisibility(View.VISIBLE);
            userNameText.setText(Global.userSiteName);
        }*/
        if (loadUserLogin() != null)
        {
            ApplicationsAuth auth = new ApplicationsAuth(loadUserLogin(),loadUserPassword());
            //ApplicationsAuth auth = new ApplicationsAuth("29649","ordtxvwc");
            auth.execute();
            applicationsAnonLayout.setVisibility(View.INVISIBLE);
            applicationsCabinetLayout.setVisibility(View.VISIBLE);
        }
        else {
            applicationsAnonLayout.setVisibility(View.VISIBLE);
            applicationsCabinetLayout.setVisibility(View.INVISIBLE);
        }

        applicationsFindReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!applications_ticket_number.getText().toString().equals("")) {
                    Global.supportTicketNumber = applications_ticket_number.getText().toString();
                    Intent intent = new Intent(getActivity(), ApplicationsGetInfo.class);
                    startActivity(intent);
                }
            }
        });
        applicationsFindReqCabinet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!applicationsFindReqNumber.getText().toString().equals("")) {
                    Global.supportTicketNumber = applicationsFindReqNumber.getText().toString();
                    Intent intent = new Intent(getActivity(), ApplicationsGetInfo.class);
                    startActivity(intent);
                }
            }
        });


    }


    public String loadUserLogin()
    {
        sPref = getActivity().getPreferences(MODE_PRIVATE);
        return sPref.getString("user_login", "no data");
    }

    public String loadUserPassword()
    {
        sPref = getActivity().getPreferences(MODE_PRIVATE);
        return sPref.getString("user_password", "no data");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    class ApplicationsAuth extends AsyncTask<Void, Void, Void> {
        protected Map<String, String> applicationsAuthCookies = null;
        protected String userLogin;
        protected String userPassword;
        protected String userName;
        protected String userLastName;
        protected int applicationsTicketsNumber;
        protected int applicationsResolvedTicketsNumber;
        public ApplicationsAuth(String login, String password)
        {
            this.userLogin = login;
            this.userPassword = password;
        }

        @Override
        protected Void doInBackground(Void... params) {

            Document testPage = null;

            Connection.Response authStartResponse = null;
            Connection.Response authFinalResponse = null;


            try {
                authStartResponse = Jsoup.connect("https://support.tltsu.ru/")
                        .method(Connection.Method.GET)
                        .timeout(3500)
                        .execute();

                try {
                    authFinalResponse = Jsoup.connect("https://support.tltsu.ru/login.php")
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36")
                            .data("login", userLogin)
                            .data("password", userPassword)
                            .cookies(authStartResponse.cookies())
                            .method(Connection.Method.POST)
                            .timeout(3500)
                            .execute();
                    applicationsAuthCookies = authStartResponse.cookies();
                    testPage = authFinalResponse.parse();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    if (applicationsAuthCookies != null) {
                        testPage = Jsoup.connect("https://support.tltsu.ru/")
                                .cookies(applicationsAuthCookies)
                                .timeout(3000)
                                .get();
                        String puy = "FFF" + testPage.select("div#container > div#header > p").text();
                        userName = Global.parsit(puy,"FFF"," - ");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (applicationsAuthCookies != null) {
                        Document page = Jsoup.connect("https://support.tltsu.ru/tickets.php")
                                //.data("status", "open")
                                .cookies(applicationsAuthCookies)
                                .timeout(3000)
                                .get();
                        Elements ttt = page.select("select > option");
                        applicationsResolvedTicketsNumber = 0;
                        applicationsTicketsNumber = 0;
                        if (ttt != null) {
                            if (ttt.size() >1) {
                                for (int i = 0; i < ttt.size(); i++) {
                                    if ( ttt.get(i).attr("value").equals("closed"))
                                    {
                                        applicationsResolvedTicketsNumber =+ Integer.parseInt(Global.parsit(ttt.get(i).text(), "(", ")"));
                                    } else if ( ttt.get(i).attr("value").equals("open") || ttt.get(i).attr("value").equals("assigned"))
                                    {
                                        applicationsTicketsNumber =+ Integer.parseInt(Global.parsit(ttt.get(i).text(), "(", ")"));
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if (userName != null) {
                    applicationsUserName.setText(userName);
                    applicationsResolvedTickets.setText(Integer.toString(applicationsResolvedTicketsNumber));
                    applicationsTickets.setText(Integer.toString(applicationsTicketsNumber));
                    if (applicationsTicketsNumber > 0) {
                        applicationsNoactive.setVisibility(View.INVISIBLE);
                    } else {
                        applicationsNoactive.setVisibility(View.VISIBLE);
                    }
                }
                else
                {
                    Toast.makeText(getActivity().getApplicationContext(),"Нет подключния к интеренту" ,Toast.LENGTH_LONG).show();
                }
        }
    }

}


