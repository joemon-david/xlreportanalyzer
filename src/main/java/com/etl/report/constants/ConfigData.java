package com.etl.report.constants;

public interface ConfigData {
    enum FILE_FORMAT {XLS,XLSX};
    String EXCEL_DATE_FORMAT = "M/dd/yyyy";
    FILE_FORMAT SELECTED_FORMAT = FILE_FORMAT.XLSX;
    String COMPARE_FILE_TYPE = "FIleType";
    String COMPARE_SRC_FILE = "SourceFile";
    String COMPARE_SRC_COLUMN = "SourceColumns";
    String COMPARE_TAR_FILE = "TargetFIle";
    String COMPARE_TAR_COLUMN = "TargetColumn";
    String COMPARE_PRIMARY_KEY = "Primarykey";
    String COMPARE_TRANSF_LOGIC = "Tranformation Logic";
    String COMPARE_MAPPING_DIR_PATH = "data/input/compare/mapping/";
    String COMPARE_SRC_DIR_PATH = "data/input/compare/source/";
    String COMPARE_TARGET_DIR_PATH = "data/input/compare/target/";
    String COMPARE_REPORT_DIR_PATH = "data/input/compare/report/";
    String COMPARE_REPORT_SHEET_NAME = "FULL MISMATCHES 1";
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

}
