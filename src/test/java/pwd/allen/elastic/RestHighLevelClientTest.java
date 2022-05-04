package pwd.allen.elastic;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.admin.indices.analyze.DetailAnalyzeResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.*;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 在Elasticsearch7.15版本之后，Elasticsearch官方将它的高级客户端RestHighLevelClient标记为弃用状态
 * 同时推出了全新的Java API客户端Elasticsearch Java API Client，该客户端也将在Elasticsearch8.0及以后版本中成为官方推荐使用的客户端。
 * Elasticsearch 8移除了RestHighLevelClient
 *
 * @author 门那粒沙
 * @create 2022-05-01 21:17
 **/
@Slf4j
public class RestHighLevelClientTest {

    private RestHighLevelClient client;

    private static final String INDEX_NAME = "rest_high_level_client";

    @Before
    public void init() throws IOException {
        String host = null;
        Properties pps = new Properties();
        pps.load(RestHighLevelClientTest.class.getClassLoader().getResourceAsStream("application.properties"));
        host = pps.getProperty("spring.elasticsearch.jest.uris");
        host = host.replace("http://", "");

        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(host)
                .build();
        client = RestClients.create(clientConfiguration).rest();


//        RestClientBuilder builder = RestClient.builder(
//                new HttpHost("21.145.229.153",9200,"http"),
//                new HttpHost("21.145.229.253",9200,"http"),
//                new HttpHost("21.145.229.353",9200,"http"));
//        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
//        credentialsProvider .setCredentials(AuthScope.ANY,new UsernamePasswordCredentials("elastic","password"));
//        builder.setHttpClientConfigCallback(f->f.setDefaultCredentialsProvider(credentialsProvider));
//        client = new RestHighLevelClient(builder);
    }

    @After
    public void destroy() throws IOException {
        client.close();
    }

    /**
     * 获取所有的Index
     *
     * @throws IOException
     */
    @Test
    public void getAllIndex() throws IOException {
        GetAliasesRequest request = new GetAliasesRequest();
        GetAliasesResponse getAliasesResponse = client.indices().getAlias(request, RequestOptions.DEFAULT);
        Map<String, Set<AliasMetaData>> aliases = getAliasesResponse.getAliases();
        Set<String> indices = aliases.keySet();
        for (String key : indices) {
            log.info(key);
        }
    }

    /**
     * 判断是否存在索引
     */
    @Test
    public void ifExist() throws IOException {
        GetIndexRequest indexRequest = new GetIndexRequest(INDEX_NAME);
        boolean exists = client.indices().exists(indexRequest, RequestOptions.DEFAULT);
        log.info("{}索引是否存在:{}", INDEX_NAME, exists);

        if (exists) {
            //<editor-fold desc="查询索引配置信息">
            // 查询分片
            GetSettingsRequest settingsRequest = new GetSettingsRequest();
            GetSettingsResponse getSettingsResponse = client.indices().getSettings(settingsRequest, RequestOptions.DEFAULT);
            Settings indexSettings = getSettingsResponse.getIndexToSettings().get(INDEX_NAME);
            Integer numberOfShards = indexSettings.getAsInt("index.number_of_shards", null);
            log.info("number_of_shards={}", numberOfShards);
            //</editor-fold>
        }
    }

    /**
     * 操作mapping
     */
    @Test
    public void mapping() throws IOException {
        //<editor-fold desc="查询mapping信息">
        GetMappingsResponse getMappingResponse = client.indices().getMapping(new GetMappingsRequest(), RequestOptions.DEFAULT);
        Map<String, MappingMetaData> allMappings = getMappingResponse.mappings();
        MappingMetaData indexMapping = allMappings.get(INDEX_NAME);
        Map<String, Object> mapping = indexMapping.sourceAsMap();
        log.info("mapping信息：{}", mapping);
        //</editor-fold>

        //<editor-fold desc="修改mapping">
        // 报错：Mapper for [text_max_word] conflicts with existing mapping:\n[mapper [text_max_word] has different [analyzer]]分词器指定后就不能修改了
        PutMappingRequest request = new PutMappingRequest(INDEX_NAME);
        Map map_properties = JSONUtil.toBean("{\n" +
                "    \"properties\": {\n" +
                "        \"text_max_word\": {\n" +
                "            \"type\": \"text\",\n" +
                "            \"analyzer\": \"ik_max_word\"\n" +
                "        }\n" +
                "    }\n" +
                "}", Map.class);
        request.source(map_properties);

        AcknowledgedResponse putMappingResponse = client.indices().putMapping(request, RequestOptions.DEFAULT);
        log.info("---------------" + JSONUtil.toJsonStr(putMappingResponse));
        //</editor-fold>
    }

