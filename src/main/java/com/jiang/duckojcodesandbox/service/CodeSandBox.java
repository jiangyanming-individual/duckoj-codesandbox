package com.jiang.duckojcodesandbox.service;



import com.jiang.duckojcodesandbox.model.ExecuteCodeRequest;
import com.jiang.duckojcodesandbox.model.ExecuteCodeResponse;

import java.io.IOException;

public interface CodeSandBox {

    /**
     * 执行代码：
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse doExecute(ExecuteCodeRequest executeCodeRequest) throws IOException;
}
