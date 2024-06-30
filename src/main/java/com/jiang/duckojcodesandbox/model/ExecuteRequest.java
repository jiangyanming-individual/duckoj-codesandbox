package com.jiang.duckojcodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 代码沙箱请求参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder //链式调用
public class ExecuteRequest {

    /**
     * 输入用例
     */
    private List<String> inputList;

    /**
     * 提交语言：
     */
    private String submitLanguage;

    /**
     * 提交的代码
     */
    private String submitCode;

}
