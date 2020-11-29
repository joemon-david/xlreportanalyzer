package com.etl.report.utils.common;

public class TypeIdentifier {

    public enum DATA_TYPES {STRING,DOUBLE};

    public static DATA_TYPES getDataTypes(String data)
    {
        Object obj;
        DATA_TYPES type = DATA_TYPES.STRING;
        try {
            obj= Double.parseDouble(data);
        } catch (NumberFormatException e) {
            obj=data;
        }
        if (obj instanceof Double)
            type = DATA_TYPES.DOUBLE;
        return type;
    }


    public static void main(String[] args) {

        if (TypeIdentifier.getDataTypes("-468.5") == DATA_TYPES.DOUBLE)
            System.out.println("The value is a Numeric Type");

    }
}
