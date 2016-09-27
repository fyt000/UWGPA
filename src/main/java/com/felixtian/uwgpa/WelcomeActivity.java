package com.felixtian.uwgpa;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class WelcomeActivity extends AppCompatActivity {
    private TextView welcomeView;
    private ProgressDialog waitDialog;
    private String html;
    private static final ArrayList<GradeItem> gradeItems = new ArrayList<>();
    private static int index=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        welcomeView=(TextView) findViewById(R.id.welcomeView);
        waitDialog=new ProgressDialog(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            html = extras.getString("html");
            DumbScraper scraper1 = new DumbScraper(html);
            welcomeView.setText("Welcome, "+scraper1.scrape("id='DERIVED_SSTSNAV_PERSON_NAME'>","</span>"));
            Log.d("parse",scraper1.scrape("<title id='PSPAGETITLE'>","</title>"));
        }
        else{
            backToLoginDialog();
        }

    }
    public void getGrades(View view){
        waitDialog.setMessage("Fetching grade...");
        waitDialog.show();
        getGradesX(html);
    }
    private void getGradesX(String curHtml){

        String url="https://quest.pecs.uwaterloo.ca/psc/SS/ACADEMIC/SA/c/SA_LEARNER_SERVICES.SSR_SSENRL_GRADE.GBL?Page=SSR_SSENRL_GRADE&Action=A";
        PostRequest post1 = (PostRequest) new PostRequest(new PostRequest.AsyncCallBack(){
            @Override
            public void preExecute(){
            }
            @Override
            public void postExecute(ResponseData r){
                DumbScraper dumbScraper = new DumbScraper(r.responseContent);
                String result =dumbScraper.scrape("'TERM_CAR$"+index,"<!-- TERM_CAR$"+index);
                Log.d("oncreate","checking "+index);
                if (result.equals("")){
                    waitDialog.dismiss();
                    Intent i = new Intent(getApplicationContext(), GradeActivity.class);
                    Log.d("oncreate",gradeItems.size()+"");
                    i.putParcelableArrayListExtra("grades", gradeItems);
                    startActivity(i);
                    return; //in case it doesn't
                }
                else{
                    String url="https://quest.pecs.uwaterloo.ca/psc/SS/ACADEMIC/SA/c/SA_LEARNER_SERVICES.SSR_SSENRL_GRADE.GBL";
                    PostData postData = QuestHeader.GetGradeFormData(r.responseContent,index+"");
                    new PostRequest(new PostRequest.AsyncCallBack() {
                        @Override
                        public void preExecute() {
                            //do nothing I guess
                        }
                        public void postExecute(ResponseData r){
                            Log.d("oncreate","post execute at "+index);
                            gradeParsing(r.responseContent);
                            index++;
                            getGradesX(r.responseContent);
                        }
                    }).execute(url,postData.toString(),"POST");
                }
            }
        }).execute(url,"","GET");
    }
    private void gradeParsing(String html){
        for (int i=0;i<10;i++) { //assume you have a max of 10 courses right now
            String courseIDP1=String.format("'CLS_LINK$%d');\"  class='PSHYPERLINK' >",i);
            String courseIDP2=String.format("</a></span>");
            String courseGradeP1= String.format("id='STDNT_ENRL_SSV1_CRSE_GRADE_OFF$%d'>",i);
            String courseGradeP2=String.format("</span><!-- STDNT_ENRL_SSV1_CRSE_GRADE_OFF$%d -->",i);
            DumbScraper dumbScraper = new DumbScraper(html);
            String courseID = dumbScraper.scrape(courseIDP1,courseIDP2);
            if (courseID.equals("")) {
                Log.d("oncreate","exit at "+i);
                break;
            }
            String courseGrade = dumbScraper.scrape(courseGradeP1,courseGradeP2);
            if (courseGrade.equals("&nbsp;"))
                courseGrade="";
            gradeItems.add(new GradeItem(courseID,courseGrade,GPAConvert.convert(courseGrade)));
        }
    }
    private void backToLoginDialog(){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Timeout");
        alertDialog.setMessage("Session timed out, please re-login");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent loginIntent = new Intent(WelcomeActivity.this, LoginActivity.class);
                        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(loginIntent);
                        finish();
                    }
                });
        alertDialog.show();
    }
}
