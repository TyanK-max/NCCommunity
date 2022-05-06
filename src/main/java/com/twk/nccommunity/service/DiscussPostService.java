package com.twk.nccommunity.service;

import com.twk.nccommunity.dao.DiscussPostMapper;
import com.twk.nccommunity.entity.DiscussPost;
import com.twk.nccommunity.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {
    @Autowired
    DiscussPostMapper discussPostMapper;

    @Autowired
    SensitiveFilter sensitiveFilter;

    /**
     * 查找帖子
     * @param userId 当前用户id
     * @param offset 当前行
     * @param limit 分页
     */
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit,int orderMode){
        return discussPostMapper.selectDiscussPosts(userId,offset,limit,orderMode);
    }

    public int findDiscussPostRows(int userId){
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost post){
        if(post == null){
            throw new IllegalArgumentException("发布帖子失败,帖子不能为空");
        }
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));

        post.setTitle(sensitiveFilter.goToFilter(post.getTitle()));
        post.setContent(sensitiveFilter.goToFilter(post.getContent()));
        return discussPostMapper.insertDiscussPost(post);
    }

    public DiscussPost findDiscussPostById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }

    public int updateCommentCount(int id,int commentCount){
        return discussPostMapper.updateCommentCount(id,commentCount);
    }

    public int updatePostType(int id,int type){
        return discussPostMapper.updatePostType(id,type);
    }
    public int updatePostStatus(int id,int status){
        return discussPostMapper.updatePostStatus(id,status);
    }
    public int updatePostScore(int id,double score){
        return discussPostMapper.updatePostScore(id,score);
    }
}
