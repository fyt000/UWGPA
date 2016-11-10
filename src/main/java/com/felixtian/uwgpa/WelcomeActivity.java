package com.felixtian.uwgpa;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;

public class WelcomeActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private TextView welcomeView;
    private ProgressDialog waitDialog;
    private String html;
    private static final ArrayList<GradeItem> gradeItems = new ArrayList<>();
    private static int index=0;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, 0, this)
                .addApi(Auth.CREDENTIALS_API)
                .build();

        welcomeView=(TextView) findViewById(R.id.welcomeView);
        waitDialog=new ProgressDialog(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            html = extras.getString("html");
            DumbScraper scraper1 = new DumbScraper(html);
            welcomeView.setText("Hello, "+scraper1.scrape("id='DERIVED_SSTSNAV_PERSON_NAME'>","</span>"));
            //Log.d("parse",scraper1.scrape("<title id='PSPAGETITLE'>","</title>"));
        }
        else{
            backToLoginDialog();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.welcomeToolbar);
        setSupportActionBar(toolbar);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        //setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class AsyncPullGrade extends AsyncTask<Void,Void,ArrayList<GradeItem>>{
        @Override
        protected ArrayList<GradeItem> doInBackground(Void... params) {
            return  GradeNotificationService.pullGrade();
        }
        protected void onPostExecute(ArrayList<GradeItem> curGrade) {
            waitDialog.dismiss();
            if (curGrade!=null){
                Intent i = new Intent(getApplicationContext(), CurGradeActivity.class);
                //Log.d("oncreate",curGrade.size()+"");
                setBaseLine(curGrade);
                i.putParcelableArrayListExtra("grades", curGrade);
                startActivity(i);
            }
            else{
                Toast.makeText(getApplicationContext(), "fetch grade failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void setBaseLine(ArrayList<GradeItem> grades){
        if (grades==null)
            return;
        //clear all
        SharedPreferences sharedPref = getSharedPreferences(GradeNotificationService.GradePrefName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        int curRunning = sharedPref.getInt("running",0);
        editor.clear();editor.commit();
        editor.putInt("running",curRunning);
        //store the current stuff
        for (GradeItem grade : grades) {
            if (!grade.getGrade().equals("")) {
                editor.putString(grade.getCourseCode(), grade.getGrade());
            }
        }
        editor.commit();
    }
    public void curGrade(View view){
        waitDialog.setMessage("Fetching grade...");
        waitDialog.show();
        new AsyncPullGrade().execute();

    }

    public void getGrades(View view){
        waitDialog.setMessage("Fetching grade...");
        waitDialog.show();
        getGrades();
    }

    public void signOut(View view){
        Auth.CredentialsApi.disableAutoSignIn(mGoogleApiClient);
        GradeNotificationReceiver.cancel(this);
        Intent intent = new Intent(view.getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void getGrades(){

        String url="https://quest.pecs.uwaterloo.ca/psc/SS/ACADEMIC/SA/c/SA_LEARNER_SERVICES.SSR_SSENRL_GRADE.GBL?Page=SSR_SSENRL_GRADE&Action=A";
        PostRequest post1 = (PostRequest) new PostRequest(new PostRequest.AsyncCallBack(){
            @Override
            public void preExecute(){
            }
            @Override
            public void postExecute(ResponseData r){
                DumbScraper dumbScraper = new DumbScraper(r.responseContent);
                String loggedOff = dumbScraper.scrape("id=\"login\" name=\"login\"","document.login");
                if (!loggedOff.equals("")){
                    backToLoginDialog();
                    return;
                }
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
                            DumbScraper dumbScraper = new DumbScraper(r.responseContent);
                            String loggedOff = dumbScraper.scrape("id=\"login\" name=\"login\"","document.login");
                            if (!loggedOff.equals("")){
                                backToLoginDialog();
                                return;
                            }
                            gradeItems.addAll(gradeParsing(r.responseContent));
                            index++;
                            getGrades();
                        }
                    }).execute(url,postData.toString(),"POST");
                }
            }
        }).execute(url,"","GET");
    }
    public static ArrayList<GradeItem> gradeParsing(String html){
        ArrayList<GradeItem> grades = new ArrayList<GradeItem>();
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
            grades.add(new GradeItem(courseID,courseGrade,GPAConvert.convert(courseGrade)));
        }
        return grades;
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
    public void curGrade(){
        //pull grade, wrap it in async task
        //send it to a new CurGradeActivity
    }

}
