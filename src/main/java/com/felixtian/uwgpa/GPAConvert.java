package com.felixtian.uwgpa;

/**
 * Created by Felix on 2016/9/24.
 */
//values are taken from
//http://www.ouac.on.ca/docs/omsas/c_omsas_b.pdf
public class GPAConvert {
    public static String convert(String percMarkStr){
        try{
            int percMarkInt = Integer.parseInt(percMarkStr);
            if (percMarkInt>=90)
                return "4.00";
            else if (percMarkInt>=85)
                return "3.90";
            else if (percMarkInt>=80)
                return "3.70";
            else if (percMarkInt>=77)
                return "3.30";
            else if (percMarkInt>=73)
                return "3.00";
            else if (percMarkInt>=70)
                return "2.70";
            else if (percMarkInt>=67)
                return "2.30";
            else if (percMarkInt>=63)
                return "2.00";
            else if (percMarkInt>=60)
                return "1.70";
            else if (percMarkInt>=57)
                return "1.30";
            else if (percMarkInt>=53)
                return "1.00";
            else if (percMarkInt>=50)
                return "0.70";
            else
                return "0.00";
        }
        catch (NumberFormatException e){
            //WD, CR, whatelse?
            return "";
        }
    }
}
