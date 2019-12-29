package com.mycomany.community.controller;

import com.mycomany.community.entity.DiscussPost;
import com.mycomany.community.entity.User;
import com.mycomany.community.services.DiscussPostService;
import com.mycomany.community.services.UserService;
import com.mycomany.community.util.HostHolder;
import com.mycomany.community.util.communityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController {

  @Autowired
  private DiscussPostService discussPostService;

  @Autowired
  private HostHolder hostHolder;

  @Autowired
  private UserService userService;


  @RequestMapping(path = "/add", method = RequestMethod.POST)
   @ResponseBody
    public String addDiscussPost(String title, String content){

      User user = hostHolder.getUser();
      if(user == null){
          return communityUtil.getJSONString(403,"please login to your account");
      }

      DiscussPost post = new DiscussPost();
      post.setUserId(user.getId());
      post.setTitle(title);
      post.setContent(content);
      post.setCreateTime(new Date());
      discussPostService.addDiscussPost(post);

      return communityUtil.getJSONString(0,"Post content success");
  }

  @RequestMapping(path = "/detail/{discussPostId}",method = RequestMethod.GET)
  public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model){

    DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
    model.addAttribute("post",post);

    User user = userService.findUserById(post.getUserId());
    model.addAttribute("user",user);

    return "/site/discuss-detail";



  }
}
