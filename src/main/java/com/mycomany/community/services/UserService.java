package com.mycomany.community.services;


import com.mycomany.community.dao.LoginTicketMapper;
import com.mycomany.community.dao.UserMapper;
import com.mycomany.community.entity.LoginTicket;
import com.mycomany.community.entity.User;
import com.mycomany.community.util.MailClient;
import com.mycomany.community.util.RedisKeyUtil;
import com.mycomany.community.util.communityUtil;
import com.mycomany.community.util.communityConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements communityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id){
        //return userMapper.selectById(id);
        User user = getCache(id);
        if(user == null){
           user = initCache(id);
        }
        return user;
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
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/145t.png",new Random().nextInt(1000)));
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
           clearCache(userId);
           return ACTIVATION_SUCCESS;
       }else{
           return ACTIVATION_FAILURE;
       }

    }

    public Map<String,Object> login(String username, String password, int expiredSeconds){

        Map<String,Object> map = new HashMap<>();

        // checking empty value.
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","empty username not allow");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","empty password not allow");
            return map;
        }

        //  username validations
        User user = userMapper.selectByName(username);
        if(user==null){
            map.put("usernameMsg","account not exist");
            return map;
        }

        //status validations
        if(user.getStatus() == 0){
            map.put("usernameMsg","please active this account");
            return map;
        }

        // password validations
        password = communityUtil.md5(password+user.getSalt());
        if(!user.getPassword().equals(password)){
            map.put("passwordMsg","password not valid");
            return map;
        }

        // generate login ticket
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(communityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+expiredSeconds*1000));
      //  loginTicketMapper.insetLoginTicket(loginTicket);

        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey,loginTicket);

        map.put("ticket", loginTicket.getTicket());




        return map;

    }

    public void logout(String ticket){
        //loginTicketMapper.updateStatus(ticket, 1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
       LoginTicket loginTicket =(LoginTicket)redisTemplate.opsForValue().get(redisKey);
       loginTicket.setStatus(1);
       redisTemplate.opsForValue().set(redisKey,loginTicket);
    }

    public LoginTicket findLoginTicket(String ticket){
        //return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket)redisTemplate.opsForValue().get(redisKey);
    }

    public int updateHeader(int userId, String headerUrl){
     //   return userMapper.updateHeader(userId,headerUrl);
        int rows = userMapper.updateHeader(userId,headerUrl);
        clearCache(userId);
        return rows;
    }

    public Map<String,Object> updatePassword(int userId, String password, String newPassword,String confirmPassword){

          Map<String,Object> map = new HashMap<>();

          if(StringUtils.isBlank(password)){
              map.put("passwordMsg","empty password not allow");
              return map;
          }
          if(StringUtils.isBlank(newPassword)){
            map.put("newPasswordMsg","empty password not allow");
            return map;
          }
          if(StringUtils.isBlank(confirmPassword)){
              map.put("confirmPasswordMsg","empty password not allow");
              return map;
          }

        User user = userMapper.selectById(userId);

        password = communityUtil.md5(password+user.getSalt());
        if(!user.getPassword().equals(password)){
            map.put("passwordMsg","password not valid");
            return map;
        }

        newPassword = communityUtil.md5(newPassword+user.getSalt());
        if(user.getPassword().equals(newPassword)){
            map.put("newPasswordMsg","password not valid, please enter different password");
            return map;
        }

        confirmPassword = communityUtil.md5(confirmPassword+user.getSalt());
        if(!newPassword.equals(confirmPassword)){
            map.put("confirmPasswordMsg","new password and confirm password must be the same");
            return map;
        }
        if(user.getPassword().equals(newPassword)){
            map.put("confirmPasswordMsg","password not valid, please enter different password");
            return map;
        }



        userMapper.updatePassword(userId,confirmPassword);


          return map;
    }

    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }

    // collect user data from Cache
    private User getCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User)redisTemplate.opsForValue().get(redisKey);
    }

    // init the Cache if user data do not exists.
    private User initCache(int userId){
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);
        return  user;
    }

    // delete user data when user data updated.
    private void clearCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }








}
