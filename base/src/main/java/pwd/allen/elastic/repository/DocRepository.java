package pwd.allen.elastic.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import pwd.allen.elastic.bean.Doc;

import java.util.List;


public interface DocRepository extends ElasticsearchRepository<Doc, String> {

    /**
     * 查询语句的转换可以参考官方文档给出的例子：https://docs.spring.io/spring-data/elasticsearch/docs/3.0.6.RELEASE/reference/html/
     * 该方法对应的查询语句：{"bool" : {"must" : {"field" : {"textSmart" : {"query" : "?*","analyze_wildcard" : true}}}}}
     *
     * @param textSmart
     * @return
     */
    public List<Doc> findByTextSmartLike(String textSmart);

}
