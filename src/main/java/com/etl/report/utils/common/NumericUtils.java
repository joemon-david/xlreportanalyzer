package com.etl.report.utils.common;

public class NumericUtils {
    public static Double percentageOfDifference(Double value1,Double value2)
    {

        return (((value2-value1)/value1)*100);

    }
}
