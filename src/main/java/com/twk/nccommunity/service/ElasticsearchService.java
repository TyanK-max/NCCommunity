package com.twk.nccommunity.service;

import com.twk.nccommunity.dao.elasticsearch.DiscussPostRepository;
import com.twk.nccommunity.entity.DiscussPost;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PrimitiveIterator;

@Service
public class ElasticsearchService {

    @Autowired
    private DiscussPostRepository discussPostRepository;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    public void saveDiscussPost(DiscussPost discussPost){
        discussPostRepository.save(discussPost);
    }
    public void deleteDiscussPost(int id){
        discussPostRepository.deleteById(id);
    }
    
    public List<Object> searchDiscussPost(String keyword, int current, int limit){
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword,"title","content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(current,limit))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        
        SearchHits<DiscussPost> search = elasticsearchRestTemplate.search(searchQuery, DiscussPost.class);
        List<SearchHit<DiscussPost>> searchHits = search.getSearchHits();
        ArrayList<DiscussPost> discussPosts = new ArrayList<>();
        for (SearchHit<DiscussPost> searchHit : searchHits) {
            Map<String, List<String>> highlightFields = searchHit.getHighlightFields();
            searchHit.getContent().setTitle(highlightFields.get("title") == null ? searchHit.getContent().getTitle() : highlightFields.get("title").get(0));
            searchHit.getContent().setContent(highlightFields.get("content") == null ? searchHit.getContent().getContent() : highlightFields.get("content").get(0));
            discussPosts.add(searchHit.getContent());
        }
        List<Object> res = new ArrayList<>();
        res.add((int)search.getTotalHits());
        res.add(discussPosts);
        return res;
    }
}
