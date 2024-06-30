package com.jiang.duckojcodesandbox.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    /**
     * 服务检测：
     *
     * @return
     */
    @GetMapping("/health")
    public String health() {
        return "ok";
    }

}
