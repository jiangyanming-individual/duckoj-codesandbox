package com.jiang.duckojcodesandbox.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.jiang.duckojcodesandbox.model.ExecuteProcessMessage;
import com.jiang.duckojcodesandbox.model.ExecuteCodeRequest;
import com.jiang.duckojcodesandbox.model.ExecuteCodeResponse;
import com.jiang.duckojcodesandbox.model.JudgeInfo;
import com.jiang.duckojcodesandbox.utils.ProcessUtils;
import org.springframework.util.StopWatch;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * java原生实现的代码沙箱
 */
public class JavaDockerCodeSandBoxOld implements CodeSandBox {

    public static final String GLOBAL_CODE_DIR_NAME = "tempCode";

    public static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";
    /**
     * 设置超时时间
     */
    public static final Long TIME_OUT = 20000L;

    private static final Boolean FIRST_INIT = true;

    /**
     * 测试
     *
     * @param args
     */
    public static void main(String[] args) {
        JavaDockerCodeSandBoxOld dockerCodeSandBox = new JavaDockerCodeSandBoxOld();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 2", "3 4"));
        executeCodeRequest.setSubmitLanguage("java");
        //读取文件内容：
        String code = ResourceUtil.readStr("testCode/Main.java", StandardCharsets.UTF_8);
        //运行超时
        executeCodeRequest.setSubmitCode(code);
        ExecuteCodeResponse executeCodeResponse = null;
        try {
            executeCodeResponse = dockerCodeSandBox.doExecute(executeCodeRequest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(executeCodeResponse);
    }

    @Override
    public ExecuteCodeResponse doExecute(ExecuteCodeRequest executeCodeRequest) throws IOException {
        List<String> inputList = executeCodeRequest.getInputList();
        String submitLanguage = executeCodeRequest.getSubmitLanguage();
        String submitCode = executeCodeRequest.getSubmitCode();
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
        //(2) 编译代码：
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsoluteFile());
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ProcessUtils.runProcess(compileProcess, "编译");
        } catch (Exception e) {
            //返回异常处理类：
            return getErrorResponse(e);
        }
        //(3) 拉取镜像，创建容器，上传编译文件到容器：

        //3.1 拉取镜像：
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        String image = "openjdk:8-alpine";
        if (FIRST_INIT) {
            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
                @Override
                public void onNext(PullResponseItem item) {
                    System.out.println("镜像状态：" + item.getStatus());
                    super.onNext(item);
                }
            };
            //异步等待：
            try {
                pullImageCmd.exec(pullImageResultCallback).awaitCompletion();
            } catch (InterruptedException e) {
                System.out.println("拉取镜像失败！");
                throw new RuntimeException(e);
            }
        }
        System.out.println("拉取镜像成功");

        //3.2 创建容器，然后进行容器挂载，传入编译后的文件：
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
        HostConfig hostConfig = new HostConfig();
        //设置cpu个数
        hostConfig.withCpuCount(1L);
        //设置最大内存
        hostConfig.withMemory(100 * 1000 * 1000L);
        hostConfig.withMemorySwap(0L);//内存交换
        //Linux 内核权限控制
//        String profileConfig = ResourceUtil.readUtf8Str("profile.json");
//        hostConfig.withSecurityOpts(Arrays.asList("seccomp=" + profileConfig));
        //进行虚拟机和容器目录挂载：
        hostConfig.setBinds(new Bind(userCodeParentPath, new Volume("/app")));
        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                .withNetworkDisabled(true) //设置网络不可用
                .withReadonlyRootfs(true) //限制用户使用root来操作
                .withAttachStdin(true) //开启容器交互
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withTty(true) //开启交互终端
                .exec();
        System.out.println("创建容器id为：" + createContainerResponse.getId());
        String containerId = createContainerResponse.getId();

        //3.3 启动容器
        dockerClient.startContainerCmd(containerId).exec();
        System.out.println("启动容器成功!");

