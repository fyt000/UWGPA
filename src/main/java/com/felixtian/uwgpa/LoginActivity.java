package com.felixtian.uwgpa;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

public class LoginActivity extends AppCompatActivity {

    private EditText userIDView;
    private EditText pswdView;
    private ProgressDialog waitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //move this to application oncreate
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

        userIDView = (EditText) findViewById(R.id.IDText);
        pswdView = (EditText) findViewById(R.id.PswdText);

        waitDialog=new ProgressDialog(this);
    }

    public void login(View view) {
        String url="https://quest.pecs.uwaterloo.ca/psp/SS/?cmd=login&languageCd=ENG";
        PostData postData = new PostData();
        postData.add("httpPort","");postData.add("timezoneOffset","240");postData.add("userid",userIDView.getText().toString().toUpperCase());
        postData.add("pwd",pswdView.getText().toString());postData.add("Submit","Sign in");

        PostRequest post1 = (PostRequest) new PostRequest(new PostRequest.AsyncCallBack(){
            @Override
            public void preExecute(){
                waitDialog.setMessage("Logging in...");
                waitDialog.show();
            }
            @Override
            public void postExecute(ResponseData r){
                //waitDialog.dismiss();
                //Toast.makeText(getApplicationContext(), r.responseCode+"", Toast.LENGTH_SHORT).show();
                Log.d("post","Req1 "+r.responseCode+"");
                Log.d("post","Req1 "+r.responseContent);
                //I need to run 2 requests

                String url="https://quest.pecs.uwaterloo.ca/psc/SS/ACADEMIC/SA/c/SA_LEARNER_SERVICES.SSR_SSENRL_GRADE.GBL?Page=SSR_SSENRL_GRADE&Action=A";
                PostRequest post2 = (PostRequest) new PostRequest(new PostRequest.AsyncCallBack(){
                    @Override
                    public void preExecute(){
                    }
                    @Override
                    public void postExecute(ResponseData r){
                        waitDialog.dismiss();
                        Log.d("post","Req2 "+r.responseCode+"");
                        //I could put more stuff into ResponseData like new location header and check for error params
                        if (r.responseContent.indexOf("DERIVED_SSTSNAV_PERSON_NAME")!=-1){
                            GradeNotificationService.password=pswdView.getText().toString();
                            GradeNotificationService.username=userIDView.getText().toString().toUpperCase();
                            //usually this is good
                            Intent i = new Intent(getApplicationContext(), WelcomeActivity.class);
                            i.putExtra("html",r.responseContent); //lets just pass the whole thing over
                            //Should I also pass the pswd and userID .. so I could re-loggin and refresh session etc.
                            startActivity(i);
                            //finish(); //should be able to come back here
                        }
                        else{
                            if (r.responseCode==200)
                                Toast.makeText(getApplicationContext(), "Incorrect ID/Password", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(getApplicationContext(), "Check your connection", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).execute(url,"","GET");
            }
        }).execute(url,postData.toString(),"POST");


    }
}


