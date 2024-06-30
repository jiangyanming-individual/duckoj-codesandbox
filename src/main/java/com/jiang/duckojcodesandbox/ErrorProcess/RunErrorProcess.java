package com.jiang.duckojcodesandbox.ErrorProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 执行程序
 */
public class RunErrorProcess {

    public static void main(String[] args) throws IOException, InterruptedException {

        String user_dir = System.getProperty("user.dir");
        String filepath=user_dir+ File.separator + "/src/main/resources/木马.bat";
        Process process = Runtime.getRuntime().exec(filepath);
        process.waitFor();
        //获取控制台输出成功信息；
        InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuffer compileOutputBuffer = new StringBuffer();
        String readLine;
        while ((readLine = bufferedReader.readLine()) != null) {
            compileOutputBuffer.append(readLine);
        }
        System.out.println(compileOutputBuffer);

    }
}
