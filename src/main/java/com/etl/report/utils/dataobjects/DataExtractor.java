package com.etl.report.utils.dataobjects;

import com.etl.report.business.LogTester;
import com.etl.report.constants.ConfigData;
import com.etl.report.dto.ReportSummaryData;
import com.etl.report.utils.common.CommonUtils;
import com.etl.report.utils.common.NumericUtils;
import com.etl.report.utils.common.TypeIdentifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class DataExtractor implements ConfigData {

    private static final Logger logger = LogManager.getLogger(DataExtractor.class);
    private static DecimalFormat df = new DecimalFormat("0.00");

    public LinkedHashSet<String> getUniqueColumnList(LinkedHashMap<Integer, LinkedHashMap<String, Object>> sheetData, String headerName) {
        LinkedHashSet<String> columnData = new LinkedHashSet<>();

        sheetData.forEach((index, rowData) ->
            columnData.add(rowData.get(headerName).toString())
        );
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

    public LinkedHashMap<String, String> getConditionalColumnValueMap(LinkedHashMap<Integer, LinkedHashMap<String, Object>> sheetData, String conditionalHeaderName,
                                            String conditionalValue, String keyColumnHeader,String valColumnHeader) {
        LinkedHashMap<String, String> columnValueMap = new LinkedHashMap<String, String>();

        for (Integer rowIndex : sheetData.keySet()) {
            LinkedHashMap<String, Object> rowData = sheetData.get(rowIndex);
            if (rowData.get(conditionalHeaderName).toString().equalsIgnoreCase(conditionalValue)) {
                String key = rowData.get(keyColumnHeader)+"";
                String value = rowData.get(valColumnHeader)+"";
                columnValueMap.put(key,value);
            }
        }
        return columnValueMap;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getSrcMappingList(LinkedHashMap<Integer, LinkedHashMap<String, Object>> s, ArrayList<String> fileTypeToRunList, String targetColumn) {
        LinkedHashMap<String, LinkedHashMap<String, String>> srcTargetColumnMapList = new LinkedHashMap<String, LinkedHashMap<String, String>>();
        for (String fileType : fileTypeToRunList) {
//            LinkedHashSet<String> srcList = getConditionalColumnValueList(s, COMPARE_FILE_TYPE, fileType, COMPARE_SRC_COLUMN);
//            LinkedHashMap<String, String> columnMap = new LinkedHashMap<>();
//            srcList.forEach((key) -> {
//                String value = getConditionalColumnValue(s, COMPARE_SRC_COLUMN, key, targetColumn);
//                columnMap.put(key, value);
//            });
            LinkedHashMap<String, String> columnMap = getConditionalColumnValueMap(s,COMPARE_FILE_TYPE,fileType,COMPARE_SRC_COLUMN,targetColumn);
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

    private String replacePostfix(String text,String replace,String postfix)
    {

        String trimmed = text.substring(0,text.lastIndexOf(replace));
        return trimmed+postfix;
    }

    /**
     * This method to analyse each row of a Mismatch Report
     * @param rowData
     * @param srcTargetColumnMap
     * @param srcTransLogicMap
     * @return
     */
    public LinkedHashMap<String, String> analyzeRowAndAddResult(LinkedHashMap<String, Object> rowData,
                                                                LinkedHashMap<String, String> srcTargetColumnMap,
                                                                LinkedHashMap<String, String> srcTransLogicMap,
                                                                LinkedHashMap<String, String> dataTypeMap) {
        LinkedHashMap<String, String> analyzedData = new LinkedHashMap<String, String>();
       // Source Target columns values read from the config file need to be appended with _S and _T postfixes
        LinkedHashMap<String, String> sourceTargetMap = appendSrcTargetColumnWithPostFix(srcTargetColumnMap, false);
        LinkedHashMap<String, String> transLogicMap = appendSrcTargetColumnWithPostFix(srcTransLogicMap, true);
        LinkedHashMap<String, String> srcDataTypeMap = appendSrcTargetColumnWithPostFix(dataTypeMap, true);
        /**
         * Iterate through all the column of the Row passed and checks whether the column is to be analysed
         * and to add the comparison and final results
         */
        for (String column : rowData.keySet()) {



            if (sourceTargetMap.containsKey(column) && !analyzedData.containsKey(column)) {
//                logger.debug("Match found " + column);
                String value1 = (null!=rowData.get(column))?rowData.get(column).toString():"";
                String value2 = (null!=rowData.get(sourceTargetMap.get(column)))?rowData.get(sourceTargetMap.get(column)).toString():"";


                String cVal = null, finalResult = null, deviation = null;
                boolean isKnownDifferance = false;
                String dataType = (null==srcDataTypeMap.get(column))?"":srcDataTypeMap.get(column);
                String transLogic = transLogicMap.get(column);
                String transLogicType = CommonUtils.extractTransLogicType(transLogic);
//                logger.debug("Trans Logic Type found as "+transLogicType);

                Double allowedTolerance = 0.0;
                if (transLogicType.equalsIgnoreCase(COMPARE_TRANS_LOGIC_TOLERANCE))
                {
                    allowedTolerance = Double.parseDouble(CommonUtils.extractTransLogicValue(transLogic).toString());
//                    logger.debug("Allowed tolerance for the column "+column+"  found as "+allowedTolerance);
                }
                else if (transLogicType.equalsIgnoreCase(COMPARE_TRANS_LOGIC_KNOWN_DIFF))
                {
                    isKnownDifferance = true;
//                    logger.debug("Known Difference is set for the column "+column);
                }
                if(dataType.equalsIgnoreCase(COMPARE_DATA_TYPE_FLOAT))
                {
                    cVal = df.format(Double.parseDouble(value1) - Double.parseDouble(value2)) ;
                    Double dVal1 = Double.parseDouble(value1);
                    Double dVal2 = Double.parseDouble(value2);
                    Double diffPercentage ;
                    if( (dVal1 == 0 && dVal2 == 0) || (dVal1==0 && dVal2!=0 ) || (dVal1!=0  && dVal2 ==0) )
                        diffPercentage= 0.0;
                   else
                        diffPercentage= NumericUtils.percentageOfDifference(dVal1, dVal2);

                    deviation = df.format(diffPercentage) ;
                    finalResult = (Math.abs(diffPercentage) <= allowedTolerance || isKnownDifferance) ? COMPARE_RESULT_PASSED : COMPARE_RESULT_FAILED;
                }else if (dataType.equalsIgnoreCase(COMPARE_DATA_TYPE_VAR_CHAR))
                {
                    if (isKnownDifferance || value1.equalsIgnoreCase(value2)) {
                        cVal = COMPARE_RESULT_MATCHED;
                        deviation = null;
                        finalResult = COMPARE_RESULT_PASSED;
                    }else
                    {
                        cVal = COMPARE_RESULT_DIFF;
                        deviation = null;
                        finalResult = COMPARE_RESULT_FAILED;

                    }

                }



//                if (value1.equalsIgnoreCase(value2)) {
//                    cVal = (TypeIdentifier.getDataTypes(value1) == TypeIdentifier.DATA_TYPES.DOUBLE) ? "0.0" : COMPARE_RESULT_MATCHED;
//                    deviation = (TypeIdentifier.getDataTypes(value1) == TypeIdentifier.DATA_TYPES.DOUBLE) ? "0.0" : null;
//                    finalResult = COMPARE_RESULT_PASSED;
//                } else if (TypeIdentifier.getDataTypes(value1) == TypeIdentifier.DATA_TYPES.DOUBLE && TypeIdentifier.getDataTypes(value2) == TypeIdentifier.DATA_TYPES.DOUBLE) {
//
//                    cVal = Double.parseDouble(value1) - Double.parseDouble(value2) + "";
//                    Double diffPercentage = NumericUtils.percentageOfDifference(Double.parseDouble(value1), Double.parseDouble(value2));
//                    deviation = diffPercentage + "";
//                    finalResult = (Math.abs(diffPercentage) <= allowedTolerance || isKnownDifferance) ? COMPARE_RESULT_PASSED : COMPARE_RESULT_FAILED;
//
//                } else {
//                    if (value1.equalsIgnoreCase("Not Available") || value2.equalsIgnoreCase("Not Available")) {
//                        // Can add the logic later
//                    } else {
//                        cVal = (isKnownDifferance) ? COMPARE_RESULT_KNOWN_DIFF : COMPARE_RESULT_DIFF;
//                        deviation = (TypeIdentifier.getDataTypes(value1) == TypeIdentifier.DATA_TYPES.DOUBLE) ? "0.0" : null;
//                        finalResult = (isKnownDifferance) ? COMPARE_RESULT_PASSED : COMPARE_RESULT_FAILED;
//                    }
//                }


                analyzedData.put(column,rowData.get(column)+"");
                String targetHeader = sourceTargetMap.get(column);
                analyzedData.put(targetHeader,rowData.get(targetHeader)+"");
                String cHeader = replacePostfix(column,COMPARE_SOURCE_POSTFIX,COMPARE_COMP_POSTFIX);
                analyzedData.put(cHeader,cVal);
                if(null!= deviation)
                {
                    String deviationHeader = replacePostfix(column,COMPARE_SOURCE_POSTFIX,COMPARE_DEVIATION_POSTFIX);
                    analyzedData.put(deviationHeader,deviation);
                }
                String fResultHeader = replacePostfix(column,COMPARE_SOURCE_POSTFIX,COMPARE_FINAL_RESULT_POSTFIX);
                analyzedData.put(fResultHeader,finalResult);


            } else if (!analyzedData.containsKey(column)) {
//                logger.debug(column + " is not required to be analysed");
                analyzedData.put(column, rowData.get(column)+"");
            }


        }

        return analyzedData;

    }


    public ReportSummaryData createSummaryData(LinkedHashMap<String, String> srcTargetColumnMap , LinkedHashMap<Integer, LinkedHashMap<String, String>> outputReportData )
    {
        ReportSummaryData summaryData = new ReportSummaryData();

        for(int row:outputReportData.keySet())
        {
            LinkedHashMap<String, String> rowData = outputReportData.get(row);
            for(String srcKey:srcTargetColumnMap.keySet())
            {

                Object source = rowData.get(srcKey+COMPARE_SOURCE_POSTFIX);
                if(null == source ||  source.toString().isEmpty())
                    summaryData.sourceNullPlusOne(srcKey);
                Object target = rowData.get(srcKey+COMPARE_TARGET_POSTFIX);
                if(null == target ||  target.toString().isEmpty())
                    summaryData.targetNullPlusOne(srcKey);
                String resultColumn = srcKey+COMPARE_FINAL_RESULT_POSTFIX;
                Object result = rowData.get(resultColumn);
                if(result == null)
                {
//                logger.debug("There is no entry Corresponding  to "+resultColumn);
                    continue;
                }
                else if(result.toString().equalsIgnoreCase(COMPARE_RESULT_PASSED))
                {
                    summaryData.passPlusOne(srcKey);
                }else if(result.toString().equalsIgnoreCase(COMPARE_RESULT_FAILED))
                {
                    summaryData.failPlusOne(srcKey);
                }
            }
        }

        return summaryData;

    }

}
