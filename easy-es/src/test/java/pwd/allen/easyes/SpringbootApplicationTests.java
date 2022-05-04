package pwd.allen.easyes;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.xpc.easyes.core.conditions.LambdaEsQueryWrapper;
import com.xpc.easyes.core.conditions.LambdaEsUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.geo.GeoPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pwd.allen.easyes.entity.EasyEsDoc;
import pwd.allen.easyes.mapper.EasyEsDocMapper;

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

}
