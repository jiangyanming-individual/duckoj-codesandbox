package com.jiang.duckojcodesandbox.service;



import com.jiang.duckojcodesandbox.model.ExecuteCodeRequest;
import com.jiang.duckojcodesandbox.model.ExecuteCodeResponse;

import java.io.IOException;

public interface CodeSandBox {

    /**
     * 执行代码：
     * @param executeRequest
     * @return
     */
    ExecuteCodeResponse doExecute(ExecuteCodeRequest executeRequest) throws IOException;
}
