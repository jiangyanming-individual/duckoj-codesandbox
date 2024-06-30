package com.jiang.duckojcodesandbox.security;

import java.security.Permission;


/**
 * 拒绝所有权限：
 */
public class DenySecurityManager extends SecurityManager{

    @Override
    public void checkPermission(Permission perm) {
//        super.checkPermission(perm);
        throw new SecurityException("运行操作：" + perm.getActions());
    }
}
