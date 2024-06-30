package com.jiang.duckojcodesandbox.service;



import com.jiang.duckojcodesandbox.model.ExecuteRequest;
import com.jiang.duckojcodesandbox.model.ExecuteResponse;

import java.io.IOException;

public interface CodeSandBox {

    /**
     * 执行代码：
     * @param executeRequest
     * @return
     */
    ExecuteResponse doExecute(ExecuteRequest executeRequest) throws IOException;
}
