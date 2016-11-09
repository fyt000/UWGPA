package com.felixtian.uwgpa;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.CredentialRequestResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SAVE=0;
    private EditText userIDView;
    private EditText pswdView;
    private Button submitBtn;
    private ProgressBar loadBar;
    private ProgressDialog waitDialog;
    private GoogleApiClient mGoogleApiClient;
    private boolean mIsResolving;
    private String responseContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, 0, this)
                .addApi(Auth.CREDENTIALS_API)
                .build();

        //move this to application oncreate
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

        userIDView = (EditText) findViewById(R.id.IDText);
        pswdView = (EditText) findViewById(R.id.PswdText);
        submitBtn = (Button) findViewById(R.id.loginButton);
        loadBar = (ProgressBar) findViewById(R.id.waitProgressBar);
        //waitDialog=new ProgressDialog(this);
    }

    //this works for auto login... so I've already stored credential
    private class AsyncTryLoginAuto extends AsyncTask<Credential,Void,ResponseData> {
        Credential credential;
        @Override
        protected ResponseData doInBackground(Credential... params) {
            credential=params[0];
            return  GradeNotificationService.tryLogin(credential.getId(),credential.getPassword());
        }
        protected void onPostExecute(ResponseData r) {
            //waitDialog.dismiss();
            if (r.responseContent.indexOf("DERIVED_SSTSNAV_PERSON_NAME")!=-1){
                //valid password
                GradeNotificationService.username=credential.getId();
                GradeNotificationService.password=credential.getPassword();
                responseContent=r.responseContent;
                /*
                Credential credential = new Credential.Builder(GradeNotificationService.username)
                        .setPassword(GradeNotificationService.password)
                        .build();*/
                //saveCredential(credential);
                goToContent();
            }
            else{
                if (r.responseCode==200)
                    Toast.makeText(getApplicationContext(), "Incorrect ID/Password", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), "Check your connection", Toast.LENGTH_SHORT).show();
                showLoginInput();

            }
        }
    }

    private class AsyncTryLogin extends AsyncTask<String,Void,ResponseData> {
        String username;
        String password;
        @Override
        protected ResponseData doInBackground(String... params) {
            username=params[0];password=params[1];
            return  GradeNotificationService.tryLogin(username,password);
        }
        protected void onPostExecute(ResponseData r) {
            //waitDialog.dismiss();
            if (r.responseContent.indexOf("DERIVED_SSTSNAV_PERSON_NAME")!=-1){
                //valid password
                GradeNotificationService.username=username;
                GradeNotificationService.password=password;
                responseContent=r.responseContent;
                Credential credential = new Credential.Builder(GradeNotificationService.username)
                        .setPassword(GradeNotificationService.password)
                        .build();
                saveCredential(credential); //this will call goto content
            }
            else{
                if (r.responseCode==200)
                    Toast.makeText(getApplicationContext(), "Incorrect ID/Password", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), "Check your connection", Toast.LENGTH_SHORT).show();
                showLoginInput();

            }
        }
    }

    public void login(View view) {
        //waitDialog.setMessage("Trying to login....");
        //waitDialog.show();
        userIDView.clearFocus();
        pswdView.clearFocus();
        String username=userIDView.getText().toString().toUpperCase();
        String password = pswdView.getText().toString();
        userIDView.setVisibility(View.INVISIBLE);
        pswdView.setVisibility(View.INVISIBLE);
        submitBtn.setVisibility(View.INVISIBLE);
        loadBar.setVisibility(View.VISIBLE);
        new AsyncTryLogin().execute(username,password);
    }

    private void goToContent(){
        if (responseContent==null)
            return;
        Intent i = new Intent(getApplicationContext(), WelcomeActivity.class);
        i.putExtra("html",responseContent); //lets just pass the whole thing over
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    private void showLoginInput(){
        loadBar.setVisibility(View.INVISIBLE);
        userIDView.setVisibility(View.VISIBLE);
        pswdView.setVisibility(View.VISIBLE);
        submitBtn.setVisibility(View.VISIBLE);
    }

    protected void saveCredential(Credential credential) {
        // Credential is valid so save it.
        Auth.CredentialsApi.save(mGoogleApiClient,
                credential).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    Log.d("credential", "Credential saved");
                    goToContent();
                  } else {
                    Log.d("credential", "Attempt to save credential failed " +
                            status.getStatusMessage() + " " +
                            status.getStatusCode());
                    resolveResult(status, RC_SAVE);
                }
            }
        });
    }
    private void deleteCredential(Credential credential) {
        Auth.CredentialsApi.delete(mGoogleApiClient,
                credential).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    Log.d("credential", "Credential successfully deleted.");
                } else {
                    // This may be due to the credential not existing, possibly
                    // already deleted via another device/app.
                    Log.d("credential", "Credential not deleted successfully.");
                }
            }
        });
    }

    private void resolveResult(Status status, int requestCode) {
        // We don't want to fire multiple resolutions at once since that
        // can   result in stacked dialogs after rotation or another
        // similar event.
        if (mIsResolving) {
            Log.w("credential", "resolveResult: already resolving.");
            return;
        }

        Log.d("credential", "Resolving: " + status);
        if (status.hasResolution()) {
            Log.d("credential", "STATUS: RESOLVING");
            try {
                status.startResolutionForResult(this, requestCode);
                mIsResolving = true;
            } catch (IntentSender.SendIntentException e) {
                showLoginInput();
                Log.e("credential", "STATUS: Failed to send resolution.", e);
            }
        } else {
            Log.e("credential", "STATUS: FAIL");
            if (requestCode == RC_SAVE) {
                goToContent();
            }
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("credential", "onActivityResult:" + requestCode + ":" + resultCode + ":" +
                data);
        if (requestCode == RC_SAVE) {
            Log.d("credential", "Result code: " + resultCode);
            if (resultCode == RESULT_OK) {
                Log.d("credential", "Credential Save: OK");
            } else {
                Log.e("credential", "Credential Save Failed");
            }
            goToContent();
        }
        mIsResolving = false;
    }
    private void processRetrievedCredential(Credential credential) {
        //waitDialog.setMessage("Trying to login....");
        //waitDialog.show();
        userIDView.setVisibility(View.INVISIBLE);
        pswdView.setVisibility(View.INVISIBLE);
        submitBtn.setVisibility(View.INVISIBLE);
        loadBar.setVisibility(View.VISIBLE);
        new AsyncTryLoginAuto().execute(credential);
    }

    private void requestCredentials() {
        //mIsRequesting = true;

        CredentialRequest request = new CredentialRequest.Builder()
                .setSupportsPasswordLogin(true)
                .build();

        Auth.CredentialsApi.request(mGoogleApiClient, request).setResultCallback(
                new ResultCallback<CredentialRequestResult>() {
                    @Override
                    public void onResult(CredentialRequestResult credentialRequestResult) {
                        //mIsRequesting = false;
                        Status status = credentialRequestResult.getStatus();
                        if (credentialRequestResult.getStatus().isSuccess()) {
                            // Successfully read the credential without any user interaction, this
                            // means there was only a single credential and the user has auto
                            // sign-in enabled.
                            Credential credential = credentialRequestResult.getCredential();
                            processRetrievedCredential(credential);
                        } else if (status.getStatusCode() == CommonStatusCodes.SIGN_IN_REQUIRED) {
                            //setFragment(null);
                            // This is most likely the case where the user does not currently
                            // have any saved credentials and thus needs to provide a username
                            // and password to sign in.
                            Log.d("credential", "Sign in required");
                            //setSignInEnabled(true);
                            showLoginInput();
                        } else {
                            Log.w("credential", "Unrecognized status code: " + status.getStatusCode());
                            //setFragment(null);
                            //setSignInEnabled(true);
                            showLoginInput();
                        }
                    }
                }
        );

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        requestCredentials();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}


