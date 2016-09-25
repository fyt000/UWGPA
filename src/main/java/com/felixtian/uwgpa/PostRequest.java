package com.felixtian.uwgpa;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.cert.Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;

/**
 * Created by Felix on 2016/9/18.
 */
//used to make requests
//assuming that the cookie manager is already initiated
public class PostRequest extends AsyncTask<String, Void, ResponseData> {

    public static int TIMEOUT=5000;

    public interface AsyncCallBack {
        void postExecute(ResponseData response);
        void preExecute();
    }

    public AsyncCallBack delegate = null; //grantees to have postExecute method

    public PostRequest(AsyncCallBack a){
        delegate=a;
    }

    //params[0] is the url
    //params[1] is the post data from PostData
    @Override
    protected ResponseData doInBackground(String... params) {
        HttpsURLConnection urlConnection = null;
        BufferedReader reader = null;
        ResponseData responseData=null;
        try{
            URL url = new URL(params[0]);
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(TIMEOUT);
            urlConnection.setReadTimeout(TIMEOUT);
            urlConnection.setInstanceFollowRedirects(true);
            urlConnection.setRequestMethod(params[2]);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);


            urlConnection.setRequestProperty("Host","quest.pecs.uwaterloo.ca");
            urlConnection.setRequestProperty("Accept","*/*");
            urlConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            //I might not need to set this
            urlConnection.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.27 Safari/537.36");
            OutputStream os = urlConnection.getOutputStream();
            os.write(params[1].getBytes());
            os.flush();

            int code = urlConnection.getResponseCode();
            Log.d("post","header location "+urlConnection.getHeaderField("Location"));
            Log.d("post","new url "+urlConnection.getURL());

            BufferedReader br = new BufferedReader(new InputStreamReader((urlConnection.getInputStream())));
            String output;
            StringBuffer sb = new StringBuffer();
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            responseData=new ResponseData(code,sb.toString());
        }
        catch (SocketTimeoutException e) {
            e.printStackTrace();
            responseData=new ResponseData(408,"Request timeout");
        }
        catch (IOException e){
            e.printStackTrace();
            responseData=new ResponseData(400,"Request error");
        }
        finally{
            urlConnection.disconnect();
        }

        return responseData;
    }

    @Override
    protected void onPreExecute() {
        delegate.preExecute();
    }

    @Override
    protected void onPostExecute(ResponseData r) {
        delegate.postExecute(r);
    }

}
