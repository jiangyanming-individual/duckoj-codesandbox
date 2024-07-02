package com.jiang.duckojcodesandbox.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.jiang.duckojcodesandbox.model.ExecuteProcessMessage;
import com.jiang.duckojcodesandbox.model.ExecuteCodeRequest;
import com.jiang.duckojcodesandbox.model.ExecuteCodeResponse;
import com.jiang.duckojcodesandbox.model.JudgeInfo;
import com.jiang.duckojcodesandbox.utils.ProcessUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 模版模式实现，代码沙箱模版
 */
public abstract class JavaCodeSandBoxTemplate implements CodeSandBox {

    public static final String GLOBAL_CODE_DIR_NAME = "tempCode";

    public static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    @Override
    public ExecuteCodeResponse doExecute(ExecuteCodeRequest executeCodeRequest) throws IOException {
        List<String> inputList = executeCodeRequest.getInputList();
        String submitLanguage = executeCodeRequest.getSubmitLanguage();
        String submitCode = executeCodeRequest.getSubmitCode();
        // 1.保存代码文件
        File userCodeFile = saveFile(submitCode);
        // 2.编译文件
        compileFile(userCodeFile);
        //3. 运行代码文件
        List<ExecuteProcessMessage> executeProcessMessageList = runFile(inputList, userCodeFile);
        //4. 整理输出结果
        ExecuteCodeResponse executeCodeResponse = getOutput(executeProcessMessageList);
        //5. 删除文件
        Boolean b = delFile(userCodeFile);
        if (!b) {
            throw new RuntimeException("删除文件失败");
        }
        return executeCodeResponse;
    }

    /**
     * 1. 保存文件
     *
     * @param submitCode
     * @return
     */
    public File saveFile(String submitCode) {
        // (1) 把用户代码保存为文件
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }
        //用户代码存放目录：
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        //java代码
        String userJavaCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        //写入文件
        File userCodeFile = FileUtil.writeString(submitCode, userJavaCodePath, StandardCharsets.UTF_8);

        return userCodeFile;
    }


    /**
     * 2. 编译代码文件
     */
    public void compileFile(File userCodeFile) {
        //(2) 编译代码：
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsoluteFile());
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ProcessUtils.runProcess(compileProcess, "编译");
        } catch (Exception e) {
            //返回异常处理类：
            throw new RuntimeException(e);
        }
    }

    /**
     * 3. 运行代码文件
     *
     * @param inputList
     * @param userCodeFile
     * @return
     */
    public List<ExecuteProcessMessage> runFile(List<String> inputList, File userCodeFile) {
        //获取父目录的绝对路径：
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        List<ExecuteProcessMessage> executeProcessMessageList = new ArrayList<>();
        for (String inputArgs : inputList) {
            //加入安全管理器：
            String runCmd = String.format("java -Xmx256 -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath, inputArgs);
            //执行进程：
            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                //输出结果
                ExecuteProcessMessage executeProcessMessage = ProcessUtils.runProcess(runProcess, "运行");
                executeProcessMessageList.add(executeProcessMessage);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return executeProcessMessageList;
    }

    /**
     * 4. 整理输出结果
     *
     * @param executeProcessMessageList
     * @return
     */
    public ExecuteCodeResponse getOutput(List<ExecuteProcessMessage> executeProcessMessageList) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        //判题时，取最大的时间，看是否超时
        long maxTime = 0;
        for (ExecuteProcessMessage executeProcessMessage : executeProcessMessageList) {
            String errorMessage = executeProcessMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage)) {
                //运行错误；设置状态为失败和失败信息
                executeCodeResponse.setMessage(errorMessage);
                executeCodeResponse.setSubmitState(3);
                break;
            }
            //获取输出的信息到outputList中：
            outputList.add(executeProcessMessage.getMessage());
            Long time = executeProcessMessage.getTime();
            if (time != null) {
                maxTime = Math.max(time, maxTime);
            }
        }
        //如果输出和判题结果的输出个数相等：
        if (outputList.size() == executeProcessMessageList.size()) {
            executeCodeResponse.setSubmitState(2); //成功
        }
        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
        // judgeInfo.setMemory();
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }

    /**
     * 5. 删除文件
     *
     * @param userCodeFile
     * @return
     */
    public Boolean delFile(File userCodeFile) {
        //(5) 删除文件
        if (userCodeFile.getParentFile() != null) {
            boolean del = FileUtil.del(userCodeFile.getParentFile());
            System.out.println("执行删除文件：" + (del ? "删除" : "失败"));
            return del;
        }
        return true;
    }

    //(6) 异常处理
    /**
     * 返回异常处理类：
     *
     * @param e
     * @return
     */
    public ExecuteCodeResponse getErrorResponse(Throwable e) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        executeCodeResponse.setSubmitState(3);
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return executeCodeResponse;
    }

}
