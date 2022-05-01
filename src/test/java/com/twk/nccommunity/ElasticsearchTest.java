package com.twk.nccommunity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.twk.nccommunity.dao.DiscussPostMapper;
import com.twk.nccommunity.dao.elasticsearch.DiscussPostRepository;
import com.twk.nccommunity.entity.DiscussPost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

@SpringBootTest
@ContextConfiguration(classes = NcCommunityApplication.class)
public class ElasticsearchTest {
    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Qualifier("client")
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Test
    public void insertAll(){
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(156,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(149,0,100));
    }

    @Test
    public void testSearchByRepository() throws IOException {
        SearchRequest request = new SearchRequest("discusspost");
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.field("content");
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<em>");
        highlightBuilder.postTags("</em>");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .from(0)
                .size(10)
                .highlighter(highlightBuilder);
        request.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        LinkedList<DiscussPost> list = new LinkedList<>();
        for(SearchHit hit:searchResponse.getHits().getHits()){
            DiscussPost post = new DiscussPost();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            post.setId(Integer.parseInt(sourceAsMap.get("id").toString()));
            post.setUserId(Integer.parseInt(sourceAsMap.get("userId").toString()));
            post.setTitle(sourceAsMap.get("title").toString());
            post.setContent(sourceAsMap.get("content").toString());
            post.setStatus(Integer.parseInt(sourceAsMap.get("status").toString()));
            post.setCreateTime(new Date(Long.parseLong(sourceAsMap.get("createTime").toString())));
            post.setCommentCount(Integer.parseInt(sourceAsMap.get("commentCount").toString()));
            HighlightField title = hit.getHighlightFields().get("title");
            if(title != null){
                post.setTitle(title.getFragments()[0].toString());
            }
            HighlightField contentField = hit.getHighlightFields().get("content");
            if (contentField != null) {
                post.setContent(contentField.getFragments()[0].toString());
            }
            System.out.println(post);
            list.add(post);
        }

    }
}
