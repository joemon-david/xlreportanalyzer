package com.etl.report.constants;

public interface ConfigData {
    enum FILE_FORMAT {XLS,XLSX}
    String EXCEL_DATE_FORMAT = "M/dd/yyyy";
    FILE_FORMAT SELECTED_FORMAT = FILE_FORMAT.XLSX;
    String COMPARE_FILE_TYPE = "FIleType";
    String COMPARE_SRC_FILE = "SourceFile";
    String COMPARE_SRC_COLUMN = "SourceColumns";
    String COMPARE_TAR_FILE = "TargetFIle";
    String COMPARE_TAR_COLUMN = "TargetColumn";
    String COMPARE_PRIMARY_KEY = "Primarykey";
    String COMPARE_TRANSF_LOGIC = "Tranformation Logic";
    String COMPARE_END_USER_ACCEPTED="EndUserAccepted";
    String COMPARE_DATA_TYPE = "Data Type";
    String COMPARE_DATA_TYPE_VAR_CHAR = "varchar";
    String COMPARE_DATA_TYPE_FLOAT = "float";
    String COMPARE_MAPPING_DIR_PATH = "data/input/compare/mapping/";
    String COMPARE_SRC_DIR_PATH = "data/input/compare/source/";
    String COMPARE_TARGET_DIR_PATH = "data/input/compare/target/";
    String COMPARE_REPORT_DIR_PATH = "data/input/compare/report/";
    String COMPARE_REPORT_OUTPUT_PATH ="data//output//FullReport.xlsx";
    String COMPARE_REPORT_SUMMARY_PATH ="data//output//summaryReport.xlsx";
    String COMPARE_REPORT_SHEET_NAME = "FULL MISMATCHES 1";
    String COMPARE_REPORT_SHEET_NAME_ADDED = "FULL MISMATCHES 1 UPDATED";
    String COMPARE_REPORT_SUMMARY_SHEET_NAME = "REPORT SUMMARY";
    String COMPARE_RESULT_PASSED = "PASS";
    String COMPARE_RESULT_FAILED= "FAIL";
    String COMPARE_RESULT_MATCHED= "MATCH";
    String COMPARE_RESULT_DIFF= "DIFF";
    String COMPARE_RESULT_KNOWN_DIFF= "KNOWN_DIFFERENCE";
    Integer COMPARE_MAX_COLUMN_IN_CONFIG_FILE = 7;
    String COMPARE_TRANS_LOGIC_TOLERANCE= "Tolerance";
    String COMPARE_TRANS_LOGIC_KNOWN_DIFF= "KnownDifference";
    String COMPARE_SOURCE_POSTFIX="_S";
    String COMPARE_TARGET_POSTFIX="_T";
    String COMPARE_COMP_POSTFIX="_C";
    String COMPARE_FINAL_RESULT_POSTFIX="_Final_Results";
    String COMPARE_DEVIATION_POSTFIX="_Deviation%";
    String COMPARE_MATCH_TRANS_COUNT_FINAL = "Match Trans Count_Final";
    String COMPARE_DIFF_TRANS_COUNT_FINAL = "Diff Trans Count_Final";
    String COMPARE_SRC_COLUMN_NULL_COUNT = "SourceColumnNullCount";
    String COMPARE_TAR_COLUMN_NULL_COUNT = "TargetColumnNullCount";
    String COMPARE_MATCH_COUNT_FINAL = "Match Count_Final";
    String COMPARE_DIFF_COUNT_FINAL = "Diff Count_Final";
    int COMPARE_MATCH_COUNT_COLUMN_INDEX=5;
    int COMPARE_DIFF_COUNT_COLUMN_INDEX=7;

}
