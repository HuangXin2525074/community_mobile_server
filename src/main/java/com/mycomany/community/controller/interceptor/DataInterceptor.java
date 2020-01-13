package com.mycomany.community.controller.interceptor;


import com.mycomany.community.entity.User;
import com.mycomany.community.services.DataService;
import com.mycomany.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class DataInterceptor implements HandlerInterceptor {

    @Autowired
    private DataService dataService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

       String ip = request.getRemoteHost();
       dataService.recordUV(ip);

        User user = hostHolder.getUser();
       if(user != null){
           dataService.recordDAU(user.getId());
       }


        return true;
    }
}
