package com.hdu.utils;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    /**
     * 将多个 CSV 文件字节数组打包成 ZIP 压缩包
     *
     * @param csvBytesMap CSV 文件字节数组 Map, key 是文件名, value 是字节数组
     * @return ZIP 压缩包的字节数组
     */
    public static byte[] zipCsvBytes(Map<String, byte[]> csvBytesMap) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Map.Entry<String, byte[]> entry : csvBytesMap.entrySet()) {
                String fileName = entry.getKey();
                byte[] csvBytes = entry.getValue();

                ZipEntry zipEntry = new ZipEntry(fileName);
                zos.putNextEntry(zipEntry);
                zos.write(csvBytes);
                zos.closeEntry();
            }
            zos.finish(); // 确保完成 ZIP 文件
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error creating ZIP file", e);
        }
    }
}
