package com.mycomany.community.controller;

import com.mycomany.community.entity.Comment;
import com.mycomany.community.entity.DiscussPost;
import com.mycomany.community.entity.Page;
import com.mycomany.community.entity.User;
import com.mycomany.community.services.CommentService;
import com.mycomany.community.services.DiscussPostService;
import com.mycomany.community.services.LikeService;
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
import com.mycomany.community.util.communityConstant;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements communityConstant {

  @Autowired
  private DiscussPostService discussPostService;

  @Autowired
  private HostHolder hostHolder;

  @Autowired
  private UserService userService;

  @Autowired
  private CommentService commentService;

  @Autowired
  private LikeService likeService;


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
  public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page){

    DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
    model.addAttribute("post",post);

    User user = userService.findUserById(post.getUserId());
    model.addAttribute("user",user);

    //display total Like for discussPost
    long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,discussPostId);
    model.addAttribute("likeCount",likeCount);

    // display Like status
    int likeStatus = hostHolder.getUser()==null? 0:
    likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_POST,discussPostId);
    model.addAttribute("likeStatus",likeStatus);

    page.setLimit(5);
    page.setPath("/discuss/detail/" + discussPostId);
    page.setRows(post.getCommentCount());

    // comment for post.
    // reply of the comment.
    List<Comment> commentList = commentService.findCommentsByEntity(ENTITY_TYPE_POST,post.getId(),page.getOffset(),page.getLimit());

    List<Map<String, Object>> commentVoList = new ArrayList<>();
    if(commentList!=null){
      for(Comment comment : commentList){
        // comment view object
        Map<String, Object> commentVo = new HashMap<>();
        // add comment
        commentVo.put("comment",comment);
        //add user.
        commentVo.put("user",userService.findUserById(comment.getUserId()));


        // display like count in comment
         likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,comment.getId());
        commentVo.put("likeCount",likeCount);

        // display Like status in comment
         likeStatus = hostHolder.getUser()==null? 0:
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,comment.getId());
        commentVo.put("likeStatus",likeStatus);



        // reply list
        List<Comment> replyList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT,comment.getId(),0,Integer.MAX_VALUE);

        List<Map<String, Object>> replyVoList = new ArrayList<>();
        if(replyList!=null) {
          for (Comment reply : replyList) {
            Map<String, Object> replyVo = new HashMap<>();
            // add reply Msg
            replyVo.put("reply", reply);

            // add reply user
            replyVo.put("user", userService.findUserById(reply.getUserId()));

            // reply by target
            User target = reply.getTargetId() ==0 ? null:userService.findUserById(reply.getTargetId());
            replyVo.put("target",target);

            // display like count in reply
            likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,reply.getId());
            replyVo.put("likeCount",likeCount);

            // display Like status in reply
            likeStatus = hostHolder.getUser()==null? 0:
                    likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,reply.getId());
            replyVo.put("likeStatus",likeStatus);

            replyVoList.add(replyVo);


          }
        }
        commentVo.put("replys",replyVoList);

        // add number of count of replys
        int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT,comment.getId());
        commentVo.put("replyCount",replyCount);

        commentVoList.add(commentVo);

      }

    }

    model.addAttribute("comments",commentVoList);

    return "/site/discuss-detail";

  }
}
