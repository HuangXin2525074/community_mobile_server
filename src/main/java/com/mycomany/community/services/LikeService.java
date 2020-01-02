package com.mycomany.community.services;

import com.mycomany.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    //Like function
    public void like(int userId, int entityType, int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey,userId);
       if(isMember){
           redisTemplate.opsForSet().remove(entityLikeKey,userId);
       }else{
           redisTemplate.opsForSet().add(entityLikeKey,userId);
       }
    }

    // find number of likes
    public long findEntityLikeCount(int entityType, int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    //find particular Like status.
    public int findEntityLikeStatus(int userId,int entityType, int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey,userId)?1:0;
    }


}
