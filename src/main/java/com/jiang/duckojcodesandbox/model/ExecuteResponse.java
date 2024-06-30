package com.jiang.duckojcodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 代码沙箱响应参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExecuteResponse {

    /**
     * 输入结果：
     */
    private List<String> outputList;
    /**
     * 判题接口的信息
     */
    private String message;
    /**
     * 判题的状态
     */
    private Integer submitState;

    //判题的信息：
    private JudgeInfo judgeInfo;



}