    /**
     * 创建索引
     */
    @Test
    public void createIndex() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(INDEX_NAME);

        // 设置分片数量和副本数量
        request.settings(Settings.builder()
                .put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 1));

        // 字段映射
        Map map_properties = JSONUtil.toBean("{\n" +
                "    \"properties\": {\n" +
                "        \"message\": {\n" +
                "            \"type\": \"text\"\n" +
                "        }\n" +
                "    }\n" +
                "}", Map.class);
        request.mapping(map_properties);

        // 执行创建请求
        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
        // {"acknowledged":true,"shardsAcknowledged":true}
        log.info("---------------" + JSONUtil.toJsonStr(createIndexResponse));
    }

    /**
     * 删除索引
     *
     * @throws IOException
     */
    @Test
    public void deleteIndex() throws IOException, InterruptedException {
        DeleteIndexRequest request = new DeleteIndexRequest(INDEX_NAME);

        //<editor-fold desc="同步的方式请求">
//        AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
//        log.info("---------------" + JSONUtil.toJsonStr(response));
        //</editor-fold>


        //<editor-fold desc="异步的方式请求">
        ActionListener<AcknowledgedResponse> listener = new ActionListener<AcknowledgedResponse>() {
            @Override
            public void onResponse(AcknowledgedResponse deleteIndexResponse) {
                log.info("执行成功:{}", JSONUtil.toJsonStr(deleteIndexResponse));
            }
            @Override
            public void onFailure(Exception e) {
                log.error("执行失败", e);

            }
        };
        client.indices().deleteAsync(request, RequestOptions.DEFAULT, listener);
        // 等待异步任务执行完成
        Thread.sleep(1000);
        //</editor-fold>
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
    public void document() throws IOException {
        //<editor-fold desc="添加文档">
        Map<String, Object> map_doc = MapUtil.<String, Object>builder()
                // 这里指定id是无效的，如果要指定id，需要在IndexRequest对象中指定
                // 这里不能使用_id，否则报错：[_id] is a metadata field and cannot be added inside a document
                .put("float", RandomUtil.randomBigDecimal(new BigDecimal(10D), new BigDecimal(100D)))
                .put("int", RandomUtil.randomInt(10, 100))
                .put("date", DateUtil.today())
                .put("text_stand", "通过RestHighLevelClient添加数据")
                .put("text_smart", "通过RestHighLevelClient添加数据")
                .put("text_max_word", "通过RestHighLevelClient添加数据")
                .put("text_interests", new String[]{"滑雪", "橄榄球"}).build();

        IndexRequest indexRequest = new IndexRequest(INDEX_NAME, "_doc").source(map_doc);
        // 指定id，如果不指定，则由es生成
//        indexRequest.id("123456789");
        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        log.info("-------------添加文档：{}", JSONUtil.toJsonStr(indexResponse));
        //</editor-fold>


        //<editor-fold desc="查询文档">
        String id = indexResponse.getId();
        GetRequest getRequest = new GetRequest(INDEX_NAME).id(id);
        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
        log.info("-------------查询文档：{}", JSONUtil.toJsonStr(getResponse));

        // Disable fetching _source.
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        // Disable fetching stored fields.
        getRequest.storedFields("_none_");
        boolean exists = client.exists(getRequest, RequestOptions.DEFAULT);
        log.info("文档是否存在:{}", exists);
        //</editor-fold>


        //<editor-fold desc="更新文档">
        UpdateRequest updateRequest = new UpdateRequest(INDEX_NAME, "_doc", id)
                .doc(XContentFactory.jsonBuilder().startObject().field("int", 56).endObject());
        UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
        log.info("-------------更新文档：{}", JSONUtil.toJsonStr(updateResponse));

        // 更新的方式二：使用脚本
        updateRequest = new UpdateRequest(INDEX_NAME, "_doc", id);
        Script inline = new Script(ScriptType.INLINE, "painless", "ctx._source.text_stand += params.text_stand",
                Collections.singletonMap("text_stand", "test"));
        updateRequest.script(inline);
        updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
        log.info("-------------使用脚本更新文档：{}", JSONUtil.toJsonStr(updateResponse));
        //</editor-fold>


        //删除文档
        DeleteRequest deleteRequest = new DeleteRequest(INDEX_NAME).id(id);
        // type不指定的话会报错：type is missing
        deleteRequest.type("_doc");
        DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
        log.info("-----------------删除文档：{}", JSONUtil.toJsonStr(deleteResponse));
    }

    /**
     * 分析分词
     */
    @Test
    public void analyzeRequest() throws IOException {
        AnalyzeRequest request = new AnalyzeRequest().analyzer("ik_smart")
                .text("我是一个程序员", "I am cxy!")
                .explain(true);

        AnalyzeResponse response = client.indices().analyze(request, RequestOptions.DEFAULT);
        DetailAnalyzeResponse detail = response.detail();
        DetailAnalyzeResponse.AnalyzeTokenList analyzeTokenList = detail.analyzer();
        log.info("分词解析器使用的是：{}", analyzeTokenList.getName());

        AnalyzeResponse.AnalyzeToken[] arr_token = analyzeTokenList.getTokens();
        for (AnalyzeResponse.AnalyzeToken analyzeToken : arr_token) {
            log.info(JSONUtil.toJsonStr(analyzeToken));
        }
    }

    /**
     * 分页查询、高亮显示
     */
    @Test
    public void search() throws IOException {
        //<editor-fold desc="构造查询条件">
        QueryBuilder queryBuilder = null;
        // 查询全部
//        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        // term查询，不会对关键词进行分词再查询，这个查不出来的，因为text_smart默认是standard分词器，单个中文为一个token
//        queryBuilder = QueryBuilders.termQuery("text_smart", "数据");
//        queryBuilder = QueryBuilders.matchQuery("text_smart", "数据");
        // text_smart字段含有数据这个token 或者 int>=50（通过boost配置权重，优先排列）
        queryBuilder = QueryBuilders.boolQuery().should(QueryBuilders.matchQuery("text_smart", "数据")).should(QueryBuilders.rangeQuery("int").gte(56).boost(2F));
        //</editor-fold>

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(queryBuilder)
                .sort(new ScoreSortBuilder().order(SortOrder.DESC)) // 按分数降序
                .sort(new FieldSortBuilder("date").order(SortOrder.DESC))  // 按日期降序
                .fetchSource(new String[]{"text_*", "int"}, new String[]{"text_max_word"}) // 前面为includes，后面为excludes，结果是只返回除text_max_word之外的 以text_开头的列 和 int 列
                .highlighter(new HighlightBuilder().field("text_smart"))    // 指定 text_smart列 匹配的关键词高亮
                .from(0)
                .size(2);
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME).source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        SearchHits hits = searchResponse.getHits();
        log.info("总记录数：{}", hits.getTotalHits());
        for (SearchHit hit : hits) {
            log.info("记录详情：{}", hit.getSourceAsString());
            log.info("高亮：{}", hit.getHighlightFields());
        }
    }

    /**
     * 聚合查询
     */
    @Test
    public void aggregation() throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .aggregation(AggregationBuilders.stats("statsInt").field("int"))    // stats 查询所有聚合类型
                .aggregation(AggregationBuilders.max("maxFloat").field("float"));
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME).source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        Aggregations aggregations = searchResponse.getAggregations();
        log.info("聚合查询：{}", JSONUtil.toJsonStr(aggregations));
    }
}
