package com.jiang.duckojcodesandbox.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.WordTree;
import com.jiang.duckojcodesandbox.model.ExecuteProcessMessage;
import com.jiang.duckojcodesandbox.model.ExecuteCodeRequest;
import com.jiang.duckojcodesandbox.model.ExecuteCodeResponse;
import com.jiang.duckojcodesandbox.model.JudgeInfo;
import com.jiang.duckojcodesandbox.utils.ProcessUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * java原生实现的代码沙箱
 */
@Component
public class JavaNativeCodeSandBoxOld implements CodeSandBox {


    public static final String GLOBAL_CODE_DIR_NAME = "tempCode";

    public static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    public static final List<String> BLACK_NAME_LIST = Arrays.asList("Files", "exec");

    // 只要指定到 MySecurityManager 的目录即可
    public static final String SECURITY_MANAGER_PATH = "D:\\WorkSpace\\Java_workspace\\duckoj-codesandbox\\src\\main\\resources\\security";

    public static final String SECURITY_MANAGER_CLASS_NAME = "MySecurityManager";

    public static final WordTree WORD_TREE;

    //初始化,词典树：
    static {
        WORD_TREE = new WordTree();
        WORD_TREE.addWords(BLACK_NAME_LIST);
    }

    /**
     * 测试
     *
     * @param args
     */
    public static void main(String[] args) {
        JavaNativeCodeSandBoxOld javaNativeCodeSandBox = new JavaNativeCodeSandBoxOld();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 2", "3 4"));
        executeCodeRequest.setSubmitLanguage("java");
        //读取文件内容：
        String code = ResourceUtil.readStr("testCode/Main.java", StandardCharsets.UTF_8);
        //运行超时
//        String code = ResourceUtil.readStr("errorCode/RunTimeError.java", StandardCharsets.UTF_8);
        //内存异常
//        String code = ResourceUtil.readStr("errorCode/MemoryError.java", StandardCharsets.UTF_8);
        //读文件，造成内存泄漏：
//        String code = ResourceUtil.readStr("errorCode/ReadFileError.java", StandardCharsets.UTF_8);
        //写入非法的文件：
//        String code = ResourceUtil.readStr("errorCode/WriteFileError.java", StandardCharsets.UTF_8);
        //运行非法程序
//        String code = ResourceUtil.readStr("errorCode/RunErrorProcess.java", StandardCharsets.UTF_8);
        executeCodeRequest.setSubmitCode(code);
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandBox.doExecute(executeCodeRequest);
        System.out.println(executeCodeResponse);

    }

    @Override
    public ExecuteCodeResponse doExecute(ExecuteCodeRequest executeCodeRequest) {
        //设置安全管理器
//        System.setSecurityManager(new MySecurityManager());

        List<String> inputList = executeCodeRequest.getInputList();
        String submitLanguage = executeCodeRequest.getSubmitLanguage();
        String submitCode = executeCodeRequest.getSubmitCode();
        // (1) 把用户代码保存为文件
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;

        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }

        //进行敏感词汇的匹配：
//        FoundWord foundWord = WORD_TREE.matchWord(submitCode);
//        if (foundWord != null) {
//            System.out.println("含有敏感词汇:" + foundWord.getFoundWord());
//            return null;
//        }

        //用户代码存放目录：
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        //java代码
        String userJavaCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        //写入文件
        File userCodeFile = FileUtil.writeString(submitCode, userJavaCodePath, StandardCharsets.UTF_8);
        //(2) 编译代码：
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsoluteFile());
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ProcessUtils.runProcess(compileProcess, "编译");
        } catch (Exception e) {
            //返回异常处理类：
            return getErrorResponse(e);
        }
        //(3) 运行代码：
        List<ExecuteProcessMessage> executeProcessMessageList = new ArrayList<>();
        for (String inputArgs : inputList) {
            //加入安全管理器：
            String runCmd = String.format("java -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=%s Main %s", userCodeParentPath,SECURITY_MANAGER_PATH,SECURITY_MANAGER_CLASS_NAME, inputArgs);
            //执行进程：
            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                new Thread(() -> {
                    try {
                        Thread.sleep(10000);
                        System.out.println("设置超时控制");
                        //时间一到进行销毁操作
                        runProcess.destroy();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                //输出结果
                ExecuteProcessMessage executeProcessMessage = ProcessUtils.runProcess(runProcess, "运行");
                executeProcessMessageList.add(executeProcessMessage);
            } catch (Exception e) {
                return getErrorResponse(e);
            }
        }
        //(4) 整理输出
        //遍历结果：
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        long maxTime = 0; //判题时，取最大的时间，看是否超时
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
//        judgeInfo.setMemory();
        executeCodeResponse.setJudgeInfo(judgeInfo);
        //(5) 删除文件
        if (userCodeFile.getParentFile() != null) {
            boolean del = FileUtil.del(userCodeFile.getParentFile());
            System.out.println("执行删除文件：" + (del ? "删除" : "失败"));
        }
        return executeCodeResponse;
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
