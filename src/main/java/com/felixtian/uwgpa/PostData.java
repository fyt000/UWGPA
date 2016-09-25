package com.felixtian.uwgpa;

import android.util.Pair;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Felix on 2016/9/18.
 */
public class PostData {
    private StringBuilder dataSb;
    private boolean init;
    PostData(){
        dataSb=new StringBuilder();
        init=true;
    }

    /*
    return true on success else false
     */
    boolean add(String k,String v){
        try {
            k=URLEncoder.encode(k, "UTF-8");
            v=URLEncoder.encode(v, "UTF-8");
        }
        catch (UnsupportedEncodingException e){
            e.printStackTrace();
            return false;
        }
        if (!init)
            dataSb.append("&");
        else
            init=false;
        dataSb.append(k);
        dataSb.append("=");
        dataSb.append(v);
        return true;
    }

    @Override
    public String toString() {
        return dataSb.toString();
    }
}
