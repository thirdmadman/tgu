package com.thirdmadman.tgu;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Map;


public class MyCabinet extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static int SPLASH_TIME_OUT = 2000;
    SharedPreferences sPref;
    FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_cabinet);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
/*                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                Intent intent = new Intent(view.getContext(), ApplicationsNewReq.class);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (Global.firstLaunch == true) {
            dispalyFragment(R.layout.welcome_screen);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sPref = getPreferences(MODE_PRIVATE);
                    String savedUserLogin = sPref.getString("user_login", "no data");
                    if (!savedUserLogin.equals("no data"))
                    {
                        dispalyFragment(R.layout.fragment_tow);
                    }
                    else
                    {
                        dispalyFragment(R.layout.loginpage);
                    }
                    Global.firstLaunch = false;
                }
            }, SPLASH_TIME_OUT);
        }
        else {
            sPref = getPreferences(MODE_PRIVATE);
            String savedUserLogin = sPref.getString("user_login", "no data");
            if (!savedUserLogin.equals("no data"))
            {
                dispalyFragment(R.layout.fragment_tow);
            }
            else
            {
                dispalyFragment(R.layout.loginpage);
            }

        }

    }

    public  void updateNavHeader()
    {
        sPref = getPreferences(MODE_PRIVATE);
        String savedUserLogin = sPref.getString("user_login", "no data");
        String savedUserName = sPref.getString("user_name", "no data");

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View hView =  navigationView.getHeaderView(0);
        TextView nav_username = (TextView)hView.findViewById(R.id.navHeaderTitlename);
        TextView nav_userlogin = (TextView)hView.findViewById(R.id.navHeaderEmail);
        if (!savedUserLogin.equals("no data") &  !savedUserName.equals("no data")) {
            nav_username.setText(savedUserName);
            nav_userlogin.setText(savedUserLogin);
        }
        else
        {
            nav_username.setText("Unknown user");
            nav_userlogin.setText("Unknown id");
        }
        return;
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.my_cabinet, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void dispalyFragment(int id) {
        fab.setVisibility(View.INVISIBLE);
        Fragment fragment = null;
        switch (id)
        {
            case R.layout.loginpage:
                fragment= new Login();
                break;
            case R.layout.fragment_tow:
                fragment= new Timetable();
                break;
            case R.layout.welcome_screen:
                fragment = new WelcomeScreen();
                break;
            case R.layout.terms_of_service:
                fragment = new TermsOfService();
                break;
            case R.layout.settings:
                fragment = new Settings();
                break;
            case R.layout.cource_selection:
                fragment = new CourseSelection();
                break;
            case R.layout.applications:
                fragment = new Applications();
                fab.setVisibility(View.VISIBLE);
                break;
        }
        if (fragment != null)
        {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.MyCabinetFrame,fragment);
            ft.commit();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        updateNavHeader();
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_login) {
            //Intent myIntetnt = new Intent(MyCabinet.this, Login.class);
            //startActivity(myIntetnt);
            dispalyFragment(R.layout.loginpage);
        } else if (id == R.id.nav_show_timtable) {
            //Intent myIntetnt = new Intent(MyCabinet.this, Timetable.class);
            //startActivity(myIntetnt);
            dispalyFragment(R.layout.fragment_tow);
        } else if (id == R.id.nav_terms) {
            dispalyFragment(R.layout.terms_of_service);
        }
        else if (id == R.id.nav_settings) {
            dispalyFragment(R.layout.settings);
        }
        else if (id == R.id.nav_courses) {
            dispalyFragment(R.layout.cource_selection);
        }
        else if (id == R.id.nav_applications) {
            dispalyFragment(R.layout.applications);
        }

        return true;
    }
}
