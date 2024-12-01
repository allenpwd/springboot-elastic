package pwd.allen.easyes;

import cn.easyes.core.conditions.LambdaEsQueryWrapper;
import cn.easyes.core.conditions.LambdaEsUpdateWrapper;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.search.aggregations.metrics.ParsedMax;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pwd.allen.easyes.entity.EasyEsDoc;
import pwd.allen.easyes.mapper.EasyEsDocMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringbootApplicationTests {

	@Autowired
	EasyEsDocMapper easyEsDocMapper;

    /**
     * 使用ElasticsearchRepository操作elasticsearch
     */
	@Test
	public void add(){
		EasyEsDoc easyEsDoc = new EasyEsDoc();
		easyEsDoc.setId("test");
		easyEsDoc.setAInt(RandomUtil.randomInt(100));
		easyEsDoc.setAFloat(RandomUtil.randomBigDecimal(new BigDecimal(100)).floatValue());
		easyEsDoc.setDate(DateUtil.date());
		easyEsDoc.setTextStand("测试下easy es这个工具");
		easyEsDoc.setTextMaxWord("测试下easy es这个工具");
		easyEsDoc.setTextSmart("测试下easy es这个工具");
		easyEsDoc.setGeoPoint(String.format("%f,%f",
				RandomUtil.randomBigDecimal(new BigDecimal(20), new BigDecimal(30)).doubleValue()
				, RandomUtil.randomBigDecimal(new BigDecimal(110), new BigDecimal(150)).doubleValue()
		));

		Integer insert = easyEsDocMapper.insert(easyEsDoc);
		log.info("insert结果：{}", insert);
	}

	@Test
	public void update() {
		// {"query":{"term":{"_id":{"value":"test","boost":1.0}}}}
		EasyEsDoc doc = easyEsDocMapper.selectById("test");
		log.info("查询结果：{}", doc);

		if (doc != null) {
			// 根据id更新
			doc.setAInt(doc.getAInt() + 100);
			Integer rel = easyEsDocMapper.updateById(doc);
			log.info("更新结果：{}", rel);
		}

		// 根据条件更新
		LambdaEsUpdateWrapper<EasyEsDoc> wrapper = new LambdaEsUpdateWrapper<>();
		wrapper.eq(EasyEsDoc::getAInt, 18);
		EasyEsDoc doc4update = new EasyEsDoc();
		doc4update.setAInt(24);
		Integer update = easyEsDocMapper.update(doc4update, wrapper);
		log.info("更新结果：{}", update);
	}

	/**
	 * {
	 *     "wildcard": {
	 *         "textStand": {
	 *             "wildcard": "*easy es*",
	 *             "boost": 1
	 *         }
	 *     }
	 * }
	 */
	@Test
	public void like() {
		LambdaEsQueryWrapper<EasyEsDoc> wrapper = new LambdaEsQueryWrapper<>();
		wrapper.like(EasyEsDoc::getTextStand, "easy es");
		// 输出最终的DSL语句
		log.info("DSL语句：{}", easyEsDocMapper.getSource(wrapper));

		List<EasyEsDoc> list = easyEsDocMapper.selectList(wrapper);
		log.info("查询结果：{}", list);

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 查询圆内的地点
	 * {
	 *   "size": 10000,
	 *   "query": {
	 *     "bool": {
	 *       "filter": [
	 *         {
	 *           "geo_distance": {
	 *             "geoPoint": [
	 *               115,
	 *               23
	 *             ],
	 *             "distance": 100000,
	 *             "distance_type": "arc",
	 *             "validation_method": "STRICT",
	 *             "ignore_unmapped": false,
	 *             "boost": 1
	 *           }
	 *         }
	 *       ],
	 *       "adjust_pure_negative": true,
	 *       "boost": 1
	 *     }
	 *   }
	 * }
	 */
	@Test
	public void geoDistance() {
		// 查询以经度为23.0,纬度为115.0为圆心,半径100公里内的所有点
		LambdaEsQueryWrapper<EasyEsDoc> wrapper = new LambdaEsQueryWrapper<>();
		// 其中单位可以省略,默认为km
		wrapper.geoDistance(EasyEsDoc::getGeoPoint, 100.0, DistanceUnit.KILOMETERS, new GeoPoint(23.0, 115.0));
		// 上面语法也可以写成下面这几种形式,效果是一样的,兼容不同用户习惯而已:
//        wrapper.geoDistance(Document::getLocation,"100km", new GeoPoint(23.0, 115.0));
//        wrapper.geoDistance(Document::getLocation, "100km", "23.0,115.0");

		//查询不在圆形内的所有点
		// wrapper.notInGeoDistance(Document::getLocation, 100.0, DistanceUnit.KILOMETERS, new GeoPoint(23.0, 115.0));

		List<EasyEsDoc> list = easyEsDocMapper.selectList(wrapper);
		log.info("查询结果：{}", list);
	}

	@Test
	public void max() throws IOException {
		LambdaEsQueryWrapper<EasyEsDoc> wrapper = new LambdaEsQueryWrapper<>();
//		wrapper.likeRight(EasyEsDoc::getTextMaxWord,"推");
		wrapper.max(EasyEsDoc::getAInt);
		wrapper.size(0);
		SearchResponse response = easyEsDocMapper.search(wrapper);
		ParsedMax aInt = (ParsedMax) response.getAggregations().get("aInt");
		System.out.println("" + Double.valueOf(aInt.getValue()).intValue());
	}

}
