package com.felixtian.uwgpa;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.CredentialRequestResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GradeNotificationService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_POLL = "com.felixtian.uwgpa.action.POLLGRADE";
    private static final int NOTIFICATION_ID = 1;
    //not sure if I should place them here
    public static String username="";
    public static String password="";
    public static String GradePrefName="UWGradeRecord";

    public GradeNotificationService() {
        super("GradeNotificationService");
    }


    public static Intent prepareIntentPoll(Context context) {
        Intent intent = new Intent(context, GradeNotificationService.class);
        intent.setAction(ACTION_POLL);
        //context.startService(intent);
        return intent;
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_POLL.equals(action)) {
                handleActionPoll();
            }
            WakefulBroadcastReceiver.completeWakefulIntent(intent);
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     * Not sure if it safe to pass username and pw around
     */
    private void handleActionPoll() {

        //if password is missing - need to login again
        if (password.equals("")||username.equals("")) {
            GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Auth.CREDENTIALS_API)
                    .build();
            mGoogleApiClient.blockingConnect(5, TimeUnit.SECONDS);
            if (mGoogleApiClient.isConnected()) {
                CredentialRequest request = new CredentialRequest.Builder()
                        .setSupportsPasswordLogin(true)
                        .build();

                CredentialRequestResult credentialReq = Auth.CredentialsApi.request(mGoogleApiClient, request).await(5, TimeUnit.SECONDS);
                if (credentialReq.getStatus().isSuccess()) {
                    Credential credential = credentialReq.getCredential();
                    password=credential.getPassword();
                    username=credential.getId();
                }

            }
        }
        boolean showNotification=true; //TODO set this to false on production
        ArrayList<GradeItem> grades = pullGrade(); //sync requests to get grades
        if (grades==null) {
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setContentTitle("Grade poll service reset")
                    .setAutoCancel(true)
                    .setContentText("Please login and restart")
                    .setSmallIcon(R.drawable.ic_arrow_downward_black_24dp);

            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    NOTIFICATION_ID,
                    new Intent(this, LoginActivity.class),
                    PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);

            final NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(NOTIFICATION_ID, builder.build());
        }

        SharedPreferences sharedPref = getSharedPreferences(GradePrefName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        StringBuilder notificationSB = new StringBuilder();

        //check if there is an update
        for (GradeItem grade : grades) {
            String curGrade = sharedPref.getString(grade.getCourseCode(),null);
            if (curGrade==null) { //check if we already have a grade for it
                if (!grade.getGrade().equals("")) { //non empty means we got an actual update
                    showNotification = true;
                    notificationSB.append(" ").append(grade.getCourseCode()).append(": ").append(grade.getGrade());
                    editor.putString(grade.getCourseCode(),grade.getGrade());
                }
            }
            editor.putString(grade.getCourseCode(),grade.getGrade());
        }

        Log.d("notification","content "+notificationSB.toString());
        if (!showNotification)
            return;

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle("Your grades has been updated")
                .setAutoCancel(true)
                .setContentText(notificationSB.toString())
                .setSmallIcon(R.drawable.ic_arrow_downward_black_24dp);

        Intent curGradeIntent =  new Intent(this, CurGradeActivity.class);
        curGradeIntent.putParcelableArrayListExtra("grades", grades);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                NOTIFICATION_ID,
                curGradeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        final NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, builder.build());
    }

    public static ResponseData tryLogin(String username,String password){
        if (username==null||username.equals("")||password==null||password.equals("")){
            ResponseData response = new ResponseData(400,"Failed",false);
            return response;
        }

        String loginUrl="https://quest.pecs.uwaterloo.ca/psp/SS/?cmd=login&languageCd=ENG";
        PostData postData = new PostData();
        postData.add("httpPort","");postData.add("timezoneOffset","240");postData.add("userid",username.toUpperCase());
        postData.add("pwd",password);postData.add("Submit","Sign in");
        PostRequestSync.Post(loginUrl,postData.toString());

        String gradeUrl="https://quest.pecs.uwaterloo.ca/psc/SS/ACADEMIC/SA/c/SA_LEARNER_SERVICES.SSR_SSENRL_GRADE.GBL?Page=SSR_SSENRL_GRADE&Action=A";
        ResponseData response= PostRequestSync.Post(gradeUrl,"","GET");
        if (response.responseContent.indexOf("DERIVED_SSTSNAV_PERSON_NAME")==-1){ //login failed
            response.passed=false;
            return response;
        }
        response.passed=true;
        return response;
    }


    public static ArrayList<GradeItem> pullGrade(){
        ArrayList<GradeItem> grades=null;


        ResponseData response = tryLogin(username,password);
        if (!response.passed)
            return null;
        Log.d("notification","logged in");
        //forgot why, but do it again
        String gradeUrl="https://quest.pecs.uwaterloo.ca/psc/SS/ACADEMIC/SA/c/SA_LEARNER_SERVICES.SSR_SSENRL_GRADE.GBL?Page=SSR_SSENRL_GRADE&Action=A";
        response= PostRequestSync.Post(gradeUrl,"","GET");
        //now find the index
        //I can't just use index 0 or 1 - it just may not be the term
        //based on use case following should be correct
        //Dec check Fall
        //Jan - March check Fall of previous year
        //April - July check Winter
        //August - November check Spring
        //need to store the marks somewhere.
        Calendar current = Calendar.getInstance();
        String lookupStr;
        switch (current.get(current.MONTH)){
            case Calendar.JANUARY:
            case Calendar.FEBRUARY:
            case Calendar.MARCH:
                lookupStr=String.format("Fall %d",current.get(current.YEAR)-1);break;
            case Calendar.APRIL:
            case Calendar.MAY:
            case Calendar.JUNE:
            case Calendar.JULY:
                lookupStr=String.format("Winter %d",current.get(current.YEAR));break;
            case Calendar.AUGUST:
            case Calendar.SEPTEMBER:
            case Calendar.OCTOBER:
            case Calendar.NOVEMBER:
                lookupStr=String.format("Spring %d",current.get(current.YEAR));break;
            case Calendar.DECEMBER:
                lookupStr=String.format("Fall %d",current.get(current.YEAR));break;
            default:
                lookupStr=String.format("Fall %d",current.get(current.YEAR));
        }
        Log.d("notification",lookupStr);
        DumbScraper dumbScraper = new DumbScraper(response.responseContent);
        String indexStr = dumbScraper.backwardScrape("id='TERM_CAR$","'>"+lookupStr);
        if (indexStr.isEmpty())
            return grades;
        PostData postData = QuestHeader.GetGradeFormData(response.responseContent,indexStr);
        response=PostRequestSync.Post("https://quest.pecs.uwaterloo.ca/psc/SS/ACADEMIC/SA/c/SA_LEARNER_SERVICES.SSR_SSENRL_GRADE.GBL",
                postData.toString());
        grades=WelcomeActivity.gradeParsing(response.responseContent);

        return grades;
    }
}
