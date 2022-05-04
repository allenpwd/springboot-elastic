package pwd.allen.easyes;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
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
		easyEsDoc.setAInt(RandomUtil.randomInt(100));
		easyEsDoc.setAFloat(RandomUtil.randomBigDecimal(new BigDecimal(100)).floatValue());
		easyEsDoc.setDate(DateUtil.date());
		easyEsDoc.setTextStand("测试下easy es这个工具");
		easyEsDoc.setTextMaxWord("测试下easy es这个工具");
		easyEsDoc.setTextSmart("测试下easy es这个工具");
		easyEsDoc.setGeoPoint(new GeoPoint(
				RandomUtil.randomBigDecimal(new BigDecimal(20), new BigDecimal(30)).doubleValue()
				, RandomUtil.randomBigDecimal(new BigDecimal(110), new BigDecimal(150)).doubleValue()
		));

		Integer insert = easyEsDocMapper.insert(easyEsDoc);
		log.info("insert结果：{}", insert);
	}

}
