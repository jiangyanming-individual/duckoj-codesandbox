package com.jiang.duckojcodesandbox.ErrorProcess;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * 写入比如木马程序这种：
 */
public class WriteFileError {

    public static void main(String[] args) throws IOException {
        //获取当前的工作目录
        String user_dir = System.getProperty("user.dir");
        String filepath=user_dir + File.separator+ "/src/main/resources/木马.bat";
        String errorProgram="java -version 2>&1";
        Files.write(Paths.get(filepath), Arrays.asList(errorProgram));
    }
}
