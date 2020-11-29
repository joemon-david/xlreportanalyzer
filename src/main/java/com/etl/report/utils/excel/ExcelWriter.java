package com.etl.report.utils.excel;

import com.etl.report.constants.ConfigData;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ExcelWriter implements ConfigData {

    public  void writeDataToExcelSheet(String outPutFilePath, LinkedHashMap<Integer, LinkedHashMap<String,String>> tableData, String sheetName) throws IOException {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);
        Map<String, CellStyle> styles = createStyles(workbook);



        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(25.75f);
        int headerIndex = 0;




        for(String key:tableData.get(1).keySet())
        {
            Cell cell = headerRow.createCell(headerIndex);
            cell.setCellValue(key);
            int mod = headerIndex% 3;
            if(mod == 1)
                cell.setCellStyle(styles.get("header_blue"));
            else if(mod==2)
                cell.setCellStyle(styles.get("header_green"));
            else if(mod==0)
                cell.setCellStyle(styles.get("header_light_orange"));
            sheet.setColumnWidth(headerIndex,10000);
            headerIndex++;
        }



        for(int index: tableData.keySet())
        {
            Row rw = sheet.createRow(index+1);
            HashMap<String,String> data = tableData.get(index);
            AtomicInteger cellIndex= new AtomicInteger();
            data.forEach((key,value) -> {
                Cell cw = rw.createCell(cellIndex.getAndIncrement());
                cw.setCellValue(value);
                if(value.equalsIgnoreCase(COMPARE_RESULT_PASSED))
                    cw.setCellStyle(styles.get("cell_passed"));
                else if(value.equalsIgnoreCase(COMPARE_RESULT_FAILED))
                    cw.setCellStyle(styles.get("cell_failed"));
                else
                    cw.setCellStyle(styles.get("cell_normal_centered"));

            });


        }

        FileOutputStream fo = new FileOutputStream(outPutFilePath);
        workbook.write(fo);
        workbook.close();

    }

    /**
     * create a library of cell styles
     */
    private static Map<String, CellStyle> createStyles(Workbook wb){
        Map<String, CellStyle> styles = new HashMap<>();
        DataFormat df = wb.createDataFormat();

        CellStyle style;
        Font headerFont = wb.createFont();
        headerFont.setBold(true);
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        ((XSSFCellStyle) style).setFillForegroundColor(new XSSFColor(new java.awt.Color(133, 187, 217)));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(headerFont);
        styles.put("header_blue", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(headerFont);
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        styles.put("header_green", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(headerFont);
//        style.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        ((XSSFCellStyle) style).setFillForegroundColor(new XSSFColor(new java.awt.Color(252, 194, 3)));
        styles.put("header_light_orange", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(headerFont);
        style.setDataFormat(df.getFormat("d-mmm"));
        styles.put("header_date", style);

        Font font1 = wb.createFont();
        font1.setBold(true);
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setFont(font1);
        styles.put("cell_b", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFont(font1);
        styles.put("cell_b_centered", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setFont(font1);
        style.setDataFormat(df.getFormat("d-mmm"));
        styles.put("cell_b_date", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setFont(font1);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setDataFormat(df.getFormat("d-mmm"));
        styles.put("cell_g", style);

        Font font2 = wb.createFont();
        font2.setColor(IndexedColors.BLUE.getIndex());
        font2.setBold(true);
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setFont(font2);
        styles.put("cell_bb", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setFont(font1);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setDataFormat(df.getFormat("d-mmm"));
        styles.put("cell_bg", style);

        Font font3 = wb.createFont();
        font3.setFontHeightInPoints((short)14);
        font3.setColor(IndexedColors.DARK_BLUE.getIndex());
        font3.setBold(true);
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setFont(font3);
        style.setWrapText(true);
        styles.put("cell_h", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setWrapText(true);
        styles.put("cell_normal", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setWrapText(true);
        styles.put("cell_normal_centered", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setWrapText(true);
        style.setDataFormat(df.getFormat("d-mmm"));
        styles.put("cell_normal_date", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setIndention((short)1);
        style.setWrapText(true);
        styles.put("cell_indented", style);

        style = createBorderedStyle(wb);
        style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put("cell_blue", style);

        style = createBorderedStyle(wb);
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put("cell_passed", style);

        style = createBorderedStyle(wb);
//        style.setFillForegroundColor(IndexedColors.RED.getIndex());
        ((XSSFCellStyle) style).setFillForegroundColor(new XSSFColor(new java.awt.Color(250, 79, 36)));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put("cell_failed", style);

        return styles;
    }

    private static CellStyle createBorderedStyle(Workbook wb){
//        BorderStyle thin = BorderStyle.THIN;
        BorderStyle thick = BorderStyle.THICK;
//        short black = IndexedColors.BLACK.getIndex();
        short aqua = IndexedColors.AQUA.getIndex();

        CellStyle style = wb.createCellStyle();
        style.setBorderRight(thick);
        style.setRightBorderColor(aqua);
        style.setBorderBottom(thick);
        style.setBottomBorderColor(aqua);
        style.setBorderLeft(thick);
        style.setLeftBorderColor(aqua);
        style.setBorderTop(thick);
        style.setTopBorderColor(aqua);
        return style;
    }
}
