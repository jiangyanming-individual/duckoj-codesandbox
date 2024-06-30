package com.jiang.duckojcodesandbox.security;

import java.security.Permission;

public class MySecurityManager extends SecurityManager {

    @Override
    public void checkPermission(Permission perm) {
        System.out.println("放开所有权限");
    }

    //限制读
    @Override
    public void checkRead(String file) {
        throw new SecurityException("checkRead异常：" + file);
    }

    //限制写
    @Override
    public void checkWrite(String file) {
        throw new SecurityException("checkWrite异常：" + file);
    }

    //限制运行
    @Override
    public void checkExec(String cmd) {
        throw new SecurityException("checkExec异常：" + cmd);
    }

    //限制网络连接：
    @Override
    public void checkConnect(String host, int port) {
        throw new SecurityException("checkConnect异常：" + host + ":" + port);
    }

    //限制删除操作：
    @Override
    public void checkDelete(String file) {
        throw new SecurityException("checkDelete异常：" + file);
    }
}
