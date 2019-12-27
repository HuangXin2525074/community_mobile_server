package com.mycomany.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtil {

    public static String getValue(HttpServletRequest request, String name){
        if(request ==null || name==null){
            throw new IllegalArgumentException("pameter value is empty");
        }

        Cookie[] cookies = request.getCookies();

        if(cookies!=null){
            for(Cookie cookie:cookies){
                 if(cookie.getName().equals(name)){
                     return cookie.getValue();
                 }
            }

        }

        return null;
    }
}