        // 4、创建和执行命令 docker exec containerId java -cp /app Main 1 2
        List<ExecuteProcessMessage> executeProcessMessageList = new ArrayList();
        for (String inputArgs : inputList) {
            System.out.println("inputArgs:" + inputArgs);
            String[] inputArrays = inputArgs.split(" ");
            //4.1 创建命令：
            String[] cmdArray = ArrayUtil.append(new String[]{"java", "-cp", "/app", "Main"}, inputArrays);
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(cmdArray)
                    .withAttachStderr(true)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .exec();
            System.out.println("执行创建命令：" + execCreateCmdResponse);
            String execId = execCreateCmdResponse.getId();
            if (execId == null) {
                throw new RuntimeException("执行命令不存在");
            }
            ExecuteProcessMessage executeProcessMessage = new ExecuteProcessMessage();
            final String[] message = {null};
            final String[] errMessage = {null};
            //4.1 执行命令：
            final boolean[] IS_TIME_OUT = {true};
            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
                @Override
                public void onNext(Frame frame) {
                    StreamType streamType = frame.getStreamType();
                    if (StreamType.STDERR.equals(streamType)) {
                        errMessage[0] = new String(frame.getPayload());
                        System.out.println("输出错误结果：" + errMessage[0]);
                    } else {
                        message[0] = new String(frame.getPayload());
                        System.out.println("输出正确结果：" + message[0]);
                    }
                    super.onNext(frame);
                }

                @Override
                public void onComplete() {
                    //如果顺利执行完会走这一步
                    IS_TIME_OUT[0] = false;
                    System.out.println("程序执行没有超时");
                    super.onComplete();
                }
            };
            //统计占用内存
            final long[] maxMemory = {0L};
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            ResultCallback<Statistics> resultCallback = statsCmd.exec(new ResultCallback<Statistics>() {
                @Override
                public void onNext(Statistics statistics) {
                    System.out.println("内存占用统计：" + statistics.getMemoryStats().getUsage());
                    maxMemory[0] = Math.max(maxMemory[0], statistics.getMemoryStats().getUsage());
                }

                @Override
                public void onStart(Closeable closeable) {
                }

                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onComplete() {

                }

                @Override
                public void close() throws IOException {

                }
            });
            //统计命令
            statsCmd.exec(resultCallback);
            statsCmd.close();

            //统计运行时间, 执行命令
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            try {
                dockerClient.execStartCmd(execId)
                        .exec(execStartResultCallback)
                        .awaitCompletion(TIME_OUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                System.out.println("执行命令失败");
                throw new RuntimeException(e);
            }
            stopWatch.stop();
            long stop_time = stopWatch.getLastTaskTimeMillis();
            executeProcessMessage.setMessage(message[0]);
            executeProcessMessage.setErrorMessage(errMessage[0]);
            executeProcessMessage.setTime(stop_time);
            executeProcessMessage.setMemory(maxMemory[0]);
            //添加到返回信息中：
            executeProcessMessageList.add(executeProcessMessage);
        }

        //5 整理输出，遍历executeProcessMessageList
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        //5.1 题时，取最大的时间，看是否超时
        long maxTime = 0L;
        long maxMemory = 0L;
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
            Long memory = executeProcessMessage.getMemory();
            if (time != null) {
                maxTime = Math.max(time, maxTime);
            }
            if (memory != null) {
                maxMemory = Math.max(maxMemory, memory);
            }
        }

        //5.2 如果输出和判题结果的输出个数相等：
        if (outputList.size() == executeProcessMessageList.size()) {
            executeCodeResponse.setMessage("运行成功！");
            executeCodeResponse.setSubmitState(2); //成功
        }
        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
        judgeInfo.setMemory(maxMemory);
        executeCodeResponse.setJudgeInfo(judgeInfo);

        //6 删除文件
        if (userCodeFile.getParentFile() != null) {
            boolean del = FileUtil.del(userCodeFile.getParentFile());
            System.out.println("执行删除文件：" + (del ? "删除" : "失败"));
        }
        return executeCodeResponse;
    }

    //7 异常处理

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
