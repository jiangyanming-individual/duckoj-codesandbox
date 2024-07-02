package com.jiang.duckojcodesandbox.controller;


import com.jiang.duckojcodesandbox.model.ExecuteCodeRequest;
import com.jiang.duckojcodesandbox.model.ExecuteCodeResponse;
import com.jiang.duckojcodesandbox.service.JavaNativeCodeSandBox;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;

@RestController
public class MainController {


    @Resource
    private JavaNativeCodeSandBox javaNativeCodeSandBox;

    /**
     * 服务检测：
     *
     * @return
     */
    @GetMapping("/health")
    public String health() {
        return "ok";
    }

    /**
     * 执行代码沙箱的接口
     *
     * @param executeCodeRequest
     * @return
     */
    @PostMapping("/executeCode")
    ExecuteCodeResponse doExecute(@RequestBody ExecuteCodeRequest executeCodeRequest) {
        if (executeCodeRequest == null) {
            throw new RuntimeException("请求参数为空");
        }
        ExecuteCodeResponse executeCodeResponse = null;
        try {
            executeCodeResponse = javaNativeCodeSandBox.doExecute(executeCodeRequest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return executeCodeResponse;
    }
}
