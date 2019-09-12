package com.thirdmadman.tgu;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by third on 08.09.2017.
 */

public class Login extends Fragment {
    public CheckBox save_login_data;
    public Button Button_destroy_btn;
    public Button exitButton;
    public TextView MainTitle_text;
    //    public EditText consoleText;
    public EditText loginField;
    public EditText passField;
    public RelativeLayout loginLayout;
    public RelativeLayout exitLayout;
    public TextView userNameText;
    public String pass, login, user_name;
    public Map<String, String> authCookies = null;
    public ProgressBar loginPrgrBr;
    public TextView loginCongratulationsText;

    SharedPreferences sPref;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.loginpage, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button_destroy_btn = (Button) view.findViewById(R.id.destroyBtn);
        exitButton = (Button) view.findViewById(R.id.exitButton);
        MainTitle_text = (TextView) view.findViewById(R.id.MainTitle);
        loginLayout = (RelativeLayout) view.findViewById(R.id.loginLayout);
        exitLayout = (RelativeLayout) view.findViewById(R.id.exitLayout);
        userNameText = (TextView) view.findViewById(R.id.loginUserName);
        loginCongratulationsText = (TextView) view.findViewById(R.id.loginCongratulationsText);
        getActivity().setTitle(R.string.login_title_name);
/*
        try {
            String someinf = homePage();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        loginField = (EditText) view.findViewById(R.id.loginEditText);
        passField = (EditText) view.findViewById(R.id.passEditText);
        save_login_data = (CheckBox) view.findViewById(R.id.saveLoginCheckBox);
        loginPrgrBr = (ProgressBar) view.findViewById(R.id.loginProgressBar);
//        consoleText = (EditText) view.findViewById(R.id.console);
        Button_destroy_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pass = passField.getText().toString();
                login = loginField.getText().toString();
                if (pass.length() > 4 & login.length() >= 4) {

                    if (GlobalSettings.authCookies == null) {
                        MyTask mt = new MyTask();
                        mt.execute();
                        loginPrgrBr.setVisibility(View.VISIBLE);
                        Button_destroy_btn.setClickable(false);
                    } else {
                        Context context = getActivity().getApplicationContext();

                        CharSequence text = "Вы уже вошли в аккаунт";
                        int duration = Toast.LENGTH_LONG;
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                }
            }

        });
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GlobalSettings.authCookies = null;
                exitLayout.setVisibility(View.INVISIBLE);
                loginLayout.setVisibility(View.VISIBLE);
            }
        });
        loadText();
        if (GlobalSettings.authCookies == null) {
            loginLayout.setVisibility(View.VISIBLE);
            exitLayout.setVisibility(View.INVISIBLE);
        } else if (GlobalSettings.authCookies != null) {
            loginLayout.setVisibility(View.INVISIBLE);
            exitLayout.setVisibility(View.VISIBLE);
            userNameText.setText(GlobalSettings.userSiteName);
        }


    }

    void saveText() {
        sPref = getActivity().getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString("user_login", loginField.getText().toString());
        ed.putString("user_password", passField.getText().toString());
        ed.apply();
    }

    void loadText() {
        sPref = getActivity().getPreferences(MODE_PRIVATE);
        String savedUserLogin = sPref.getString("user_login", "no data");
        String savedUserPassword = sPref.getString("user_password", "no data");
        if (!savedUserLogin.equals("no data")) {
            loginField.setText(savedUserLogin);
            passField.setText(savedUserPassword);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    class MyTask extends AsyncTask<Void, Void, Void> {
        protected Map<String, String> authCookies = null;
        String title;
        boolean needToUpdate = false;

        @Override
        protected Void doInBackground(Void... params) {

            Document page = null;

            Connection.Response loginFormget = null;
            Connection.Response loginForm = null;
            Document InboxJson = null;


            try {
                loginFormget = Jsoup.connect("http://edu.tltsu.ru/")
                        .method(Connection.Method.GET)
                        .timeout(3500)
                        .execute();
                Document loginFragmenPage = loginFormget.parse();
                // WARNING, VERY IMPORTATN
                //TODO: rewrite code of check if its upd available
                //wtf did I just wrote up here
/*                try {
                    Connection.Response resp = HttpConnection.connect("https://jsonblob.com/api/jsonBlob/4515815d-a8df-11e7-9d0a-2db3ffdad627")
                            .ignoreContentType(true)
                            .timeout(3000)
                            .execute();
                    String body = resp.body();
                    try {
                        JSONObject jsonObject = new JSONObject(body);
                        if (jsonObject.getBoolean("resoonse")) {
                            if (jsonObject.getDouble("lastversion") > GlobalSettings.CurrentVersion) {
                                needToUpdate = true;
                            }
                        }
                    } catch (JSONException e) {
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                try {
                    loginForm = Jsoup.connect("https://edu.tltsu.ru/doAutho.php")
                            .method(Connection.Method.POST)
                            .data("core_log", login)
                            .data("core_pas", pass)
                            .data("burl", "http://edu.tltsu.ru/")
                            .cookies(loginFormget.cookies())
                            .execute();
                    authCookies = loginForm.cookies();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    //page = Jsoup.connect("http://edu.tltsu.ru/edu/timetable.php")
                    if (authCookies != null) {
                        page = Jsoup.connect("http://edu.tltsu.ru/index.php")
                                .cookies(authCookies)
                                .timeout(3000)
                                .get();

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    title = page.select("tr > td.status_box:not(#core_time_place) > p").text();
                    if (!title.equals("")) {
                        user_name = title;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            loginPrgrBr.setVisibility(View.INVISIBLE);
            try {
                super.onPostExecute(result);
                if (user_name != null) {
                    if (!user_name.equals("")) {
                        if (!user_name.contains("Гость")) {
                            saveText();
                            GlobalSettings.authCookies = authCookies;
                            Context context = getActivity().getApplicationContext();
                            //CharSequence text = "Вы успешно вошли!";
                            int duration = Toast.LENGTH_SHORT;
                            //Toast toast = Toast.makeText(context, text, duration);
                            //toast.show();
                            if (needToUpdate) {
                                Toast toast2 = Toast.makeText(context, "Необходимо обновление!", duration);
                                toast2.show();
                            }
                            GlobalSettings.userSiteName = user_name;
                            userNameText.setText(user_name);
                            //userNameText.setText("Иванов И.И.");
                            sPref = getActivity().getPreferences(MODE_PRIVATE);
                            String savedUserLogin = sPref.getString("user_login", "no data");
                            if (savedUserLogin.equals("29549")) {
                                loginCongratulationsText.setText(GlobalSettings.loginCongratulations[(new Random()).nextInt(GlobalSettings.loginCongratulations.length)]);
                            } else if (savedUserLogin.equals("29649")) {
                                loginCongratulationsText.setText(GlobalSettings.loginCongratulations[(new Random()).nextInt(GlobalSettings.loginCongratulations.length)]);
                            }
                            loginLayout.setVisibility(View.INVISIBLE);
                            exitLayout.setVisibility(View.VISIBLE);
                            sPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                            SharedPreferences.Editor ed = sPref.edit();
                            ed.putString("user_name", user_name);
                            ed.commit();
                        } else {
                           /* Toast toast3 = Toast.makeText(getActivity().getApplicationContext(), "Введены неверные данные", Toast.LENGTH_SHORT);
                            toast3.show();*/
                            Snackbar.make(getView(), "Введены неверные данные", Snackbar.LENGTH_LONG).show();
                        }
                    } else {
                        Toast toast3 = Toast.makeText(getActivity().getApplicationContext(), "Попробуйте ещё раз - сервер отправил пустой ответ", Toast.LENGTH_SHORT);
                        toast3.show();
                    }
                } else {
/*                    Toast toast3 = Toast.makeText(getActivity().getApplicationContext(), "Нет связи с ервером", Toast.LENGTH_SHORT);
                    toast3.show();*/
                    Snackbar.make(getView(), "Нет связи с сервером", Snackbar.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Button_destroy_btn.setClickable(true);
        }
    }

}


