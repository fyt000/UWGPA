package com.felixtian.uwgpa;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class GradeActivity extends AppCompatActivity {
    ArrayList<String> gradeItems;
    private String html;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            gradeItems=extras.getStringArrayList("grades");
            //gradeParsing();
            Log.d("oncreate","grade parsed");
            if (gradeItems==null){
                Log.d("oncreate","go do grocery and eat lunch please");
            }
        }
        else{
            Log.d("oncreate","null html at grade");
        }
        final ArrayAdapter gradeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, gradeItems);
        ListView gradeListView = (ListView) findViewById(R.id.gradeList);
        gradeListView.setAdapter(gradeAdapter);
        gradeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                AlertDialog.Builder adb=new AlertDialog.Builder(GradeActivity.this);
                adb.setMessage("Delete the item?");
                final int positionToRemove = position;
                adb.setNegativeButton("Cancel", null);
                adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        gradeItems.remove(positionToRemove);
                        gradeAdapter.notifyDataSetChanged();
                    }});
                adb.show();
            }
        });

        FloatingActionButton fab1 = (FloatingActionButton) findViewById(R.id.addGrade);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.calcAvg);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double total=0;
                int items=0;
                for (String grade : gradeItems){
                    try {
                        double gpa = Double.parseDouble(grade.substring(grade.indexOf("\t\t")));
                        total += gpa;
                        items++;
                    }
                    catch (Exception e){
                        //ignore unparseable items
                    }
                }
                double avg = total/items;
                Snackbar.make(view, "Your average gpa "+avg, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}
