package com.thirdmadman.tgu;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Created by third on 08.09.2017.
 */

public class TimetableGetInfo extends AppCompatActivity{

    ListView lst;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_info);

        if (!Global.savedGetInfo.equals("")) {
            String[] names = Global.savedGetInfo.split("end");
            setTitle(Global.getInfoTitle);
            lst = (ListView) this.findViewById(R.id.timetableGetInfoListView);
            ArrayAdapter<String> arrayadapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);
            lst.setAdapter(arrayadapter);
        }
    }




}