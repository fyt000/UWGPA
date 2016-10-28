package com.felixtian.uwgpa;

import android.os.AsyncTask;

/**
 * Created by Felix on 2016/9/18.
 */
//used to make Async requests - need to attach callbacks
public class PostRequest extends AsyncTask<String, Void, ResponseData> {

    public interface AsyncCallBack {
        void postExecute(ResponseData response);
        void preExecute();
    }

    public AsyncCallBack delegate = null; //grantees to have postExecute method

    public PostRequest(AsyncCallBack callBack){
        delegate=callBack;
    }

    //params[0] is the url
    //params[1] is the post data from PostData
    @Override
    protected ResponseData doInBackground(String... params) {
        if (params.length==3)
            return PostRequestSync.Post(params[0],params[1],params[2]);
        else
            return PostRequestSync.Post(params[0],params[1]);
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
