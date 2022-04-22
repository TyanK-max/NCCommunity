package com.twk.nccommunity.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * 找到Cookie中的值
 */
public class CookieUtil {
    public static String getValue(HttpServletRequest request,String name){
        if(request == null || name == null){
            throw new IllegalArgumentException("传入参数有误!");
        }
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for (Cookie cookie:
                 cookies) {
                if(cookie.getName().equals(name)){
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
