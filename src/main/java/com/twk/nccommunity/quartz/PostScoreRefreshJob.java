package com.twk.nccommunity.quartz;

import com.twk.nccommunity.entity.DiscussPost;
import com.twk.nccommunity.service.DiscussPostService;
import com.twk.nccommunity.service.ElasticsearchService;
import com.twk.nccommunity.service.LikeService;
import com.twk.nccommunity.util.CommunityConstant;
import com.twk.nccommunity.util.RedisKeyUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import javax.management.RuntimeErrorException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostScoreRefreshJob implements Job, CommunityConstant {
    public static final Logger logger = LogManager.getLogger();

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private ElasticsearchService elasticsearchService;

    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2010-06-18 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化建站时间失败!", e);
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String scoreKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(scoreKey);
        if(operations.size() == 0){
            logger.info("※※※[任务取消]※※※,没有需要刷新的帖子");
        }
        logger.info("※※※[任务开始]※※※,正在刷新帖子分数");
        while(operations.size()>0){
            this.refresh((Integer)operations.pop());
        }

        logger.info("※※※[任务完成]※※※,帖子分数刷新成功");
    }

    private void refresh(int postId) {
        DiscussPost post = discussPostService.findDiscussPostById(postId);
        if(post == null){
            logger.error("不存在该帖子");
        }
        boolean wonderful = post.getStatus() == 1;
        int commentCount = post.getCommentCount();
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);
        Date createTime = post.getCreateTime();

        //计算分数公式为 log(是否精华 + 评论数*10 + 点赞数*2) + (帖子发表时间 - 建站时间)
        //log中的权重
        long w = (wonderful ? 75 : 0) + commentCount*10 + likeCount*2;
        double score = Math.log10(Math.max(w,1))
                + (createTime.getTime() - epoch.getTime())/(1000 * 3600 * 24);
        post.setScore(score);
        discussPostService.updatePostScore(postId,score);
        //搜索引擎配置
        elasticsearchService.saveDiscussPost(post);
    }
}
