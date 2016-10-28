package com.felixtian.uwgpa;

/**
 * Created by Felix on 2016/9/21.
 */

public class DumbScraper {
    private String content;
    public DumbScraper(String content) {
        this.content=content;
    }
    public String backwardScrape(String p1,String p2){
        int idx1=content.lastIndexOf(p2);
        if (idx1==-1){
            //content="";
            return "";
        }
        String rest=content.substring(0,idx1);
        int idx2=rest.lastIndexOf(p1);
        if (idx2==-1){
            //content="";
            return "";
        }
        //content=rest.substring(0,idx2);
        return rest.substring(idx2+p1.length());
    }
    public String scrape(String p1,String p2){
        int idx1=content.indexOf(p1);
        if (idx1==-1){
            //content="";
            return "";
        }
        String rest=content.substring(idx1 + p1.length());
        int idx2=rest.indexOf(p2);
        if (idx2==-1){
            //content="";
            return "";
        }
        //content=rest.substring(0,idx2);
        return rest.substring(0,idx2);
    }
    /*
    public String getContent(){
        return content;
    }*/
}
