package pwd.allen.elastic.repository;

import pwd.allen.elastic.bean.Book;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;


public interface BookRepository extends ElasticsearchRepository<Book,Integer> {

    /**
     * 查询语句的转换可以参考官方文档给出的例子：https://docs.spring.io/spring-data/elasticsearch/docs/3.0.6.RELEASE/reference/html/
     * 该方法对应的查询语句：{"bool" : {"must" : {"field" : {"bookName" : {"query" : "?*","analyze_wildcard" : true}}}}}
     *
     * @param bookName
     * @return
     */
    public List<Book> findByBookNameLike(String bookName);

}
