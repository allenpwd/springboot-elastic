package pwd.allen.elastic;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.health.ClusterIndexHealth;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.filter.FiltersAggregator;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

/**
 * 使用transport client操作ES
 * 注意：Elasticsearch从7版本开始TransportClient已经过时了不再推荐使用，将在8.0版本删除
 *
 * @deprecated
 * @author 门那粒沙
 * @create 2020-04-19 14:08
 **/
@Slf4j
public class TransportClientTest {
    
    private TransportClient client;
    
    @Before
    public void init() throws UnknownHostException {
        //指定es集群
        Settings settings = Settings.builder().put("cluster.name", "elasticsearch").build();

        //创建访问es服务器的客户端
        client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new TransportAddress(InetAddress.getByName("192.168.118.201"), 9300));
    }

    @After
    public void destroy() {
        client.close();
    }

    @Test
    public void index() {
        client.prepareIndex("index_java", null);
    }

    /**
     * 添加数据
     * PUT /index_pwd/_doc/10
     * {...}
     *
     * 查询文档
     * GET index_pwd/_doc/10
     *
     * 删除文档
     * DELETE index_pwd/_doc/10
     */
    @Test
    public void document() throws IOException, ExecutionException, InterruptedException {
        XContentBuilder doc = XContentFactory.jsonBuilder()
                .startObject()
                .field("int", 55)
                .field("float", 55.5)
                .field("date", "2020-04-19")
                .field("text_stand", "通过TransportClent添加数据")
                .field("text_smart", "通过TransportClent添加数据")
                .field("text_max_word", "通过TransportClent添加数据")
                .field("text_interests", new String[]{"击剑","橄榄球"})
                .endObject();

        //添加文档 id未指定则由es生成，通过doc设置的id是无效的
        IndexResponse indexResponse = client.prepareIndex("index_pwd", "_doc", "10")
                .setSource(doc).get();
        log.info("-------------添加文档：{}", indexResponse.status().toString());

        //查询文档
        GetResponse getResponse = client.prepareGet("index_pwd", "_doc", "10").execute().actionGet();
        log.info("-------------查询文档：{}", getResponse.getSourceAsString());

        //更新文档
        UpdateRequest updateRequest = new UpdateRequest("index_pwd", "_doc", "10")
                .doc(XContentFactory.jsonBuilder().startObject().field("int", 56).endObject());
        UpdateResponse updateResponse = client.update(updateRequest).get();
        log.info("-------------更新文档：{}", updateResponse.status().toString());


        //删除文档
        DeleteResponse deleteResponse = client.prepareDelete("index_pwd", "_doc", "10").execute().actionGet();
        log.info("-----------------删除文档：{}", deleteResponse.status().toString());
    }

    /**
     * upsert 有则更新无则新增
     */
    @Test
    public void upsert() throws IOException, ExecutionException, InterruptedException {
        IndexRequest indexRequest = new IndexRequest("index_pwd", "_doc", "10")
                .source(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("int", 55)
                        .field("float", 55.5)
                        .field("date", "2020-04-19")
                        .field("text_stand", "通过TransportClent添加数据")
                        .field("text_smart", "通过TransportClent添加数据")
                        .field("text_max_word", "通过TransportClent添加数据")
                        .field("text_interests", new String[]{"击剑","橄榄球"})
                        .endObject());

        UpdateRequest updateRequest = new UpdateRequest("index_pwd", "_doc", "10")
                .doc(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("int", 56)
                        .endObject()).upsert(indexRequest);

        UpdateResponse updateResponse = client.update(updateRequest).get();
        //如果记录不存在返回CREATED,否则返回OK
        log.info("-----------upsert：{}", updateResponse.status().toString());
    }


    /**
     * mget查询数据
     *
     */
    @Test
    public void mget() {
        MultiGetResponse multiGetItemResponses = client.prepareMultiGet()
                .add("index_pwd", "_doc", "1", "2")
                .add("index_test", "_doc", "1")
                .get();

        for (MultiGetItemResponse item : multiGetItemResponses) {
            GetResponse response = item.getResponse();
            if (response != null && response.isExists()) {
                log.info("-----------------mget：{}", response.getSourceAsString());
            }
        }
    }

    /**
     * 使用bulk批量操作
     */
    @Test
    public void bulk() throws IOException {
        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk("index_pwd", "_doc")
                .add(client.prepareIndex().setId("11").setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("int", 55)
                        .field("float", 55.5)
                        .field("date", "2020-04-19")
                        .field("text_stand", "通过TransportClent添加数据")
                        .field("text_smart", "通过TransportClent添加数据")
                        .field("text_max_word", "通过TransportClent添加数据")
                        .field("text_interests", new String[]{"击剑", "橄榄球"})
                        .endObject()))
                .add(client.prepareUpdate().setId("11").setDoc(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("int", 100)
                        .endObject()))
                .add(client.prepareDelete().setId("11"));

        BulkResponse bulkItemResponses = bulkRequestBuilder.get();
        log.info("----------------bulk结果：{}", bulkItemResponses.status());

        if (bulkItemResponses.hasFailures()) {
            log.info("--------------失败了：{}", bulkItemResponses.buildFailureMessage());
        }

        for (BulkItemResponse item : bulkItemResponses) {
            DocWriteResponse response = item.getResponse();
            log.info("--------------bulk item：{}", response.getResult());
        }
    }

    /**
     * 查询并删除
     */
    @Test
    public void query4Delete() {
        //把匹配的记录做删除操作
        BulkByScrollResponse bulkByScrollResponse = DeleteByQueryAction.INSTANCE.newRequestBuilder(client)
                .filter(QueryBuilders.matchQuery("text_stand", "哈"))
                .source("index_test")
                .get();

        long deleted = bulkByScrollResponse.getDeleted();
        log.info("----------------删除文档的个数：{}", deleted);
    }

    /**
     * 查询所有
     */
    @Test
    public void matchAll() {
        //查询第一页 两条记录
        MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
        SearchResponse searchResponse = client.prepareSearch("index_pwd")
                .setQuery(matchAllQueryBuilder)
                .setSize(2)
                .setFrom(0).get();

        SearchHits hits = searchResponse.getHits();

        for (SearchHit hit : hits) {
            log.info("-------------：{}", hit.getSourceAsString());
        }
    }

    /**
     *  match查询
     */
    @Test
    public void match() {
        //查询兴趣有画字的
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("interests", "画");

        SearchResponse searchResponse = client.prepareSearch("index_pwd")
                .setQuery(matchQueryBuilder)
                .setSize(2)
                .get();

        SearchHits hits = searchResponse.getHits();

        for (SearchHit hit : hits) {
            log.info("-------------：{}", hit.getSourceAsString());
        }
    }

    /**
     *  multimatch查询
     */
    @Test
    public void multiMatch() {
        //查询多个字段里含有 "马"字的记录
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("马", "text_stand", "text_smart");

        SearchResponse searchResponse = client.prepareSearch("index_pwd")
                .setQuery(multiMatchQueryBuilder)
                .setSize(2)
                .get();

        SearchHits hits = searchResponse.getHits();

        for (SearchHit hit : hits) {
            log.info("-------------：{}", hit.getSourceAsString());
        }
    }

    /**
     *  term查询
     */
    @Test
    public void term() {
        SearchResponse searchResponse = client.prepareSearch("index_pwd")
                .setQuery(QueryBuilders.termQuery("text_smart", "宝马"))
                .setSize(2)
                .get();
        SearchHits hits = searchResponse.getHits();
        for (SearchHit hit : hits) {
            log.info("-------------：{}", hit.getSourceAsString());
        }
    }

    /**
     *  terms查询
     */
    @Test
    public void terms() {
        SearchResponse searchResponse = client.prepareSearch("index_pwd")
                .setQuery(QueryBuilders.termsQuery("text_smart", "宝马", "游戏"))
                .setSize(2)
                .get();
        SearchHits hits = searchResponse.getHits();
        for (SearchHit hit : hits) {
            log.info("-------------：{}", hit.getSourceAsString());
        }
    }

    /**
     * 其他查询
     */
    @Test
    public void range() {
        QueryBuilder builder = null;

        //范围查询：float （20,30]
        builder = QueryBuilders.rangeQuery("float")
                .from(20)
                .lte(30);

        //前缀查询
        builder = QueryBuilders.prefixQuery("text_max_word", "游戏");

        //wildcard查询 支持通配符
        builder = QueryBuilders.wildcardQuery("text_max_word", "英*");

        //fuzzy查询 相关度满足一点条件
        //能查出一条包含"算不算"这个分词的记录
        builder = QueryBuilders.fuzzyQuery("text_max_word", "算不酸");

        //type查询
        builder = QueryBuilders.typeQuery("_doc");

        //ids查询
        builder = QueryBuilders.idsQuery("_doc")
                .addIds("1", "3");

        SearchResponse searchResponse = client.prepareSearch("index_pwd")
                .setQuery(builder)
                .get();

        SearchHits hits = searchResponse.getHits();
        for (SearchHit hit : hits) {
            log.info("-------------：{}", hit.getSourceAsString());
        }
    }


    /**
     * 聚合查询
     */
    @Test
    public void aggs() {
        //{"pwd_max":{"value":55.5}}
        AggregationBuilder builder_max = AggregationBuilders.max("pwd_max")
                .field("float");
        AggregationBuilder builder_min = AggregationBuilders.min("pwd_min")
                .field("date");
        AggregationBuilder builder_avg = AggregationBuilders.avg("pwd_avg")
                .field("int");
        AggregationBuilder builder_card = AggregationBuilders.cardinality("pwd_card")
                .field("int");
        //{"pwd_sum":{"value":132.0}}
        AggregationBuilder builder_sum = AggregationBuilders.sum("pwd_sum")
                .field("int");

        //按照int分组，然后组内统计float的平均值，结果按照平均值升序排序
        //{"group_of_int":{"doc_count_error_upper_bound":0,"sum_other_doc_count":0,"buckets":[{"key":33,"doc_count":1,"avg_of_float":{"value":12.5}},{"key":22,"doc_count":1,"avg_of_float":{"value":22.5}},{"key":11,"doc_count":2,"avg_of_float":{"value":38.95000076293945}},{"key":55,"doc_count":1,"avg_of_float":{"value":55.5}}]}}
        AggregationBuilder builder_group = AggregationBuilders.terms("group_of_int")
                .field("int")
                .order(BucketOrder.aggregation("avg_of_float", true))
                .subAggregation(AggregationBuilders.avg("avg_of_float").field("float"));

        //使用filter统计文档个数
        //{"pwd_filter":{"doc_count":5}}
        AggregationBuilder builder_filter = AggregationBuilders.filter("pwd_filter", QueryBuilders.rangeQuery("int").gte(10));
        //{"pwd_filters":{"buckets":{"float=12.5":{"doc_count":1},"text_stand":{"doc_count":1}}}}
        AggregationBuilder builder_filters = AggregationBuilders.filters("pwd_filters"
                , new FiltersAggregator.KeyedFilter("float=12.5", QueryBuilders.termQuery("float", 12.5))
                , new FiltersAggregator.KeyedFilter("text_stand", QueryBuilders.matchQuery("text_stand", "is")));


        //统计范围区间的文档个数
        //统计int 区间 [,10) [10,20) [20,)
        //{"pwd_range":{"buckets":[{"key":"*-10.0","to":10.0,"doc_count":0},{"key":"10.0-20.0","from":10.0,"to":20.0,"doc_count":2},{"key":"20.0-*","from":20.0,"doc_count":3}]}}
        AggregationBuilder builder_range = AggregationBuilders.range("pwd_range")
                .field("int")
                .addUnboundedTo(10) //[,10)
                .addRange(10, 20)   //[10,20)
                .addUnboundedFrom(20);  //[20,)

        //统计interests为空的记录个数
        //结果：{"pwd_missing":{"doc_count":2}}
        //不加keyword会报错（分词的字段不支持聚合、排序）：Fielddata is disabled on text fields by default. Set fielddata=true on [interests] in order to load fielddata in memory by uninverting the inverted index. Note that this can however use significant memory. Alternatively use a keyword field instead
        AggregationBuilder builder_missing = AggregationBuilders.missing("pwd_missing")
                .field("interests.keyword");

        SearchResponse response = client.prepareSearch("index_pwd")
                .addAggregation(builder_max)
                .addAggregation(builder_min)
                .addAggregation(builder_avg)
                .addAggregation(builder_card)
                .addAggregation(builder_sum)
                .addAggregation(builder_group)
                .addAggregation(builder_filter)
                .addAggregation(builder_filters)
                .addAggregation(builder_range)
                .addAggregation(builder_missing)
                .get();

        for (Aggregation aggregation : response.getAggregations()) {
            log.info("-------------{}", aggregation);
        }
    }

    /**
     * 精确查询
     */
    @Test
    public void query() {
        QueryBuilder builder = null;

        //term查询
        builder = QueryBuilders.commonTermsQuery("text_smart", "游戏");

        // 按查询条件查找
        // GET index_pwd/_search?q=+this int:22   不含有you且int为22的记录
        builder = QueryBuilders.queryStringQuery("-you int:22");
        // GET index_pwd/_search?q=+this int:22   不含有you或者int为22的记录
        builder = QueryBuilders.simpleQueryStringQuery("-you int:22");

        //组合查询
        //interests含有"绘画"但不含有"旅游"、或者int=55；结果过滤出date大于等于2020-01-01的
        builder = QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("interests", "绘画"))
                .mustNot(QueryBuilders.matchQuery("interests", "旅游"))
                .should(QueryBuilders.matchQuery("int", 55))
                .filter(QueryBuilders.rangeQuery("date")
                        .gte("2020-01-01"));

        //constantscore 不计算相关度的查询
        builder = QueryBuilders.constantScoreQuery(QueryBuilders.termQuery("text_stand", "this"));

        SearchResponse searchResponse = client.prepareSearch("index_pwd")
                .setQuery(builder)
                .get();

        SearchHits hits = searchResponse.getHits();
        for (SearchHit hit : hits) {
            log.info("-------------：{}", hit.getSourceAsString());
        }
    }

    /**
     * 集群管理
     */
    @Test
    public void cluster() {
        ClusterHealthResponse healthResponse = client.admin().cluster().prepareHealth().get();

        log.info("------------------------------------------");

        log.info("clusterName={}", healthResponse.getClusterName());//集群名
        log.info("getNumberOfDataNodes={}", healthResponse.getNumberOfDataNodes());//存储数据的节点
        log.info("getNumberOfNodes={}", healthResponse.getNumberOfNodes());//所有节点

        for (ClusterIndexHealth health : healthResponse.getIndices().values()) {
            log.info("index={},numberOfShards={},numberOfReplicas={},status={}"
                    , health.getIndex()
                    , health.getNumberOfShards()
                    , health.getNumberOfReplicas(), health.getStatus());
        }
    }

}
