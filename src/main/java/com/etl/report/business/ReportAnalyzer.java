package com.etl.report.business;

import com.etl.report.constants.ConfigData;
import com.etl.report.utils.dataobjects.DataExtractor;
import com.etl.report.utils.excel.ExcelReader;
import com.etl.report.utils.excel.ExcelWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class ReportAnalyzer implements ConfigData {

    public static void main(String[] args) throws IOException {
        new ReportAnalyzer().analyzeReportAndCreateFinalSummary("Mapping.xlsx","Config","Positions");
    }

    public void analyzeReportAndCreateFinalSummary(String mappingFile,String sheetName,String fileTypeToRun) throws IOException {
        DataExtractor extractor = new DataExtractor();
        ExcelReader xlReader = new ExcelReader();
        String mappingFilePath = COMPARE_MAPPING_DIR_PATH+mappingFile;
        //mapping data object is created by reading the mapping and other configuration from the mapping xlsx file.
        LinkedHashMap<Integer, LinkedHashMap<String, Object>> mappingData = xlReader.readAllDataFromExcelFile(mappingFilePath,sheetName,15);

        ArrayList<String> fileTypeToRunList = new ArrayList<>();
        LinkedHashSet<String> fileTypeList = extractor.getUniqueColumnList(mappingData,COMPARE_FILE_TYPE);
        /**
         * The application can support multiple FileTypes to be run , also user can pass a fileType value
         * to execute only selective file Type - One or more File Type ( comma separated ) can be passed and
         * if need to run all the File Type we can just pass the fileType as 'All'
         * ***With the current logic it is expected that the source column having unique column names across all the fileTypes.
         * ***If there are duplicate column names in the configuration sheet , it may not work as expected.
         */
        if(fileTypeToRun.equalsIgnoreCase("All"))
            fileTypeToRunList.addAll(fileTypeList);
        else if (fileTypeToRun.split(",").length>1)
            fileTypeToRunList.addAll(Arrays.asList(fileTypeToRun.split(",")));
        else
            fileTypeToRunList.add(fileTypeToRun);

        /**
         * srcTargetColumnMapList contains the mapping between the source and target excel columns of all the fileTypes.
         * srcTransLogicMapList contains the transformation logic used in each column in all the file types.
         * Both of these Map have the fileType as key.
         */
        LinkedHashMap<String, LinkedHashMap<String, String>> srcTargetColumnMapList = extractor.getSrcMappingList(mappingData, fileTypeToRunList,COMPARE_TAR_COLUMN);
        LinkedHashMap<String,LinkedHashMap<String, String>> srcTransLogicMapList = extractor.getSrcMappingList(mappingData, fileTypeToRunList,COMPARE_TRANSF_LOGIC);

        for(String fileType:fileTypeToRunList)
        {
            LinkedHashMap<String, String> srcTargetColumnMap = srcTargetColumnMapList.get(fileType);
            LinkedHashMap<String, String> srcTransLogicMap = srcTransLogicMapList.get(fileType);
            String reportFilePath = COMPARE_REPORT_DIR_PATH+extractor.getConditionalColumnValue(mappingData,COMPARE_FILE_TYPE,fileType,COMPARE_SRC_FILE);
            LinkedHashMap<Integer, LinkedHashMap<String, Object>> reportData = xlReader.readAllDataFromExcelFile(reportFilePath,COMPARE_REPORT_SHEET_NAME,200);
            LinkedHashMap<Integer, LinkedHashMap<String, String>> outputReportData = new LinkedHashMap<Integer, LinkedHashMap<String, String>>();
            for(Integer rowNumber:reportData.keySet())
            {
                LinkedHashMap<String, Object> existingData = reportData.get(rowNumber);
                LinkedHashMap<String, String> analyzedData = extractor.analyzeRowAndAddResult(existingData,srcTargetColumnMap,srcTransLogicMap);
                outputReportData.put(rowNumber,analyzedData);
            }
            new ExcelWriter().writeDataToExcelSheet("data//output//excelReport.xlsx",outputReportData,fileType);

        }

    }
}