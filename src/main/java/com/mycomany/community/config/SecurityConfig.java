package com.mycomany.community.config;

import com.mycomany.community.util.communityConstant;

import com.mycomany.community.util.communityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements communityConstant {

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // verify permission
        http.authorizeRequests()
                .antMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow"
                )
                .hasAnyAuthority(
                        AUTHORITY_USER,
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discuss/top",
                        "/discuss/wonderful"
                )
                .hasAnyAuthority(
                        AUTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discuss/delete"
                )
                .hasAnyAuthority(
                        AUTHORITY_ADMIN
                )
                .anyRequest().permitAll()
                .and().csrf().disable();


        // redirect method if that is no permission access.
        http.exceptionHandling()
                .authenticationEntryPoint(new AuthenticationEntryPoint() {

                    // non-login
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
                    String xRequestWith = request.getHeader("x-request-with");
                    if("XMLHttpRequest".equals(xRequestWith)){
                        response.setContentType("application/plain;charset=utf8");
                        PrintWriter writer = response.getWriter();
                        writer.write(communityUtil.getJSONString(403,"please login your account!"));

                    }else{
                       response.sendRedirect(request.getContextPath()+"/login");
                    }
                    }
                })
                .accessDeniedHandler(new AccessDeniedHandler() {

                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
                        String xRequestWith = request.getHeader("x-request-with");
                        if("XMLHttpRequest".equals(xRequestWith)){
                            response.setContentType("application/plain;charset=utf8");
                            PrintWriter writer = response.getWriter();
                            writer.write(communityUtil.getJSONString(403,"no permission to access!"));

                        }else{
                            response.sendRedirect(request.getContextPath()+"/denied");
                        }
                    }
                });

        http.logout().logoutUrl("/securitylogout");
    }





}
