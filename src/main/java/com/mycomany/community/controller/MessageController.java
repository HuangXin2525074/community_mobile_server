package com.mycomany.community.controller;

import com.mycomany.community.entity.Message;
import com.mycomany.community.entity.Page;
import com.mycomany.community.entity.User;
import com.mycomany.community.services.MessageService;
import com.mycomany.community.services.UserService;
import com.mycomany.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    // handle private message list
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page){

        User user = hostHolder.getUser();
        page.setLimit(5);
        page.setPath("/letter/list");


      List<Message> conversationList = messageService.findConversations(user.getId(),page.getOffset(),page.getLimit());

      List<Map<String,Object>> conversations = new ArrayList<>();
        if(conversations != null){
            for(Message message:conversationList){
                Map<String,Object> map = new HashMap<>();
                map.put("conversation",message);
                map.put("letterCount",messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount",messageService.findLetterUnreadCount(user.getId(),message.getConversationId()));
                int targetId = user.getId() == message.getFromId()? message.getToId():message.getFromId();
                map.put("target", userService.findUserById(targetId));

                conversations.add(map);

            }
        }
        model.addAttribute("conversations",conversations);

        // search number of total unread message
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(),null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        return "/site/letter";


    }
}
