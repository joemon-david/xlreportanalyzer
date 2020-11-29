package com.etl.report.utils.dataobjects;

import com.etl.report.business.LogTester;
import com.etl.report.constants.ConfigData;
import com.etl.report.utils.common.CommonUtils;
import com.etl.report.utils.common.NumericUtils;
import com.etl.report.utils.common.TypeIdentifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class DataExtractor implements ConfigData {

    private static final Logger logger = LogManager.getLogger(DataExtractor.class);

    public LinkedHashSet<String> getUniqueColumnList(LinkedHashMap<Integer, LinkedHashMap<String, Object>> sheetData, String headerName) {
        LinkedHashSet<String> columnData = new LinkedHashSet<>();

        sheetData.forEach((index, rowData) -> {
            columnData.add(rowData.get(headerName).toString());
        });
        return columnData;
    }

    public LinkedHashSet<String> getConditionalColumnValueList(LinkedHashMap<Integer, LinkedHashMap<String, Object>> sheetData, String conditionalHeaderName,
                                                               String conditionalValue, String columnHeader) {
        LinkedHashSet<String> columnData = new LinkedHashSet<>();

        sheetData.forEach((index, rowData) -> {
            if (rowData.get(conditionalHeaderName).toString().equalsIgnoreCase(conditionalValue))
                columnData.add(rowData.get(columnHeader).toString());
        });
        return columnData;
    }

    public String getConditionalColumnValue(LinkedHashMap<Integer, LinkedHashMap<String, Object>> sheetData, String conditionalHeaderName,
                                            String conditionalValue, String columnHeader) {
        String columnValue = "";

        for (Integer index : sheetData.keySet()) {
            LinkedHashMap<String, Object> rowData = sheetData.get(index);
            if (rowData.get(conditionalHeaderName).toString().equalsIgnoreCase(conditionalValue)) {
                columnValue = rowData.get(columnHeader).toString();
                break;
            }
        }
        return columnValue;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getSrcMappingList(LinkedHashMap<Integer, LinkedHashMap<String, Object>> s, ArrayList<String> fileTypeToRunList, String targetColumn) {
        LinkedHashMap<String, LinkedHashMap<String, String>> srcTargetColumnMapList = new LinkedHashMap<String, LinkedHashMap<String, String>>();
        /**
         * By using this logic we are expecting that the source column having unique column names across all the fileTypes.
         */
        for (String fileType : fileTypeToRunList) {
            LinkedHashSet<String> srcList = getConditionalColumnValueList(s, COMPARE_FILE_TYPE, fileType, COMPARE_SRC_COLUMN);
            LinkedHashMap<String, String> columnMap = new LinkedHashMap<>();
            srcList.forEach((key) -> {
                String value = getConditionalColumnValue(s, COMPARE_SRC_COLUMN, key, targetColumn);
                columnMap.put(key, value);
            });
            srcTargetColumnMapList.put(fileType, columnMap);
        }
        return srcTargetColumnMapList;
    }


    private LinkedHashMap<String, String> appendSrcTargetColumnWithPostFix(LinkedHashMap<String, String> srcTargetColumnMap, boolean isTransLogic) {
        LinkedHashMap<String, String> appendedMap = new LinkedHashMap<String, String>();
        for (String src : srcTargetColumnMap.keySet()) {
            if (isTransLogic)
                appendedMap.put(src + COMPARE_SOURCE_POSTFIX, srcTargetColumnMap.get(src));
            else
                appendedMap.put(src + COMPARE_SOURCE_POSTFIX, srcTargetColumnMap.get(src) + COMPARE_TARGET_POSTFIX);
        }
        return appendedMap;
    }

    public LinkedHashMap<String, String> analyzeRowAndAddResult(LinkedHashMap<String, Object> rowData, LinkedHashMap<String, String> srcTargetColumnMap, LinkedHashMap<String, String> srcTransLogicMap) {
        LinkedHashMap<String, String> analyzedData = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> sourceTargetMap = appendSrcTargetColumnWithPostFix(srcTargetColumnMap, false);
        LinkedHashMap<String, String> transLogicMap = appendSrcTargetColumnWithPostFix(srcTransLogicMap, false);
        /**
         * Iterate through all the column of the Row passed and checks whether the column is to be analysed
         * and to add the comparison and final results
         */
        for (String column : rowData.keySet()) {

            if (sourceTargetMap.containsKey(column) && !analyzedData.containsKey(column)) {
                logger.debug("Match found " + column);
                String value1 = rowData.get(column).toString();
                String value2 = rowData.get(sourceTargetMap.get(column)).toString();
                String cVal = null, finalResult = null, deviation = null;
                boolean isKnownDifferance = false;
                String transLogic = transLogicMap.get(column);
                Double allowedTolerance = 0.0;
                if (null == transLogic)
                    allowedTolerance = 0.0;
                else if (CommonUtils.extractTransLogicType(transLogic).equalsIgnoreCase(COMPARE_TRANS_LOGIC_TOLERANCE))
                    allowedTolerance = Double.parseDouble(CommonUtils.extractTransLogicValue(transLogic).toString());
                else if (CommonUtils.extractTransLogicType(transLogic).equalsIgnoreCase(COMPARE_TRANS_LOGIC_KNOWN_DIFF))
                    isKnownDifferance = true;

                if (value1.equalsIgnoreCase(value2)) {
                    cVal = (TypeIdentifier.getDataTypes(value1) == TypeIdentifier.DATA_TYPES.DOUBLE) ? "0.0" : COMPARE_RESULT_MATCHED;
                    deviation = (TypeIdentifier.getDataTypes(value1) == TypeIdentifier.DATA_TYPES.DOUBLE) ? "0.0" : null;
                    finalResult = COMPARE_RESULT_PASSED;
                } else if (TypeIdentifier.getDataTypes(value1) == TypeIdentifier.DATA_TYPES.DOUBLE && TypeIdentifier.getDataTypes(value2) == TypeIdentifier.DATA_TYPES.DOUBLE) {

                    cVal = Double.parseDouble(value1) - Double.parseDouble(value2) + "";
                    Double diffPercentage = NumericUtils.percentageOfDifference(Double.parseDouble(value1), Double.parseDouble(value2));
                    deviation = diffPercentage + "";
                    finalResult = (Math.abs(diffPercentage) <= allowedTolerance || isKnownDifferance) ? COMPARE_RESULT_PASSED : COMPARE_RESULT_FAILED;

                } else {
                    if (value1.equalsIgnoreCase("Not Available") || value2.equalsIgnoreCase("Not Available")) {
                        // Can add the logic later
                    } else {
                        cVal = (isKnownDifferance) ? COMPARE_RESULT_KNOWN_DIFF : COMPARE_RESULT_DIFF;
                        deviation = (TypeIdentifier.getDataTypes(value1) == TypeIdentifier.DATA_TYPES.DOUBLE) ? "0.0" : null;
                        finalResult = (isKnownDifferance) ? COMPARE_RESULT_PASSED : COMPARE_RESULT_FAILED;
                    }
                }
                analyzedData.put(column,rowData.get(column)+"");
                String targetHeader = sourceTargetMap.get(column);
                analyzedData.put(targetHeader,rowData.get(targetHeader)+"");
                analyzedData.put(column+COMPARE_COMP_POSTFIX,cVal);
                if(null!= deviation)
                    analyzedData.put(column+COMPARE_DEVIATION_POSTFIX,deviation);
                analyzedData.put(column+COMPARE_FINAL_RESULT_POSTFIX,finalResult);


            } else if (!analyzedData.containsKey(column)) {
                logger.debug(column + " is not required to be analysed");
                analyzedData.put(column, rowData.get(column)+"");
            }


        }

        return analyzedData;

    }

}
