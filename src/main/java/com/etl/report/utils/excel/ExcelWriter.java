package com.etl.report.utils.excel;

import com.etl.report.constants.ConfigData;
import com.etl.report.dto.ReportSummaryData;
import com.etl.report.utils.common.CommonUtils;
import com.etl.report.utils.common.TypeIdentifier;
import com.etl.report.utils.dataobjects.DataExtractor;
import com.monitorjbl.xlsx.StreamingReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ExcelWriter implements ConfigData {

    private static final Logger logger = LogManager.getLogger(ExcelWriter.class);
    ExcelHelper helper = new ExcelHelper();
    public  void writeDataToExcelSheet(String outPutFilePath, LinkedHashMap<Integer, LinkedHashMap<String,String>> tableData, String sheetName) throws IOException {

        Workbook workbook = new XSSFWorkbook();
        updateFullMatchDataToWorkbook(outPutFilePath, tableData, sheetName, workbook);

    }

    public void writeFullMatchesSheetToReport(String inputFilePath,String outPutFilePath, LinkedHashMap<Integer, LinkedHashMap<String,String>> tableData, String sheetName) throws IOException {


//        FileInputStream inStream = new FileInputStream(new File(inputFilePath));
//        Workbook workbook = new XSSFWorkbook(inStream);
        Workbook workbook = new SXSSFWorkbook(100);
//        InputStream in = new FileInputStream(new File(inputFilePath));
//        Workbook workbook = StreamingReader.builder()
//                .rowCacheSize(100)
//                .bufferSize(4096)
//                .open(in);

        updateFullMatchDataToWorkbook(outPutFilePath, tableData, sheetName, workbook);
//        in.close();
        return;

    }

    private SXSSFWorkbook createSXSSFWorkbookFromFile(Workbook workbook)
    {
        SXSSFWorkbook wb = new SXSSFWorkbook(100);
        ExcelHelper helper = new ExcelHelper();
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

        for(Sheet sheet:workbook)
        {
            Sheet sh = wb.createSheet(sheet.getSheetName());
            for(int rowNum=0; rowNum<=sheet.getLastRowNum();rowNum++)
            {

                Row row = sheet.getRow(rowNum);
                if(row==null)
                    break;
                Row rw = sh.createRow(rowNum);
                for(int cellNo=0;cellNo<=row.getLastCellNum();cellNo++)
                {
                    Cell currentCell = row.getCell(cellNo);
                    Cell newCell = rw.createCell(cellNo);

                    String newCellValue = (currentCell==null)?"":helper.getCellValueAsString(currentCell,evaluator);

                }

            }

        }

        return wb;
    }

    public void editReportSummaryPageWithAnalyzeData(String inputFilePath, String outPutFilePath, LinkedHashMap<String, String> srcTransLogicMap, ReportSummaryData summaryData, LinkedHashMap<String, String> endUserAcceptedMap) throws IOException {
        FileInputStream inStream = new FileInputStream(new File(inputFilePath));
        Workbook workbook = new XSSFWorkbook(inStream);

//        Workbook workbook = createSXSSFWorkbookFromFile(new XSSFWorkbook(inStream));
        Map<String, CellStyle> styles = createStyles(workbook);
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        Sheet datatypeSheet = workbook.getSheet(COMPARE_REPORT_SUMMARY_SHEET_NAME);
        logger.debug("Iterating through each Row on "+COMPARE_REPORT_SUMMARY_SHEET_NAME+" sheet");

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
                currentRow.createCell(lastIndex+2).setCellValue(COMPARE_END_USER_ACCEPTED);
                currentRow.getCell(lastIndex+2).setCellStyle(styles.get("header_bright_green"));
                datatypeSheet.setColumnWidth(lastIndex+2,5000);
                currentRow.createCell(lastIndex+3).setCellValue(COMPARE_MATCH_TRANS_COUNT_FINAL);
                currentRow.getCell(lastIndex+3).setCellStyle(styles.get("header_bright_green"));
                datatypeSheet.setColumnWidth(lastIndex+3,6000);
                currentRow.createCell(lastIndex+4).setCellValue(COMPARE_DIFF_TRANS_COUNT_FINAL);
                currentRow.getCell(lastIndex+4).setCellStyle(styles.get("header_bright_green"));
                datatypeSheet.setColumnWidth(lastIndex+4,6000);
                currentRow.createCell(lastIndex+5).setCellValue(COMPARE_SRC_COLUMN_NULL_COUNT);
                currentRow.getCell(lastIndex+5).setCellStyle(styles.get("header_bright_green"));
                datatypeSheet.setColumnWidth(lastIndex+5,5000);
                currentRow.createCell(lastIndex+6).setCellValue(COMPARE_TAR_COLUMN_NULL_COUNT);
                currentRow.getCell(lastIndex+6).setCellStyle(styles.get("header_bright_green"));
                datatypeSheet.setColumnWidth(lastIndex+6,5000);

                currentRow.createCell(lastIndex+7).setCellValue(COMPARE_MATCH_COUNT_FINAL);
                currentRow.getCell(lastIndex+7).setCellStyle(styles.get("header_bright_green"));
                datatypeSheet.setColumnWidth(lastIndex+7,5000);
                currentRow.createCell(lastIndex+8).setCellValue(COMPARE_DIFF_COUNT_FINAL);
                currentRow.getCell(lastIndex+8).setCellStyle(styles.get("header_bright_green"));
                datatypeSheet.setColumnWidth(lastIndex+8,5000);

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
                String endUserAccepted = endUserAcceptedMap.containsKey(srcKey)?endUserAcceptedMap.get(srcKey):"";



                currentRow.createCell(lastIndex).setCellValue(checkNull(allowedTolerance));
                currentRow.createCell(lastIndex+1).setCellValue(checkNull(isKnownDifferance));
                currentRow.createCell(lastIndex+2).setCellValue(checkNull(endUserAccepted));
                long totalMatchCount = (summaryData.getTotalMatchCountMap().containsKey(srcKey))?summaryData.getTotalMatchCountMap().get(srcKey):0;
                currentRow.createCell(lastIndex+3).setCellValue(totalMatchCount);
                long totalDiffCount = (summaryData.getTotalDiffCountMap().containsKey(srcKey))?summaryData.getTotalDiffCountMap().get(srcKey):0;
                currentRow.createCell(lastIndex+4).setCellValue(totalDiffCount);
                String srcNullCount = (null ==summaryData.getTotalSourceNullCountMap().get(srcKey))?"0":summaryData.getTotalSourceNullCountMap().get(srcKey)+"";
                currentRow.createCell(lastIndex+5).setCellValue(srcNullCount);
                String targetNullCount =(null == summaryData.getTotalTargetNullCountMap().get(srcKey))?"0":summaryData.getTotalTargetNullCountMap().get(srcKey)+"";
                currentRow.createCell(lastIndex+6).setCellValue(targetNullCount);

                // To extract the total number of matches exists on the report
                String existing_total_match=helper.getCellValueAsString(currentRow.getCell(COMPARE_MATCH_COUNT_COLUMN_INDEX),evaluator);
                Long exist_tot_match = (null!=existing_total_match && TypeIdentifier.getDataTypes(existing_total_match) == TypeIdentifier.DATA_TYPES.DOUBLE)?Long.parseLong(existing_total_match):0;

                // To extract the total number of Difference exists on the report
                String existing_total_diff=helper.getCellValueAsString(currentRow.getCell(COMPARE_DIFF_COUNT_COLUMN_INDEX),evaluator);
                Long exist_tot_diff = (null!=existing_total_diff && TypeIdentifier.getDataTypes(existing_total_diff) == TypeIdentifier.DATA_TYPES.DOUBLE)?Long.parseLong(existing_total_diff):0;

                /**
                 * We may do some check on the existing total match and the total match after this analysis . The new match count
                 * supposed to be greater than or equal to the existing match count due to Known Difference or applying Tolerance.
                 * Right now we will be checking the diff values. After applying the tolerances the different count should be decresed
                 * We can take that deviation and apply to the existing total value , so the formula assumed as follows
                 * grand_total_match_count = existing_total_count + (existing_total_diff - total_diff_count)
                 * grand_total_diff_count = total_diff_count
                 */



                long grandTotalMatchCount = exist_tot_match + (exist_tot_diff-totalDiffCount);
                currentRow.createCell(lastIndex+7).setCellValue(grandTotalMatchCount);
                long grandTotalDiffCount = totalDiffCount;
                currentRow.createCell(lastIndex+8).setCellValue(grandTotalDiffCount);
            }
            rowNum++;

        }

        logger.debug("Completed the Iteration through each Row on "+COMPARE_REPORT_SUMMARY_SHEET_NAME+" sheet");
        inStream.close();
        FileOutputStream fo = new FileOutputStream(outPutFilePath);
        logger.debug("Start writing the summary report into the physical file");
        workbook.write(fo);
        workbook.close();
        fo.close();
        logger.debug("Completed  writing the summary report into the physical file");
    }


    private Row copyRowData(Sheet newSheet,int rowNum,Row existingRow,FormulaEvaluator evaluator)
    {

        Row newRow = newSheet.createRow(rowNum);
        int CellNo=0;
        for(Cell cell:existingRow)
        {
            Cell newCell = newRow.createCell(CellNo,cell.getCellType());
            String cValue = helper.getCellValueAsString(cell,evaluator);
            newCell.setCellValue(cValue);
            CellNo++;
        }

        return newRow;
    }

    public void editReportSummaryPageWithAnalyzeDataNew(String inputFilePath, String outPutFilePath, LinkedHashMap<String, String> srcTransLogicMap, ReportSummaryData summaryData, LinkedHashMap<String, String> endUserAcceptedMap) throws IOException {
        FileInputStream inStream = new FileInputStream(new File(inputFilePath));
        Workbook workbookIn = new XSSFWorkbook(inStream);

        Workbook workbook = new SXSSFWorkbook();
        Map<String, CellStyle> styles = createStyles(workbook);
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        Sheet datatypeSheet = workbookIn.getSheet(COMPARE_REPORT_SUMMARY_SHEET_NAME);
        Sheet sheet = workbook.createSheet(COMPARE_REPORT_SUMMARY_SHEET_NAME);
        logger.debug("Iterating through each Row on "+COMPARE_REPORT_SUMMARY_SHEET_NAME+" sheet");

        int rowNum =0;
        for (Row row : datatypeSheet)
        {

            Row currentRow = copyRowData(sheet,rowNum,row,evaluator);
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
                currentRow.createCell(lastIndex+2).setCellValue(COMPARE_END_USER_ACCEPTED);
                currentRow.getCell(lastIndex+2).setCellStyle(styles.get("header_bright_green"));
                datatypeSheet.setColumnWidth(lastIndex+2,5000);
                currentRow.createCell(lastIndex+3).setCellValue(COMPARE_MATCH_COUNT_FINAL);
                currentRow.getCell(lastIndex+3).setCellStyle(styles.get("header_bright_green"));
                datatypeSheet.setColumnWidth(lastIndex+3,5000);
                currentRow.createCell(lastIndex+4).setCellValue(COMPARE_DIFF_COUNT_FINAL);
                currentRow.getCell(lastIndex+4).setCellStyle(styles.get("header_bright_green"));
                datatypeSheet.setColumnWidth(lastIndex+4,5000);
                currentRow.createCell(lastIndex+5).setCellValue(COMPARE_SRC_COLUMN_NULL_COUNT);
                currentRow.getCell(lastIndex+5).setCellStyle(styles.get("header_bright_green"));
                datatypeSheet.setColumnWidth(lastIndex+5,5000);
                currentRow.createCell(lastIndex+6).setCellValue(COMPARE_TAR_COLUMN_NULL_COUNT);
                currentRow.getCell(lastIndex+6).setCellStyle(styles.get("header_bright_green"));
                datatypeSheet.setColumnWidth(lastIndex+6,5000);

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
                String endUserAccepted = endUserAcceptedMap.containsKey(srcKey)?endUserAcceptedMap.get(srcKey):"";



                currentRow.createCell(lastIndex).setCellValue(checkNull(allowedTolerance));
                currentRow.createCell(lastIndex+1).setCellValue(checkNull(isKnownDifferance));
                currentRow.createCell(lastIndex+2).setCellValue(checkNull(endUserAccepted));
                long totalMatchCount = (summaryData.getTotalMatchCountMap().containsKey(srcKey))?summaryData.getTotalMatchCountMap().get(srcKey):0;
                currentRow.createCell(lastIndex+3).setCellValue(totalMatchCount);
                long totalDiffCount = (summaryData.getTotalDiffCountMap().containsKey(srcKey))?summaryData.getTotalDiffCountMap().get(srcKey):0;
                currentRow.createCell(lastIndex+4).setCellValue(totalDiffCount);
                currentRow.createCell(lastIndex+5).setCellValue("NA");
                currentRow.createCell(lastIndex+6).setCellValue("NA");
            }
            rowNum++;

        }

        logger.debug("Completed the Iteration through each Row on "+COMPARE_REPORT_SUMMARY_SHEET_NAME+" sheet");
        inStream.close();
        FileOutputStream fo = new FileOutputStream(outPutFilePath);
        logger.debug("Start writing the summary report into the physical file");
        workbook.write(fo);
        workbook.close();
        fo.close();
        logger.debug("Completed  writing the summary report into the physical file");
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
            if(key==null)
                continue;

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

        int totalRows = tableData.size();
        int iteration = totalRows/10;
        logger.debug("totalRows - "+totalRows+", iteration "+iteration);
        for(int i =1;i<11;i++)
        {
            int startIndex = (iteration*i)-iteration;
            int endIndex = (i==10)?(iteration*i)+(totalRows%10):(iteration*i);

            logger.debug("startIndex - "+startIndex+", endIndex "+endIndex);
            for(int index = startIndex;index<endIndex;index++)
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
        }

//        for(int index: tableData.keySet())
//        {
//            Row rw = sheet.createRow(index+1);
//            HashMap<String,String> data = tableData.get(index);
//            AtomicInteger cellIndex= new AtomicInteger();
//            data.forEach((key,value) -> {
//                Cell cw = rw.createCell(cellIndex.getAndIncrement());
//                cw.setCellValue(value);
//                if(value.equalsIgnoreCase(COMPARE_RESULT_PASSED))
//                    cw.setCellStyle(styles.get("cell_passed"));
//                else if(value.equalsIgnoreCase(COMPARE_RESULT_FAILED))
//                    cw.setCellStyle(styles.get("cell_failed"));
//                else
//                    cw.setCellStyle(styles.get("cell_normal_centered"));
//
//            });
//
//
//        }

        FileOutputStream fo = new FileOutputStream(outPutFilePath);
        logger.debug("Beginning to write the workbook ......");
        workbook.write(fo);
        logger.debug("Completed writing the workbook!!!!");
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
