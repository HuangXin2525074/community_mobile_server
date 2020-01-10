package com.mycomany.community.controller;

import com.mycomany.community.entity.Comment;
import com.mycomany.community.entity.DiscussPost;
import com.mycomany.community.entity.Event;
import com.mycomany.community.event.EventProducer;
import com.mycomany.community.services.CommentService;
import com.mycomany.community.services.DiscussPostService;
import com.mycomany.community.util.HostHolder;
import com.mycomany.community.util.communityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements communityConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;



    @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment){
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        //fire commend event
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", discussPostId);
        if(comment.getEntityType() == ENTITY_TYPE_POST){

        DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
        event.setEntityUserId(target.getUserId());

        }else if(comment.getEntityType() == ENTITY_TYPE_COMMENT){

        Comment target = commentService.findCommentById(comment.getEntityId());
        event.setEntityUserId(target.getUserId());

        }

        eventProducer.fireEvent(event);

        if(comment.getEntityType() == ENTITY_TYPE_POST){

            // trigger the event
             event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(event);


        }


        return "redirect:/discuss/detail/" + discussPostId;
    }

}
