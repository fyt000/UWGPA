package com.felixtian.uwgpa;

/**
 * Created by Felix on 2016/9/19.
 */
/* add more stuff if needed, but for now just use these 2*/
public class ResponseData {
    public String responseContent;
    public int responseCode;
    public boolean passed; //if we needed this for whatever reason
    public ResponseData(int code,String content){
        responseCode=code;responseContent=content;
    }
    public ResponseData(int code,String content,boolean passed){
        responseCode=code;responseContent=content;this.passed=passed;
    }
    public boolean isSuccess(){
        //not sure if I should consider 3xx as failure
        if (responseCode>=400)
            return false;
        else
            return true;
    }
}
