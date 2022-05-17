package sxx.xwl.community.community.service;

import com.alibaba.fastjson.JSONObject;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.query.HighlightQueryBuilder;
import org.springframework.stereotype.Service;
import sxx.xwl.community.community.elasticsearch.DiscussPostRepository;
import sxx.xwl.community.community.entity.DiscussPost;

import java.io.IOException;
import java.util.*;

/**
@author sxx_27
@create 2022-05-17 13:10
*/
@Service
public class ESService {

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    //添加、修改帖子
    public void saveDiscussPost(DiscussPost discussPost){
        discussPostRepository.save(discussPost);
    }
    //删除帖子
    public void deleteDiscussPost(int id){
        discussPostRepository.deleteById(id);
    }
    //查询帖子
    public List<DiscussPost> searchDiscussPost(String keyword, int current , int limit) throws IOException {

        //discusspost为索引名，即表名
        SearchRequest searchRequest = new SearchRequest("discusspost");

        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.field("content");
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");

        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.multiMatchQuery(keyword, "title","content"))
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .from(current)//指定从哪里开始
                .size(limit)
                .highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

//        System.out.println(JSONObject.toJSON(searchResponse));

//        Map<String,Object> map = new HashMap<>();
//        map.put("totalCount", searchResponse.getHits().getTotalHits().value);
//        System.out.println("一共有："+ searchResponse.getHits().getTotalHits().value);

        List<DiscussPost> list = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits()){
            DiscussPost post = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);
            //处理高亮
            HighlightField title = hit.getHighlightFields().get("title");
            if (title !=null){
                post.setTitle(title.getFragments()[0].toString());
            }
            HighlightField content = hit.getHighlightFields().get("content");
            if (content !=null){
                post.setContent(content.getFragments()[0].toString());
            }
//            System.out.println(post);
            list.add(post);
        }
        return list;
    }

}
