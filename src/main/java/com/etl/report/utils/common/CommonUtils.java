package com.etl.report.utils.common;

public class CommonUtils {

    public static  String extractTransLogicType(String entry)
    {

        if(entry == null)
            return "";
        String transType = (null!=entry  && !entry.isEmpty() && entry.contains("("))?entry.substring(0,entry.indexOf('(')):entry;
        return transType;
    }
    public static  Object extractTransLogicValue(String entry)
    {
        Object transValue = (null!=entry  && !entry.isEmpty() && entry.contains("("))? entry.substring(entry.indexOf('(')+1,entry.indexOf(')')):entry;
        return transValue;
    }
}
