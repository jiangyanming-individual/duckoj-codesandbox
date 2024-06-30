package com.jiang.duckojcodesandbox.utils;


import com.jiang.duckojcodesandbox.model.ExecuteProcessMessage;
import org.springframework.util.StopWatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 封装进程工具类：
 */
public class ProcessUtils {

    
    /**
     * 进程操作：
     *
     * @param process
     * @param operateName
     * @return
     */
    public static ExecuteProcessMessage runProcess(Process process, String operateName) throws IOException {
        ExecuteProcessMessage executeProcessMessage = new ExecuteProcessMessage();
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            //等待程序执行成功，根据exitValue来判断是否执行成功
            int exitValue = process.waitFor();
            executeProcessMessage.setExitValue(exitValue);
            if (exitValue == 0) {
                System.out.println(operateName + "成功");
                //获取控制台输出成功信息；
                InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuffer compileOutputBuffer = new StringBuffer();
                String readLine;
                while ((readLine = bufferedReader.readLine()) != null) {
                    compileOutputBuffer.append(readLine);
                }
                executeProcessMessage.setMessage(compileOutputBuffer.toString());
                System.out.println(executeProcessMessage);
                bufferedReader.close();
                inputStreamReader.close();
            } else {
                System.out.println(operateName + "失败,错误码为：" + exitValue);
                //编译失败正常输出
                InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuffer compileOutputBuffer = new StringBuffer();
                //逐行读取数据
                String outputReadLine;
                while ((outputReadLine = bufferedReader.readLine()) != null) {
                    compileOutputBuffer.append(outputReadLine);
                }
                executeProcessMessage.setMessage(compileOutputBuffer.toString());
//                System.out.println(compileOutputBuffer);
                //编译失败输出
                inputStreamReader = new InputStreamReader(process.getErrorStream());
                BufferedReader errorBufferedReader = new BufferedReader(inputStreamReader);
                StringBuffer compileErrorOutputBuffer = new StringBuffer();
                //逐行读取数据
                String errorReadLine;
                while ((errorReadLine = errorBufferedReader.readLine()) != null) {
                    compileErrorOutputBuffer.append(errorReadLine);
                }
                //设置错误信息
                executeProcessMessage.setErrorMessage(compileErrorOutputBuffer.toString());
                System.out.println(executeProcessMessage);
                //关闭流
                bufferedReader.close();
                errorBufferedReader.close();
                inputStreamReader.close();
            }
            stopWatch.stop();
            long costTime = stopWatch.getLastTaskTimeMillis();
            executeProcessMessage.setTime(costTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return executeProcessMessage;
    }

    /**
     * 交互式
     *
     * @param process
     * @param operateName
     * @return
     * @throws IOException
     */
    public static ExecuteProcessMessage runInteractProcess(Process process, String operateName) throws IOException {
        ExecuteProcessMessage executeProcessMessage = new ExecuteProcessMessage();

        BufferedReader bufferedReader = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader errorBufferedReader = null;
        try {
            //等待程序执行成功，根据exitValue来判断是否执行成功
            int exitValue = process.waitFor();
            executeProcessMessage.setExitValue(exitValue);
            if (exitValue == 0) {
                System.out.println(operateName + "成功");
                //获取控制台输出成功信息；
                inputStreamReader = new InputStreamReader(process.getInputStream());
                bufferedReader = new BufferedReader(inputStreamReader);
                StringBuffer compileOutputBuffer = new StringBuffer();
                String readLine;
                while ((readLine = bufferedReader.readLine()) != null) {
                    compileOutputBuffer.append(readLine);
                }
                executeProcessMessage.setMessage(compileOutputBuffer.toString());
                System.out.println(executeProcessMessage);
            } else {
                System.out.println(operateName + "失败,错误码为：" + exitValue);
                //编译失败正常输出
                inputStreamReader = new InputStreamReader(process.getInputStream());
                bufferedReader = new BufferedReader(inputStreamReader);
                StringBuffer compileOutputBuffer = new StringBuffer();
                //逐行读取数据
                String outputReadLine;
                while ((outputReadLine = bufferedReader.readLine()) != null) {
                    compileOutputBuffer.append(outputReadLine);
                }
                executeProcessMessage.setMessage(compileOutputBuffer.toString());
//                System.out.println(executeProcessMessage);
                //编译失败输出
                inputStreamReader = new InputStreamReader(process.getErrorStream());
                errorBufferedReader = new BufferedReader(inputStreamReader);
                StringBuffer compileErrorOutputBuffer = new StringBuffer();
                //逐行读取数据
                String errorReadLine;
                while ((errorReadLine = errorBufferedReader.readLine()) != null) {
                    compileErrorOutputBuffer.append(errorReadLine);
                }
                //设置错误信息
                executeProcessMessage.setErrorMessage(compileErrorOutputBuffer.toString());
                System.out.println(executeProcessMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            //销毁进程：
            if (process != null) {
                process.destroy();
            }
            if (errorBufferedReader != null) {
                errorBufferedReader.close();
            }
        }
        return executeProcessMessage;
    }
}
