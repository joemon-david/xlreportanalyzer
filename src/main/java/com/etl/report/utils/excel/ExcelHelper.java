package com.etl.report.utils.excel;

import com.etl.report.constants.ConfigData;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.SimpleDateFormat;

public class ExcelHelper implements ConfigData {
    private static final SimpleDateFormat sdf = new SimpleDateFormat(EXCEL_DATE_FORMAT);


    public String getCellValueAsString (Cell currentCell,FormulaEvaluator evaluator)
    {
        String sValue="";
        if (currentCell.getCellType() == CellType.STRING) {
            sValue = currentCell.getStringCellValue();

        } else if (currentCell.getCellType() == CellType.NUMERIC) {
            if (HSSFDateUtil.isCellDateFormatted(currentCell)) {
                String dtValue = sdf.format(currentCell.getDateCellValue());
            } else {
                long lng = (long) currentCell.getNumericCellValue();
                sValue=lng+"";
//
            }
        }else if (currentCell.getCellType() == CellType.BOOLEAN)
        {
            sValue=currentCell.getBooleanCellValue()+"";
        }
        else if(currentCell.getCellType() == CellType.FORMULA)
        {
            sValue = getFormulaValue(evaluator,currentCell);

//                        System.out.println("Cell Type found as Formula for "+currentCell);
//                        rowDataMap.put(headerMap.get(columnNumber),getFormulaValue(evaluator,currentCell));
        }

        return sValue;
    }

    private  String getFormulaValue(FormulaEvaluator evaluator, Cell cell)
    {
        CellType cellType ;
        try {
            cellType = evaluator.evaluateFormulaCell(cell);
        } catch (Exception e ) {
//            System.out.println(e.getCause());
//            return cell.getRichStringCellValue().getString();
            return "";
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

    public static void main(String[] args) throws IOException {
        String inputFile ="Custom_SS";
        String prefix = "_S";
        String replaceW ="_FinalResult";

        System.out.println(inputFile.replace(prefix,replaceW));

        String trimmed = inputFile.substring(0,inputFile.lastIndexOf(prefix));
        System.out.println(trimmed);
        System.out.println(trimmed+replaceW);



    }
}
