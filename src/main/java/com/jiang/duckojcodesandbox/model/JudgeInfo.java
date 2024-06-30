package com.jiang.duckojcodesandbox.model;


import lombok.Data;

/**
 * 题目提交信息类
 */
@Data
public class JudgeInfo {
        /**
         * 题目提交结果信息
         */
        private String message;
        /**
         * 提交所用时间
         */
        private Long time;
        /**
         * 题目提交所占
         */
        private Long memory; // 消耗内存，单位为 kb
}
