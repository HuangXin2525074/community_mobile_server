package com.mycomany.community.services;


import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.mycomany.community.dao.DiscussPostMapper;
import com.mycomany.community.entity.DiscussPost;
import com.mycomany.community.util.SensitiveFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostService {

    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;


    @PostConstruct
    public void init(){

        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Override
                    public List<DiscussPost> load(String key) throws Exception {
                        if(key == null || key.length() == 0){
                            throw new IllegalArgumentException("Value error");
                        }

                        String[] params = key.split(":");
                        if(params == null || params.length != 2){
                            throw new IllegalArgumentException("Value error");
                        }

                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        logger.debug("load post list from Database.");
                        return discussPostMapper.selectDiscussPosts(0, offset, limit,1);
                    }
                });


        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public Integer load(Integer key) throws Exception {
                        logger.debug("load post list from Database.");
                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });

    }

   // load list of discusspost into cache;
    private LoadingCache<String,List<DiscussPost>> postListCache;


    //load number of discusspost into cahce;
    private LoadingCache<Integer,Integer> postRowsCache;


    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode){

        if(userId == 0 && orderMode ==1){
            return postListCache.get(offset+":"+limit);
        }
        logger.debug("load post list from Database.");
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }

    public int findDiscussPostRows(int userId){
        if(userId == 0){
            return postRowsCache.get(userId);
        }

        logger.debug("load post rows from Database");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost post){
        if(post == null){
            throw new IllegalArgumentException("no empty text allow!");
        }

        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));

        //implement sensitive Filer
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent((sensitiveFilter.filter(post.getContent())));

        return discussPostMapper.insertDiscussPost(post);
    }

    public DiscussPost findDiscussPostById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }

    public int updateCommentCount(int id, int commentCount){
        return discussPostMapper.updateCommentCount(id,commentCount);
    }

    public int updateType(int id, int type){
        return discussPostMapper.updateType(id,type);
    }

    public int updateStatus(int id, int status){
        return discussPostMapper.updateStatus(id,status);
    }

    public int updateScore(int id, double score){
        return discussPostMapper.updateScore(id,score);
    }


}
