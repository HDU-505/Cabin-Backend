package com.hdu.utils;

import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.List;

public class CsvUtils {

    /**
     * 将对象列表转换为 CSV 格式的字节数组
     *
     * @param data    要转换的对象列表
     * @param clazz   对象类型
     * @param headers CSV 文件的标题行
     * @param <T>     对象类型
     * @return CSV 文件的字节数组
     */
    public static <T> byte[] generateCsvBytes(List<T> data, Class<T> clazz, String[] headers) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(baos);
             CSVWriter csvWriter = new CSVWriter(writer)) {
            ColumnPositionMappingStrategy<T> mappingStrategy = new ColumnPositionMappingStrategy<>();
            mappingStrategy.setType(clazz);
            mappingStrategy.setColumnMapping(headers);

            StatefulBeanToCsv<T> beanToCsv = new StatefulBeanToCsvBuilder<T>(csvWriter)
                    .withMappingStrategy(mappingStrategy)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .build();

            // 先写入表头
            csvWriter.writeNext(headers);

            // 写入数据
            beanToCsv.write(data);

            return baos.toByteArray();
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            throw new RuntimeException("Error generating CSV", e);
        }
    }


    public static <T> byte[] generateExcelBytes(List<T> data, Class<T> clazz, String[] headers) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Sheet1");

            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // 创建表头行
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 创建数据单元格样式
            CellStyle textStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            textStyle.setDataFormat(format.getFormat("@"));
            textStyle.setBorderBottom(BorderStyle.THIN);
            textStyle.setBorderTop(BorderStyle.THIN);
            textStyle.setBorderRight(BorderStyle.THIN);
            textStyle.setBorderLeft(BorderStyle.THIN);

            // 创建数据行
            int rowIndex = 1;
            for (T item : data) {
                Row row = sheet.createRow(rowIndex++);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = row.createCell(i);
                    Field field = clazz.getDeclaredField(headers[i]);
                    field.setAccessible(true);
                    Object value = field.get(item);
                    if (value != null) {
                        // 设置单元格格式为文本
                        cell.setCellStyle(textStyle);
                        // 将值设置为字符串类型
                        cell.setCellValue(value.toString());
                    }
                }
            }

            // 根据内容自动调整单元格大小
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Error generating Excel file", e);
        }
    }

}
