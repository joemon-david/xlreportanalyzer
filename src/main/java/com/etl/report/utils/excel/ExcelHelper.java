package com.etl.report.utils.excel;

import com.etl.report.constants.ConfigData;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;

public class ExcelHelper {

    public static void main(String[] args) throws IOException {
        String inputFile ="data//output//excelReport.xlsx";
        FileInputStream inStream = new FileInputStream(new File(inputFile));
        Workbook workbook = new XSSFWorkbook(inStream);
        Sheet s =workbook.createSheet("NewSheet3");
        s.createRow(0).createCell(0).setCellValue("This is a new Cell");
        inStream.close();
        FileOutputStream outputStream = new FileOutputStream(new File("data//output//excelReport2.xlsx"));
        workbook.write(outputStream);
        outputStream.close();

    }
}
