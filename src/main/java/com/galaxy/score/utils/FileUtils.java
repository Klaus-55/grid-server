package com.galaxy.score.utils;

import lombok.extern.slf4j.Slf4j;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author zouwt
 * @version 1.0.0
 * @date 2020/7/31 15:40
 */
@Slf4j
public class FileUtils {
    /**
     * 文件是否存在
     *
     * @param filePath 文件路径
     * @return 返回true或false
     */
    public static Boolean exist(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    /**
     * 文件是否不存在
     *
     * @param filePath 文件路径
     * @return 返回true或false
     */
    public static Boolean notExist(String filePath) {
        return !exist(filePath);
    }

    /**
     * 删除文件
     *
     * @param filePath 文件路径
     * @return true或false
     */
    public static Boolean del(String filePath) {
        File file = new File(filePath);
        return file.delete();
    }

    /**
     * 下载文件
     *
     * @param response 响应流
     * @param filePath 文件路径
     */
    public static void downloadFile(HttpServletResponse response, String filePath) {
        try (InputStream is = new FileInputStream(new File(filePath)); OutputStream os = response.getOutputStream()) {
            int index = filePath.lastIndexOf("\\");
            String fileName = filePath.substring(index + 1);
            setResponseHeader(response, fileName);
            byte[] b = new byte[1024];
            int len;
            while ((len = is.read(b)) != -1) {
                os.write(b, 0, len);
            }
            os.flush();
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 返回当前目录下的所有文件名
     *
     * @param files 所有文件
     * @return 返回文件名
     */
    public static List<String> fileNames(File[] files) {
        return Arrays.stream(files).map(File::getName).collect(Collectors.toList());
    }

    /**
     * 设置响应流
     *
     * @param response 响应流
     * @param fileName 文件名称
     */
    public static void setResponseHeader(HttpServletResponse response, String fileName) {
        fileName = new String(fileName.getBytes(), StandardCharsets.ISO_8859_1);
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        response.addHeader("Pargam", "no-cache");
        response.addHeader("Cache-Control", "no-cache");
    }

}
