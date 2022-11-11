package com.neko.reggie.utils;

/**
 * 基于ThreadLocal封装工具类，用户保存或获取当前登录用户id
 */
public class BaseContext {

    private static final ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 设置当前登录用户id
     * @param id 登录用户ID
     */
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    /**
     * @return 返回当前登录用户id
     */
    public static Long getCurrentId() {
        return threadLocal.get();
    }

}
