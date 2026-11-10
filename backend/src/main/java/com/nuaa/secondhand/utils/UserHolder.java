package com.nuaa.secondhand.utils;

import java.util.Map;

/** 当前登录用户 ThreadLocal 持有器 */
public class UserHolder {

    private static final ThreadLocal<Map<Object, Object>> TL = new ThreadLocal<>();

    public static void saveUser(Map<Object, Object> user) {
        TL.set(user);
    }

    public static Map<Object, Object> getUser() {
        return TL.get();
    }

    public static Long getUserId() {
        Map<Object, Object> user = TL.get();
        if (user == null) {
            return null;
        }
        Object id = user.get("id");
        return id == null ? null : Long.valueOf(id.toString());
    }

    public static void removeUser() {
        TL.remove();
    }
}
