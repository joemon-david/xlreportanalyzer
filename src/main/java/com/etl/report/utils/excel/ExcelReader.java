package com.etl.report.utils.excel;

import com.etl.report.constants.ConfigData;
import com.etl.report.utils.common.TypeIdentifier;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;

public class ExcelReader implements ConfigData {
    private static final SimpleDateFormat sdf = new SimpleDateFormat(EXCEL_DATE_FORMAT);

    private  String getFormulaValue(FormulaEvaluator evaluator, Cell cell)
    {
        CellType cellType = null;
        try {
            cellType = evaluator.evaluateFormulaCell(cell);
        } catch (FormulaParseException e) {
//            System.out.println(e.getCause());
            return cell.getRichStringCellValue().getString();
        }

        switch (cellType) {
            case NUMERIC:
                if (HSSFDateUtil.isCellDateFormatted(cell))
                    return sdf.format(cell.getDateCellValue());
                else
                    return ""+cell.getNumericCellValue();
            case STRING:
                return cell.getRichStringCellValue().getString();

            case BOOLEAN:
                return cell.getBooleanCellValue() ? "TRUE" : "FALSE";
            case BLANK:
                return "";
            case ERROR:
                return FormulaError.forInt(cell.getErrorCellValue()).getString();
            default:
                throw new RuntimeException("Unexpected celltype (" + cellType + ")");
        }
    }

    public  LinkedHashMap<Integer, LinkedHashMap<String,Object>> readAllDataFromExcelFile(String filePath, String sheetName, int maxCellToCheck)
    {
        LinkedHashMap<Integer,LinkedHashMap<String,Object>> excelDataMap = null;
        try {

            FileInputStream excelFile = new FileInputStream(new File(filePath));

            Workbook workbook = (SELECTED_FORMAT == FILE_FORMAT.XLSX)?new XSSFWorkbook(excelFile) :new HSSFWorkbook(excelFile);

            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            excelDataMap = new LinkedHashMap<>();
            LinkedHashMap<Integer,String> headerMap = new LinkedHashMap<>();
            int recordNumber=0;

            Sheet datatypeSheet = workbook.getSheet(sheetName);

            for (Row currentRow : datatypeSheet) {

                LinkedHashMap<String, Object> rowDataMap = new LinkedHashMap<>();

                for (int columnNumber =0;columnNumber<maxCellToCheck;columnNumber++) {

                    Cell currentCell = currentRow.getCell(columnNumber);
                    if(null==currentCell)
                    {
                        rowDataMap.put(headerMap.get(columnNumber),"");
                        continue;
                    }





                    if (currentCell.getCellType() == CellType.STRING) {
                        String sValue = currentCell.getStringCellValue();
                        if (recordNumber == 0)
                            headerMap.put(columnNumber, sValue);
                        else
                            rowDataMap.put(headerMap.get(columnNumber), sValue);
                    } else if (currentCell.getCellType() == CellType.NUMERIC) {
                        if (HSSFDateUtil.isCellDateFormatted(currentCell)) {
                            String dtValue = sdf.format(currentCell.getDateCellValue());
                            rowDataMap.put(headerMap.get(columnNumber), dtValue);
                        } else {
                            long lng = (long) currentCell.getNumericCellValue();
                            rowDataMap.put(headerMap.get(columnNumber), lng);
//
                        }
                    }else if (currentCell.getCellType() == CellType.BOOLEAN)
                    {
                        rowDataMap.put(headerMap.get(columnNumber),currentCell.getBooleanCellValue());
                    }
                    else if(currentCell.getCellType() == CellType.FORMULA)
                    {
                        rowDataMap.put(headerMap.get(columnNumber),getFormulaValue(evaluator,currentCell));
                    }else if (currentCell.getCellType() == CellType.BLANK)
                    {
                        rowDataMap.put(headerMap.get(columnNumber),"");
                    }


                }
                if (recordNumber > 0)
                    excelDataMap.put(recordNumber, rowDataMap);
                recordNumber++;

            }

            System.out.println("Scanning Completed ->Total Records "+recordNumber);
            System.out.print("Header values -> ");
            headerMap.forEach((key,value)-> System.out.print(value+","));
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return excelDataMap;

    }

    public static void main(String[] args) {

        String masterFilePath = COMPARE_REPORT_DIR_PATH+"JumboReport_GetAccountPosition_23Oct2020.xlsx";
        String masterSheetName = "FULL MISMATCHES 1";
        LinkedHashMap<Integer, LinkedHashMap<String, Object>> s = new ExcelReader().readAllDataFromExcelFile(masterFilePath,masterSheetName,200);
//        for(Integer index:s.keySet()){
//            LinkedHashMap<String, Object> rowData = s.get(index);
//            rowData.forEach((key,value)-> System.out.println(key+" has data type as "+ TypeIdentifier.getDataTypes(value.toString())));
//            break;
//                }

    }

}
