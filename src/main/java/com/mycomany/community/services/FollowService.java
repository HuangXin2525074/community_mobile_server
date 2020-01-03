package com.mycomany.community.services;

import com.mycomany.community.entity.User;
import com.mycomany.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import com.mycomany.community.util.communityConstant;

import java.util.*;

@Service
public class FollowService implements communityConstant{

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    public void follow(int userId,int entityType, int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {

                String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType,entityId);

                operations.multi();

                operations.opsForZSet().add(followeeKey,entityId,System.currentTimeMillis());
                operations.opsForZSet().add(followerKey,userId,System.currentTimeMillis());

                return operations.exec();
            }
        });
    }

    public void unfollow(int userId,int entityType, int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {

                String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType,entityId);

                operations.multi();

                operations.opsForZSet().remove(followeeKey,entityId);
                operations.opsForZSet().remove(followerKey,userId);

                return operations.exec();
            }
        });
    }

    // find number of following
    public long findFolloweeCount(int userId, int entityType){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);

    }

    // find number of follower.
    public long findFollowerCount(int entityType, int entityId){
        String followerKey = RedisKeyUtil.getFollowerKey(entityType,entityId);
         return redisTemplate.opsForZSet().zCard(followerKey);
    }

    // find follow status
    public boolean hasFollowed(int userId, int entityType, int entityId){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) !=null;
    }

    public List<Map<String,Object>> findFollowees(int userId,int offset, int limit){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,ENTITY_TYPE_USER);

       Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey,offset,offset + limit -1);

       if(targetIds == null){
           return null;
       }

        List<Map<String,Object>> list = new ArrayList<>();
       for(Integer targetId:targetIds){
           Map<String,Object> map = new HashMap<>();
           User user = userService.findUserById(targetId);
           map.put("user",user);
          Double score = redisTemplate.opsForZSet().score(followeeKey,targetId);
          map.put("followTime", new Date(score.longValue()));
          list.add(map);
       }

       return list;

    }

    public List<Map<String,Object>> findFollowers(int userId,int offset, int limit){
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER,userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey,offset,offset+limit-1);
        if(targetIds ==null){
            return null;
        }

        List<Map<String,Object>> list = new ArrayList<>();
        for(Integer targetId:targetIds){
            Map<String,Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user",user);
            Double score = redisTemplate.opsForZSet().score(followerKey,targetId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }

        return list;


    }


}
