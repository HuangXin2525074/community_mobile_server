package com.mycomany.community.controller;

import com.mycomany.community.entity.User;
import com.mycomany.community.services.UserService;
import com.mycomany.community.util.communityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

@Controller
public class LoginController implements communityConstant{

    @Autowired
    private UserService userService;

    @RequestMapping(path="/register", method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }

    @RequestMapping(path="/login", method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }

    @RequestMapping(path="/register", method = RequestMethod.POST)
    public String register(Model model, User user){
       Map<String,Object> map = userService.register(user);

       if(map==null || map.isEmpty()){
           model.addAttribute("msg","register account success,please check your email for activiation");
           model.addAttribute("target","/index");
           return "/site/operate-result";
       }else{
           model.addAttribute("usernameMsg",map.get("usernameMsg"));
           model.addAttribute("passwordMsg",map.get("passwordMsg"));
           model.addAttribute("emailMsg",map.get("emailMsg"));
          return "/site/register";

       }

    }

    @RequestMapping(path="/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {

    int result = userService.activation(userId,code);

    if(result == ACTIVATION_SUCCESS){
        model.addAttribute("msg"," activation success,your account is ready for uses");
        model.addAttribute("target","/login");

    }else if(result== ACTIVATION_REPECT){
        model.addAttribute("msg"," activation fail,your account is already activated");
        model.addAttribute("target","/index");

    }else{
        model.addAttribute("msg"," activation fail, wrong activate code entered");
        model.addAttribute("target","/index");

    }
        return "/site/operate-result";
    }


}
