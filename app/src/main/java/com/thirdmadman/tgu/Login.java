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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by third on 08.09.2017.
 */

public class Login extends Fragment {
    public CheckBox save_login_data;
    public Button Button_destroy_btn;
    public Button exitButton;
    public TextView MainTitle_text;
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
        loginField = (EditText) view.findViewById(R.id.loginEditText);
        passField = (EditText) view.findViewById(R.id.passEditText);
        save_login_data = (CheckBox) view.findViewById(R.id.saveLoginCheckBox);
        loginPrgrBr = (ProgressBar) view.findViewById(R.id.loginProgressBar);
        Button_destroy_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String login = loginField.getText().toString();
                String pass = passField.getText().toString();
                if (pass.length() >= 4 && login.length() >= 1) {
                    if (GlobalSettings.authCookies == null) {
                        authUser(login, pass);
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

    void saveText(String userLogin, String password) {
        sPref = getActivity().getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString("user_login", userLogin);
        ed.putString("user_password", password);
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

    void authUser(String userLogin, String password) {
        loginPrgrBr.setVisibility(View.VISIBLE);
        Button_destroy_btn.setClickable(false);
        DoAuthTask doAuth = new DoAuthTask();
        doAuth.execute(userLogin, password);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    class GetAuthAndUserName implements Callable<HashMap<String, Object>> {

        private String userLogin = null;
        private String password = null;


        public void setUserLoginAndPassword(String userLogin, String password) {
            this.userLogin = userLogin;
            this.password = password;
        }

        public HashMap<String, Object> call() throws Exception {

            HashMap<String, Object> req = new HashMap<>();
            try {
                Connection.Response loginFormget = Jsoup.connect("http://edu.tltsu.ru/")
                        .method(Connection.Method.GET)
                        .timeout(5000)
                        .execute();
                //Document loginFragmenPage = loginFormget.parse();
                Connection.Response loginForm = Jsoup.connect("https://edu.tltsu.ru/doAutho.php")
                        .method(Connection.Method.POST)
                        .data("core_log", userLogin)
                        .data("core_pas", password)
                        .data("burl", "http://edu.tltsu.ru/")
                        .cookies(loginFormget.cookies())
                        .execute();
                //Document gg = loginFormget.parse();
                Map<String, String> authCookies = loginForm.cookies();
                //page = Jsoup.connect("http://edu.tltsu.ru/edu/timetable.php")
                if (authCookies != null && authCookies.size() >= 1) {
                    Document page = Jsoup.connect("http://edu.tltsu.ru/index.php")
                            .cookies(authCookies)
                            .timeout(5000)
                            .get();
                    String userName = page.select("tr > td.status_box:not(#core_time_place) > p").text();
                    if (userName != null && userName.length() > 0) {
                        req.put("cooke", authCookies);
                        req.put("username", userName);
                        return req;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return req;
        }
    }

    private class DoAuthTask extends AsyncTask<String, Void, HashMap<String, Object>> {
        @Override
        protected HashMap<String, Object> doInBackground(String... string) {
            try {
                String userLogin = string[0];
                String password = string[1];
                for (int i = 0; i < 5; i++) {
                    GetAuthAndUserName getAuthAndUserName = new GetAuthAndUserName();
                    getAuthAndUserName.setUserLoginAndPassword(userLogin, password);
                    HashMap<String, Object> req = null;
                    FutureTask<HashMap<String, Object>> future = new FutureTask<>(getAuthAndUserName);
                    Thread thread = new Thread(future);
                    thread.start();
                    req = future.get();
                    if (req != null && req.size() == 2 && req.get("username") != null && req.get("cooke") != null) {
                        String userName = req.get("username").toString();
                        if (!userName.equals("")) {
                            if (!userName.contains("Гость")) {
                                Map<String, String> cooke = (Map<String, String>) req.get("cooke");
                                saveText(userLogin, password);
                                GlobalSettings.authCookies = cooke;
                                GlobalSettings.userSiteName = userName;
                                sPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                                SharedPreferences.Editor ed = sPref.edit();
                                ed.putString("user_name", userName);
                                ed.apply();
                                return req;
                            } else {
                                return req;
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(HashMap<String, Object> result) {
            if (result != null) {
                String userName = result.get("username").toString();
                if (userName != null &&  !userName.contains("Гость")) {
                    sPref = getActivity().getPreferences(MODE_PRIVATE);
                    String savedUserLogin = sPref.getString("user_login", "no data");
                    if (savedUserLogin.equals("29549")) {
                        loginCongratulationsText.setText(GlobalSettings.loginCongratulations[(new Random()).nextInt(GlobalSettings.loginCongratulations.length)]);
                    } else if (savedUserLogin.equals("29649")) {
                        loginCongratulationsText.setText(GlobalSettings.loginCongratulations[(new Random()).nextInt(GlobalSettings.loginCongratulations.length)]);
                    }
                    userNameText.setText(userName);
                    loginLayout.setVisibility(View.INVISIBLE);
                    exitLayout.setVisibility(View.VISIBLE);
                } else {
                    Snackbar.make(getView(), "Введены неверные данные", Snackbar.LENGTH_LONG).show();
                }
            } else {
                Snackbar.make(getView(), "Повторите попытку через некоторе вермя", Snackbar.LENGTH_LONG).show();
            }
            loginPrgrBr.setVisibility(View.INVISIBLE);
            Button_destroy_btn.setClickable(true);
        }
    }
}


