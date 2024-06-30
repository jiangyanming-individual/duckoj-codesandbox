package com.jiang.duckojcodesandbox.security;

import java.security.Permission;

public class DefaultSecurityManager extends SecurityManager{

    @Override
    public void checkPermission(Permission perm) {

        System.out.println("放开所有权限");
        System.out.println(perm);
//        super.checkPermission(perm); //默认是禁用所有权限
    }
}
