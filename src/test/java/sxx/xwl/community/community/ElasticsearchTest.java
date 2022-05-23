package sxx.xwl.community.community;

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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.ContextConfiguration;
import sxx.xwl.community.community.dao.DiscussPostMapper;
import sxx.xwl.community.community.elasticsearch.DiscussPostRepository;
import sxx.xwl.community.community.entity.DiscussPost;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author sxx_27
 * @create 2022-05-15 21:24
 */
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTest {

    @Autowired
    private DiscussPostMapper discussMapper;

    @Autowired
    private DiscussPostRepository discussRepository;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    //添加一条
    @Test
    public void test() {
        discussRepository.save(discussMapper.selectDiscussPostById(241));
        discussRepository.save(discussMapper.selectDiscussPostById(242));
        discussRepository.save(discussMapper.selectDiscussPostById(243));
    }
    //添加一堆
    @Test
    public void test2(){
//        discussRepository.saveAll(discussMapper.selectDiscussPosts(101, 0, 100));
//        discussRepository.saveAll(discussMapper.selectDiscussPosts(102, 0, 100));
//        discussRepository.saveAll(discussMapper.selectDiscussPosts(103, 0, 100));
//        discussRepository.saveAll(discussMapper.selectDiscussPosts(111, 0, 100));
//        discussRepository.saveAll(discussMapper.selectDiscussPosts(112, 0, 100));
//        discussRepository.saveAll(discussMapper.selectDiscussPosts(131, 0, 100));
//        discussRepository.saveAll(discussMapper.selectDiscussPosts(132, 0, 100));
//        discussRepository.saveAll(discussMapper.selectDiscussPosts(132, 0, 100));
//        discussRepository.saveAll(discussMapper.selectDiscussPosts(134, 0, 100));
    }

    //修改
    @Test
    public void test3(){
        DiscussPost post = discussMapper.selectDiscussPostById(231);
        post.setContent("??????水？");
        discussRepository.save(post);
    }
    //删除
    @Test
    public void test4(){
        discussRepository.deleteById(231);
    }
    //搜索(不带高亮
    @Test
    public void test5() throws IOException {

        //discusspost为索引名，即表名
        SearchRequest searchRequest = new SearchRequest("discusspost");
        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.multiMatchQuery("互联网寒冬", "title","content"))
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .from(0)//指定从哪里开始
                .size(10);//需要查出的总记录条数

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        System.out.println(JSONObject.toJSON(searchResponse));

        List<DiscussPost> list = new LinkedList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()){
            DiscussPost post = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);
            System.out.println(post);
            list.add(post);
        }
    }

    //搜索(带高亮
    @Test
    public void test6() throws IOException {

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
                .query(QueryBuilders.multiMatchQuery("互联网寒冬", "title","content"))
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .from(0)//指定从哪里开始
                .size(10)//需要查出的总记录条数
                .highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        System.out.println(JSONObject.toJSON(searchResponse));

//        System.out.println("一共有："+ searchResponse.getHits().getTotalHits().value);

        List<DiscussPost> list = new LinkedList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()){
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
            System.out.println(post);
            list.add(post);
        }
    }


}
