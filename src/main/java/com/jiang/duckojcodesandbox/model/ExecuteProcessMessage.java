package com.jiang.duckojcodesandbox.model;

import lombok.Data;

/**
 * 进程执行后的返回状态信息：
 */
@Data
public class ExecuteProcessMessage {


    //进行执行后的状态码
    private Integer exitValue;
    //执行成功后的message

    private String message;
    //执行错误后的message
    private String errorMessage;

    private Long time;
}
