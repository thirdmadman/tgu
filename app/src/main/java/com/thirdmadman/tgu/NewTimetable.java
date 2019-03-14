/*package com.thirdmadman.tgu;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
public class NewTimetable extends AppCompatActivity {
    ListView lst;
    String[] months={"Понедельник\n1 8:30 Военная подготовка\n2 8:30 Военная подготовка\n3 8:30 Военная подготовка\n4 8:30 Военная подготовка\n5 8:30 Военная подготовка","Feb","March","April","May","June","July","August","September","Octomber","November","December"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_timetable);
        lst= (ListView) findViewById(R.id.timetableListView);
        ArrayAdapter<String> arrayadapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,months);
        lst.setAdapter(arrayadapter);
        lst.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv= (TextView) view;
                Toast.makeText(NewTimetable.this,tv.getText()+"  "+position,Toast.LENGTH_LONG).show();
            }
        });
    }
}
*/