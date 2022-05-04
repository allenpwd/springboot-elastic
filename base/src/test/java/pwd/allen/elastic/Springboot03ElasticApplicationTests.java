package pwd.allen.elastic;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.test.context.junit4.SpringRunner;
import pwd.allen.elastic.bean.Article;
import pwd.allen.elastic.bean.Doc;
import pwd.allen.elastic.repository.DocRepository;

import java.io.IOException;
import java.math.BigDecimal;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class Springboot03ElasticApplicationTests {

	/**
	 * 这个高版本弃用了
	 * @deprecated
	 */
	@Autowired(required = false)
	JestClient jestClient;

	@Autowired
	DocRepository docRepository;

	/**
	 * spring-boot-starter-data-elasticsearch:2.3.2.RELEASE用的是这个，而没有JestClient
	 */
	@Autowired
	RestHighLevelClient client;


    /**
     * 使用ElasticsearchRepository操作elasticsearch
     */
	@Test
	public void testSpringDataES(){

	    //创建索引
		Doc doc = new Doc();
		doc.setAInt(RandomUtil.randomInt(100));
		doc.setAFloat(RandomUtil.randomBigDecimal(new BigDecimal(100)).floatValue());
		doc.setDate(DateUtil.date());
		doc.setTextStand("测试下springboot data进行操作");
		doc.setTextSmart("测试下springboot data进行操作");
		doc.setGeoPoint(new GeoPoint(
				RandomUtil.randomBigDecimal(new BigDecimal(20), new BigDecimal(30)).doubleValue()
				, RandomUtil.randomBigDecimal(new BigDecimal(110), new BigDecimal(150)).doubleValue()
		));
		Doc rel = docRepository.save(doc);
		log.info("保存结果：{}", rel);

		//检索
		for (Doc b : docRepository.findByTextSmartLike("测试")) {
			log.info(b.toString());
		}
	}


	/**
	 * 使用JestClient创建索引
	 */
	@Test
	public void contextLoads() {
		//1、给Es中索引（保存）一个文档；
		Article article = new Article();
		article.setId(1);
		article.setTitle("好消息");
		article.setAuthor("zhangsan");
		article.setContent("Hello World");

		//构建一个索引功能
		Index index = new Index.Builder(article).index("allen").type("news").build();

		try {
			//执行
			jestClient.execute(index);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 使用JestClient检索
	 */
	@Test
	public void search(){

		//查询表达式
		String json ="{\n" +
				"    \"query\" : {\n" +
				"        \"match\" : {\n" +
				"            \"content\" : \"hello\"\n" +
				"        }\n" +
				"    }\n" +
				"}";

		//更多操作：https://github.com/searchbox-io/Jest/tree/master/jest
		//构建搜索功能
		Search search = new Search.Builder(json).addIndex("allen").addType("news").build();

		//执行
		try {
			SearchResult result = jestClient.execute(search);
			System.out.println(result.getJsonString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
