package com.felixtian.uwgpa;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Felix on 2016/10/24.
 */
//assuming that the cookie manager is already initiated
//used to create sync requests to quest.uwaterloo, see PostRequest for wrapper to make it async
public class PostRequestSync {
    public static int TIMEOUT=5000;
    public static ResponseData Post(String requestURL,String postData){
        return Post(requestURL,postData,"POST");
    }
    public static ResponseData Post(String requestURL,String postData,String requestMethod){
        HttpsURLConnection urlConnection = null;
        BufferedReader reader = null;
        ResponseData responseData=null;
        try{
            URL url = new URL(requestURL);
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(TIMEOUT);
            urlConnection.setReadTimeout(TIMEOUT);
            urlConnection.setInstanceFollowRedirects(true);
            urlConnection.setRequestMethod(requestMethod);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);


            urlConnection.setRequestProperty("Host","quest.pecs.uwaterloo.ca");
            urlConnection.setRequestProperty("Accept","*/*");
            urlConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            //I might not need to set this
            urlConnection.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.27 Safari/537.36");
            OutputStream os = urlConnection.getOutputStream();
            os.write(postData.getBytes());
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
}
