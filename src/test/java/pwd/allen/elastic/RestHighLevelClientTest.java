package pwd.allen.elastic;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.cluster.metadata.AliasMetaData;
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
        GetIndexRequest request = new GetIndexRequest(INDEX_NAME);
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        log.info("{}索引是否存在:{}", INDEX_NAME, exists);
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
     * @throws IOException
     */
    @Test
    public void deleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest(INDEX_NAME);

        // 执行创建请求
        AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
        log.info("---------------" + JSONUtil.toJsonStr(response));
    }
}
