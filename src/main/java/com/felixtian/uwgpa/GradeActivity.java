package com.felixtian.uwgpa;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GradeActivity extends AppCompatActivity {
    ArrayList<String> gradeItems;
    private String html;
    ArrayAdapter gradeAdapter;


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
        gradeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, gradeItems);
        ListView gradeListView = (ListView) findViewById(R.id.gradeList);
        gradeListView.setAdapter(gradeAdapter);
        gradeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                AlertDialog.Builder aDB=new AlertDialog.Builder(GradeActivity.this);
                aDB.setMessage("Delete the item?");
                final int positionToRemove = position;
                aDB.setNegativeButton("Cancel", null);
                aDB.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        gradeItems.remove(positionToRemove);
                        gradeAdapter.notifyDataSetChanged();
                    }});
                aDB.show();
            }
        });

        FloatingActionButton fab1 = (FloatingActionButton) findViewById(R.id.addGrade);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder aDB = new AlertDialog.Builder(GradeActivity.this);
                LayoutInflater inflater = GradeActivity.this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.new_grade, null);
                aDB.setView(dialogView);
                final EditText subjectEdt = (EditText) dialogView.findViewById(R.id.subject);
                final EditText gradeEdt = (EditText) dialogView.findViewById(R.id.grade);
                aDB.setTitle("New Grade");
                //aDB.setMessage("Enter subject and grade below");
                aDB.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        StringBuilder sb=new StringBuilder(subjectEdt.getText().toString());
                        String grade = gradeEdt.getText().toString();
                        sb.append(": ");
                        sb.append(grade);
                        sb.append("\t\t");
                        sb.append(GPAConvert.convert(grade));
                        gradeItems.add(0,sb.toString());
                        gradeAdapter.notifyDataSetChanged();
                    }
                });
                aDB.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
                aDB.show();
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

    public void filter(View v){
        AlertDialog.Builder aDB = new AlertDialog.Builder(GradeActivity.this);
        aDB.setTitle("Filter");
        aDB.setMessage("This will delete courses without a proper course code.");
        aDB.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //operate on the list... this is not good.
                String pattern = "^.* [0-9]{3}:";
                Pattern r = Pattern.compile(pattern);
                ArrayList<String> newGradeItems=new ArrayList<String>();
                for (String gradeItem: gradeItems){ //difficult to do this in-place
                    //Log.d("regex","trying "+gradeItem);
                    if (r.matcher(gradeItem).find()){
                        newGradeItems.add(gradeItem);
                        //Log.d("regex","matched "+gradeItem);
                    }
                }
                gradeItems.clear();
                for (String gradeItem: newGradeItems){
                    gradeItems.add(gradeItem);
                }
                gradeAdapter.notifyDataSetChanged();
            }
        });
        aDB.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        aDB.show();
    }

}
