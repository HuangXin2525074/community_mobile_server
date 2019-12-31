package com.mycomany.community.dao;

import com.mycomany.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    // search message list from current user.
    List<Message> selectConversations(int userId, int offset, int limit);

    //search number of message
    int selectConversationCount(int userId);

    //search particular message list.
    List<Message> selectLetters(String conversationId,int offset, int limit);

    int selectLettersCount(String conversationId);

    int selectLetterUnreadCount(int userId, String conversationId);

    // insert prviate message.
    int insertMessage(Message message);

    // update status of message.
    int updateStatus(List<Integer> ids, int status);

}
