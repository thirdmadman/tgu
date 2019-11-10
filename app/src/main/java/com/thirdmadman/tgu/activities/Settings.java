package com.thirdmadman.tgu.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.thirdmadman.tgu.GlobalSettings;
import com.thirdmadman.tgu.R;

import java.io.File;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by third on 08.09.2017.
 */

public class Settings extends Fragment {

    public Button Button_clear_cache;
    public Button Button_clear_data;

    SharedPreferences sPref;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button_clear_cache = (Button) view.findViewById(R.id.buttonClearCache);
        Button_clear_data = (Button) view.findViewById(R.id.buttonClearData);
        getActivity().setTitle(R.string.settings_title_name);
        //getActivity().getSupportActionBar().setTitle(title);
        Button_clear_cache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor ed = sPref.edit();
                SharedPreferences sPrefRead = getActivity().getPreferences(MODE_PRIVATE);
                String userLogin = sPrefRead.getString("user_login", "no data");

                File timetablelistFile = new File(getContext().getFilesDir().getPath() + "/" + userLogin + "/"+ "timetablelist.xml");
                if (timetablelistFile.exists()){
                    if(timetablelistFile.delete()){
                        Toast toast3 = Toast.makeText(getActivity().getApplicationContext(), "Кэш расписания отчистен", Toast.LENGTH_SHORT);
                        toast3.show();
                    }
                }
                else {
                    Toast toast3 = Toast.makeText(getActivity().getApplicationContext(), "Кэш пуст. Нечего удалять", Toast.LENGTH_SHORT);
                    toast3.show();
                }


            }
        });
        Button_clear_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor ed = sPref.edit();
                ed.putString("user_name", null);
                ed.putString("user_login", null);
                ed.putString("user_password", null);
                ed.putString("last_html", null);
                GlobalSettings.authCookies = null;
                ed.apply();
                Toast toast3 = Toast.makeText(getActivity().getApplicationContext(), "Все данные удалены", Toast.LENGTH_SHORT);
                toast3.show();
            }
        });
    }


}
