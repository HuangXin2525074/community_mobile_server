package com.mycomany.community.controller;

import com.mycomany.community.entity.User;
import com.mycomany.community.services.UserService;
import com.mycomany.community.util.HostHolder;
import com.mycomany.community.util.communityUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

  @RequestMapping(path = "/setting",method = RequestMethod.GET)
  public String getSettingPage(){
      return "/site/setting";
  }

  @RequestMapping(path ="/upload",method= RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
     if(headerImage ==null){
         model.addAttribute("error","please select image");
         return "/site/setting";
     }

     String fileName = headerImage.getOriginalFilename();
     String suffix = fileName.substring(fileName.lastIndexOf("."));
      System.out.println(suffix);
     if(StringUtils.isBlank(suffix)){
         model.addAttribute("error","please select image");
         return "/site/setting";
     }

     // generate random file name
      fileName = communityUtil.generateUUID() + suffix;

      File dest = new File(uploadPath+ "/" + fileName);
      try {
          // save file into dest path
          headerImage.transferTo(dest);
      } catch (IOException e) {
         logger.error("upload file fail" + e.getMessage());
         throw  new RuntimeException("upload file fail, error in server-side");
      }

      // upload current path for headerUrl
      // http://localhost:8080/community/user/header/xxx.png

      User user = hostHolder.getUser();
      String headerUrl = domain + contextPath +"/user/header/"+fileName;
      userService.updateHeader(user.getId(),headerUrl);


      return "redirect:/index";
  }

  @RequestMapping(path = "/header/{fileName}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){

      fileName = uploadPath +"/" + fileName;

      String suffix = fileName.substring(fileName.lastIndexOf(".")+1);

      response.setContentType("image/" + suffix);

          try(FileInputStream fis = new FileInputStream(fileName);
              OutputStream os = response.getOutputStream();
              ){

              byte[] buffer = new byte[1024];
              int b = 0;
              while((b = fis.read(buffer)) != -1){
                  os.write(buffer, 0, b);
              }

          } catch (IOException e) {
              logger.error("login image fail", e.getMessage());

          }


      }

      @RequestMapping(path = "/updatePassword", method = RequestMethod.POST)
      public String updatePassword(Model model, String password, String newPassword, String confirmPassword){

          User user = hostHolder.getUser();
          Map<String,Object> map = userService.updatePassword(user.getId(),password,newPassword,confirmPassword);
          if(map==null || map.isEmpty()){
              return "redirect:/index";
          }else{
              model.addAttribute("passwordMsg",map.get("passwordMsg"));
              model.addAttribute("newPasswordMsg",map.get("newPasswordMsg"));
              model.addAttribute("confirmPasswordMsg",map.get("confirmPasswordMsg"));

              return "/site/setting";
          }

      }



}