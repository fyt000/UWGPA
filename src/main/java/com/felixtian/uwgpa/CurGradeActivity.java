package com.felixtian.uwgpa;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class CurGradeActivity extends AppCompatActivity {

    private TextView gradeView;
    ArrayList<GradeItem> gradeList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cur_grade);
        Bundle extras = getIntent().getExtras();
        gradeView = (TextView)findViewById(R.id.gradeTextView);
        if (extras != null) {
            gradeList=extras.getParcelableArrayList("grades");
            StringBuilder sb = new StringBuilder("Grades:\n");
            for (GradeItem grade : gradeList){
                sb.append(grade.getCourseCode()).append(": ").append(grade.getGrade()).append("\n" );
            }
            gradeView.setText(sb.toString());
        }
    }
    public void pollGrades(View view){
        SharedPreferences sharedPref = getSharedPreferences(GradeNotificationService.GradePrefName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        int running=sharedPref.getInt("running",0);
        if (running==1)
            GradeNotificationReceiver.cancel(this);
        else
            editor.putInt("running",1);
        //poll grades on 30min interval right away, will add a new activity for configs.
        GradeNotificationReceiver.initialize(this,1);
    }
    public void cancelPoll(View view) {
        SharedPreferences sharedPref = getSharedPreferences(GradeNotificationService.GradePrefName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("running",0);
        GradeNotificationReceiver.cancel(this);
    }

}
