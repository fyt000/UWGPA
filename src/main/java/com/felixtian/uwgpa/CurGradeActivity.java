package com.felixtian.uwgpa;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

public class CurGradeActivity extends AppCompatActivity {
    ArrayAdapter gradeAdapter;
    private Switch notificationSwitch;
    //private TextView gradeView;
    ArrayList<GradeItem> gradeList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cur_grade);
        Bundle extras = getIntent().getExtras();
        //gradeView = (TextView)findViewById(R.id.gradeTextView);
        if (extras != null) {
            gradeList=extras.getParcelableArrayList("grades");

            gradeAdapter = new GradeAdapter(this,gradeList);
            ListView gradeListView = (ListView) findViewById(R.id.curGradeList);
            gradeListView.setAdapter(gradeAdapter);

            //gradeView.setText(sb.toString());
        }
        notificationSwitch = (Switch) findViewById(R.id.notificationSwitch);
        SharedPreferences sharedPref = getSharedPreferences(GradeNotificationService.GradePrefName, MODE_PRIVATE);
        int running=sharedPref.getInt("running",0);
        if (running==1)
            notificationSwitch.setChecked(true);
        else
            notificationSwitch.setChecked(false);
        Log.d("notificationswitch",running+"");


        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    AlertDialog.Builder aDB=new AlertDialog.Builder(CurGradeActivity.this);
                    aDB.setMessage( getResources().getString(R.string.notification_disclaimer));
                    aDB.setNegativeButton("Cancel", new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            notificationSwitch.setChecked(false);
                        }});
                    aDB.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            pollGrades();
                        }});
                    aDB.show();

                }
                else
                    cancelPoll();
            }
        });
        Toolbar toolbar = (Toolbar) findViewById(R.id.curGradeToolbar);
        setSupportActionBar(toolbar);
    }
    public void pollGrades(){
        SharedPreferences sharedPref = getSharedPreferences(GradeNotificationService.GradePrefName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        int running=sharedPref.getInt("running",0);
        Log.d("notificationswitch","pollgrades "+running);
        if (running==1)
            GradeNotificationReceiver.cancel(this);
        else {
            Log.d("notificationswitch","putting 1");
            editor.putInt("running", 1);
        }
        editor.commit();
        //poll grades on 30min interval right away, will add a new activity for configs.
        GradeNotificationReceiver.initialize(this,1);
    }
    public void cancelPoll() {
        SharedPreferences sharedPref = getSharedPreferences(GradeNotificationService.GradePrefName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("running",0);
        Log.d("notificationswitch","putting 0");
        editor.commit();
        GradeNotificationReceiver.cancel(this);
    }

}
