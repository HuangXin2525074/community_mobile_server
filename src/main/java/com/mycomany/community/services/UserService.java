package com.mycomany.community.services;


import com.mycomany.community.dao.UserMapper;
import com.mycomany.community.entity.User;
import com.mycomany.community.util.MailClient;
import com.mycomany.community.util.communityUtil;
import com.mycomany.community.util.communityConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements communityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id){
        return userMapper.selectById(id);
    }


    public Map<String, Object> register(User user){
        Map<String, Object> map = new HashMap<>();

        if(user ==null){
            throw new IllegalArgumentException("empty value not allow");
        }

        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","empty username not allow");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","empty password not allow");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","empty email not allow");
            return map;
        }

        User u =userMapper.selectByName(user.getUsername());
        if(u!=null){
            map.put("usernameMsg","username exist!");
            return map;
        }

        u = userMapper.selectByEmail(user.getEmail());
        if(u!=null){
            map.put("emailMsg","email exist!");
            return map;
        }

        user.setSalt(communityUtil.generateUUID().substring(0,5));
        user.setPassword(communityUtil.md5(user.getPassword()+user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(communityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.newcoder.com/head/#dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());

        userMapper.insertUser(user);




         // email activation
        Context context = new Context();
        context.setVariable("email",user.getEmail());

        String url = domain+contextPath+"/activation/"+user.getId() +"/"+user.getActivationCode();
        context.setVariable("url",url);

        String content = templateEngine.process("/mail/activation",context);

        mailClient.sendMail(user.getEmail(),"account activation",content);



        return map;
    }

    public int activation(int userId, String code){

       User user = userMapper.selectById(userId);

       if(user.getStatus()==1){
           return ACTIVATION_REPECT;
       }else if(user.getActivationCode().equals(code)){
           userMapper.updateStatus(userId,1);
           return ACTIVATION_SUCCESS;
       }else{
           return ACTIVATION_FAILURE;
       }

    }




}
