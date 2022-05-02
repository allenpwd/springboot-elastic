package pwd.allen.elastic;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.*;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.settings.Settings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author 门那粒沙
 * @create 2022-05-01 21:17
 **/
@Slf4j
public class RestHighLevelClientTest {

    private RestHighLevelClient client;

    private static final String INDEX_NAME = "rest_high_level_client";

    @Before
    public void init() throws IOException {
        Properties pps = new Properties();
        pps.load(RestHighLevelClientTest.class.getClassLoader().getResourceAsStream("application.properties"));
        String host = pps.getProperty("spring.elasticsearch.jest.uris");
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
    public void deleteIndex() throws IOException {
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
        //</editor-fold>
    }
}
