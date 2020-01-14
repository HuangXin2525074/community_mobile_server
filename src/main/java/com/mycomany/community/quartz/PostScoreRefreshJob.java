package com.mycomany.community.quartz;

import com.mycomany.community.entity.DiscussPost;
import com.mycomany.community.services.DiscussPostService;
import com.mycomany.community.services.ElasticsearchService;
import com.mycomany.community.services.LikeService;
import com.mycomany.community.util.RedisKeyUtil;
import com.mycomany.community.util.communityConstant;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostScoreRefreshJob implements Job, communityConstant {

    private static Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;


    private static final Date epoch;

    static{
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("init error!", e);
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);
        if(operations.size() == 0){
            logger.info("Task cancel, No new post need to refresh");
            return;
        }

        logger.info("[task start]: the task processing...");
         while(operations.size() > 0){
             this.refresh((Integer)operations.pop());
         }

        logger.info("[task end]: task refresh end");


    }

    private void refresh(int postId){
        DiscussPost post = discussPostService.findDiscussPostById(postId);

        if(post == null){
            logger.error("this post do not exist:" + postId);
            return;
        }

        // is that hot post?
        boolean wonderful = post.getStatus() == 1;
        // number of commend;
        int commentCount = post.getCommentCount();

        // number of Like
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,postId);

        // calcaute the formula for score of the discussPost
        double w = (wonderful ? 75: 0) + commentCount*10 +likeCount*2;

        double score = Math.log10(Math.max(w,1))
                + (post.getCreateTime().getTime() - epoch.getTime())/(1000*3600*24);

        // update the score of  the discusspost
        discussPostService.updateScore(postId,score);

        // update the score to elastic search.
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);


    }
}
