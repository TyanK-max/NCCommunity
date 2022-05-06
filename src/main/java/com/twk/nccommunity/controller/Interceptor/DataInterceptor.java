package com.twk.nccommunity.controller.Interceptor;

import com.twk.nccommunity.entity.User;
import com.twk.nccommunity.service.DataService;
import com.twk.nccommunity.util.HostHolder;
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
    private HostHolder holder;

    //使用拦截器统计活跃指数
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = request.getRemoteHost();
        dataService.recordUV(ip);
        User users = holder.getUsers();
        if(users != null){
            dataService.recordDAU(users.getId());
        }
        return true;
    }
}
