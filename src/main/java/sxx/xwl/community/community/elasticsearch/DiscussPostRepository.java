package sxx.xwl.community.community.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import sxx.xwl.community.community.entity.DiscussPost;

/**
 * @author sxx_27
 * @create 2022-05-15 21:23
 */
@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, Integer> {
}
