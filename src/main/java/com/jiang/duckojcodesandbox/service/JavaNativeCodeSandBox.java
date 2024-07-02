package com.jiang.duckojcodesandbox.service;
import com.jiang.duckojcodesandbox.model.ExecuteCodeRequest;
import com.jiang.duckojcodesandbox.model.ExecuteCodeResponse;
import java.io.IOException;

/**
 * java原生实现的代码沙箱
 */
public class JavaNativeCodeSandBox extends JavaCodeSandBoxTemplate {

    @Override
    public ExecuteCodeResponse doExecute(ExecuteCodeRequest executeCodeRequest) throws IOException {
        return super.doExecute(executeCodeRequest);
    }
}
