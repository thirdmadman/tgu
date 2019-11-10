package com.thirdmadman.tgu.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.thirdmadman.tgu.R;
import com.thirdmadman.tgu.GlobalSettings;

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

        if (!GlobalSettings.savedGetInfo.equals("")) {
            String[] names = GlobalSettings.savedGetInfo.split("end");
            setTitle(GlobalSettings.getInfoTitle);
            lst = (ListView) this.findViewById(R.id.timetableGetInfoListView);
            ArrayAdapter<String> arrayadapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);
            lst.setAdapter(arrayadapter);
        }
    }




}