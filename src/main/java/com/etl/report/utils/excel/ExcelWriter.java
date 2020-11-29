package com.etl.report.utils.excel;

import com.etl.report.constants.ConfigData;
import com.etl.report.dto.ReportSummaryData;
import com.etl.report.utils.common.CommonUtils;
import com.etl.report.utils.dataobjects.DataExtractor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ExcelWriter implements ConfigData {

    private static final Logger logger = LogManager.getLogger(ExcelWriter.class);
    public  void writeDataToExcelSheet(String outPutFilePath, LinkedHashMap<Integer, LinkedHashMap<String,String>> tableData, String sheetName) throws IOException {

        Workbook workbook = new XSSFWorkbook();
        updateFullMatchDataToWorkbook(outPutFilePath, tableData, sheetName, workbook);

    }

    public void writeFullMatchesSheetToReport(String inputFilePath,String outPutFilePath, LinkedHashMap<Integer, LinkedHashMap<String,String>> tableData, String sheetName) throws IOException {


        FileInputStream inStream = new FileInputStream(new File(inputFilePath));
        Workbook workbook = new XSSFWorkbook(inStream);
        updateFullMatchDataToWorkbook(outPutFilePath, tableData, sheetName, workbook);
        inStream.close();
        return;

    }

    public void editReportSummaryPageWithAnalyzeData(String inputFilePath,String outPutFilePath, LinkedHashMap<String, String> srcTransLogicMap, ReportSummaryData summaryData) throws IOException {
        FileInputStream inStream = new FileInputStream(new File(inputFilePath));
        Workbook workbook = new XSSFWorkbook(inStream);
        Map<String, CellStyle> styles = createStyles(workbook);
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        Sheet datatypeSheet = workbook.getSheet(COMPARE_REPORT_SUMMARY_SHEET_NAME);
        int rowNum =0;
        for (Row currentRow : datatypeSheet)
        {
            int lastIndex =currentRow.getLastCellNum();
            if(rowNum ==0)
            {
                rowNum++;
                continue;
            }else if(rowNum ==1)
            {


                currentRow.createCell(lastIndex).setCellValue(COMPARE_TRANS_LOGIC_TOLERANCE);
                currentRow.getCell(lastIndex).setCellStyle(styles.get("header_bright_green"));
                datatypeSheet.setColumnWidth(lastIndex,5000);
                currentRow.createCell(lastIndex+1).setCellValue(COMPARE_TRANS_LOGIC_KNOWN_DIFF);
                currentRow.getCell(lastIndex+1).setCellStyle(styles.get("header_bright_green"));
                datatypeSheet.setColumnWidth(lastIndex+1,5000);
                currentRow.createCell(lastIndex+2).setCellValue(COMPARE_MATCH_COUNT_FINAL);
                currentRow.getCell(lastIndex+2).setCellStyle(styles.get("header_bright_green"));
                datatypeSheet.setColumnWidth(lastIndex+2,5000);
                currentRow.createCell(lastIndex+3).setCellValue(COMPARE_DIFF_COUNT_FINAL);
                currentRow.getCell(lastIndex+3).setCellStyle(styles.get("header_bright_green"));
                datatypeSheet.setColumnWidth(lastIndex+3,5000);
                currentRow.createCell(lastIndex+4).setCellValue(COMPARE_SRC_COLUMN_NULL_COUNT);
                currentRow.getCell(lastIndex+4).setCellStyle(styles.get("header_bright_green"));
                datatypeSheet.setColumnWidth(lastIndex+4,5000);
                currentRow.createCell(lastIndex+5).setCellValue(COMPARE_TAR_COLUMN_NULL_COUNT);
                currentRow.getCell(lastIndex+5).setCellStyle(styles.get("header_bright_green"));
                datatypeSheet.setColumnWidth(lastIndex+5,5000);

            }else
            {
                String srcKey = currentRow.getCell(1).getStringCellValue();
                if(null==srcKey || srcKey.isEmpty())
                {
                    logger.debug("The Source Column in the Report data at Row Number "+rowNum+" is Empty So breaking the further analysis");
                    break;
                }
                String transLogic = srcTransLogicMap.get(srcKey);
                String transLogicType = CommonUtils.extractTransLogicType(transLogic);
                Object allowedTolerance = null,isKnownDifferance=null;
                if (transLogicType.equalsIgnoreCase(COMPARE_TRANS_LOGIC_TOLERANCE))
                    allowedTolerance = Double.parseDouble(CommonUtils.extractTransLogicValue(transLogic).toString());
                else if (transLogicType.equalsIgnoreCase(COMPARE_TRANS_LOGIC_KNOWN_DIFF))
                    isKnownDifferance = true;


                currentRow.createCell(lastIndex).setCellValue(checkNull(allowedTolerance));
                currentRow.createCell(lastIndex+1).setCellValue(checkNull(isKnownDifferance));
                long totalMatchCount = (summaryData.getTotalMatchCountMap().containsKey(srcKey))?summaryData.getTotalMatchCountMap().get(srcKey):0;
                currentRow.createCell(lastIndex+2).setCellValue(totalMatchCount);
                long totalDiffCount = (summaryData.getTotalDiffCountMap().containsKey(srcKey))?summaryData.getTotalDiffCountMap().get(srcKey):0;
                currentRow.createCell(lastIndex+3).setCellValue(totalDiffCount);
                currentRow.createCell(lastIndex+4).setCellValue("NA");
                currentRow.createCell(lastIndex+5).setCellValue("NA");
            }
            rowNum++;

        }
        inStream.close();
        FileOutputStream fo = new FileOutputStream(outPutFilePath);
        workbook.write(fo);
        workbook.close();
        fo.close();
    }

    private String checkNull(Object value)
    {
        String formattedValue="";
         if(null!=value)
         {
             if(value instanceof Boolean)
                 formattedValue ="YES";
             else
                 formattedValue = value+"";
         }
        return formattedValue;
    }



    private void updateFullMatchDataToWorkbook(String outPutFilePath, LinkedHashMap<Integer, LinkedHashMap<String, String>> tableData, String sheetName, Workbook workbook) throws IOException {
        Sheet sheet = workbook.createSheet(sheetName);
        Map<String, CellStyle> styles = createStyles(workbook);

        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(25.75f);
        int headerIndex = 0;

        for(String key:tableData.get(0).keySet())
        {
            Cell cell = headerRow.createCell(headerIndex);
            cell.setCellValue(key);
//            int mod = headerIndex% 5;
            if(key.endsWith(COMPARE_SOURCE_POSTFIX))//mod == 1
                cell.setCellStyle(styles.get("header_light_orange"));
            else if(key.endsWith(COMPARE_TARGET_POSTFIX))//mod == 2
                cell.setCellStyle(styles.get("header_blue"));
            else if(key.endsWith(COMPARE_COMP_POSTFIX))//mod == 3
                cell.setCellStyle(styles.get("header_green"));
            else if(key.endsWith(COMPARE_DEVIATION_POSTFIX))//mod == 4
                cell.setCellStyle(styles.get("header_pink"));
            else if(key.endsWith(COMPARE_FINAL_RESULT_POSTFIX))//mod == 0
                cell.setCellStyle(styles.get("header_bright_green"));
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
        fo.close();
    }

    /**
     * create a library of cell styles
     */
    private  Map<String, CellStyle> createStyles(Workbook wb){
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
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(headerFont);
        ((XSSFCellStyle) style).setFillForegroundColor(new XSSFColor(new java.awt.Color(175, 77, 236)));
        styles.put("header_pink", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(headerFont);
        ((XSSFCellStyle) style).setFillForegroundColor(new XSSFColor(new java.awt.Color(139, 241, 67)));
        styles.put("header_bright_green", style);

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
