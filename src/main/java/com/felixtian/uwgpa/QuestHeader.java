package com.felixtian.uwgpa;

import android.util.Log;

/**
 * Created by Felix on 2016/9/21.
 */

public class QuestHeader {
    static PostData GetGradeFormData(String html,String index){
        PostData postData=new PostData();
        DumbScraper dumbScraper=new DumbScraper(html);
        postData.add("ICSID",dumbScraper.scrape("<input type='hidden' name='ICSID' id='ICSID' value='","' />"));
        postData.add("ICStateNum",dumbScraper.scrape("<input type='hidden' name='ICStateNum' id='ICStateNum' value='","' />"));
        Log.d("parse",postData.toString());
        postData.add("ICAJAX","1");
        postData.add("ICNAVTYPEDROPDOWN","0");
        postData.add("ICType","Panel");
        postData.add("ICElementNum","0");
        postData.add("ICAction","DERIVED_SSS_SCT_SSR_PB_GO");
        postData.add("ICXPos","0");
        postData.add("ICYPos","0");
        postData.add("ResponsetoDiffFrame","-1");
        postData.add("TargetFrameName","None");
        postData.add("FacetPath","None");
        postData.add("ICFocus","");
        postData.add("ICSaveWarningFilter","0");
        postData.add("ICChanged","-1");
        postData.add("ICResubmit","0");
        postData.add("ICActionPrompt","false");
        postData.add("ICFind","");
        postData.add("ICAddCount","");
        postData.add("ICAPPCLSDATA","");
        postData.add("DERIVED_SSTSNAV_SSTS_MAIN_GOTO$7$","9999");
        postData.add("DERIVED_SSTSNAV_SSTS_MAIN_GOTO$8$","9999");
        postData.add("SSR_DUMMY_RECV1$sels$0",index);
        /*
        SSR_DUMMY_RECV1$sels$0:0
         */
        return postData;
    }
}
