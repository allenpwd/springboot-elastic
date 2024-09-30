package pwd.allen.elastic.repository;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.test.context.junit4.SpringRunner;
import pwd.allen.elastic.bean.Doc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 使用ElasticsearchRepository操作elasticsearch
 *
 * @author pwdan
 * @create 2024-09-30 16:00
 **/
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
class DocRepositoryTest {

    @Autowired
    DocRepository docRepository;


    /**
     * PUT /spring-boot-doc/_doc/123456789?timeout=1m
     * {
     *     "_class": "pwd.allen.elastic.bean.Doc",
     *     "myId": "123456789",
     *     "aInt": 20,
     *     "aFloat": 58.742203,
     *     "date": 1727684825590,
     *     "textStand": "测试下springboot data进行操作",
     *     "textSmart": "测试下springboot data进行操作",
     *     "geoPoint": {
     *         "lat": 21.842933145066883,
     *         "lon": 148.3635431315999
     *     }
     * }
     * 结果
     * {
     *     "_index": "spring-boot-doc",
     *     "_type": "_doc",
     *     "_id": "123456789",
     *     "_version": 15,
     *     "result": "updated",
     *     "_shards": {
     *         "total": 2,
     *         "successful": 1,
     *         "failed": 0
     *     },
     *     "_seq_no": 16,
     *     "_primary_term": 1
     * }
     */
    @Test
    void save() {
        //创建索引
        Doc doc = new Doc();
        doc.setMyId("123456789");	// 如果主键存在则更新，否则新增，如果没有设置主键，则es自动生成主键
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
    }

    /**
     * GET /spring-boot-doc/_search
     * {
     *     "from": 0,
     *     "size": 0,
     *     "query": {
     *         "bool": {
     *             "must": [
     *                 {
     *                     "query_string": {
     *                         "query": "测试test*",
     *                         "fields": [
     *                             "textSmart^1.0"
     *                         ],
     *                         "type": "best_fields",
     *                         "default_operator": "or",
     *                         "max_determinized_states": 10000,
     *                         "enable_position_increments": true,
     *                         "fuzziness": "AUTO",
     *                         "fuzzy_prefix_length": 0,
     *                         "fuzzy_max_expansions": 50,
     *                         "phrase_slop": 0,
     *                         "analyze_wildcard": true,
     *                         "escape": false,
     *                         "auto_generate_synonyms_phrase_query": true,
     *                         "fuzzy_transpositions": true,
     *                         "boost": 1
     *                     }
     *                 }
     *             ],
     *             "adjust_pure_negative": true,
     *             "boost": 1
     *         }
     *     },
     *     "version": true,
     *     "sort": [
     *         {
     *             "date": {
     *                 "order": "desc"
     *             }
     *         }
     *     ],
     *     "track_total_hits": 2147483647
     * }
     */
    @Test
    void findByTextSmartLikeOrderByDateDesc() {
        //检索
        for (Doc b : docRepository.findByTextSmartLikeOrderByDateDesc("测试test")) {
            log.info(b.toString());
        }
    }

    /**
     * {
     *     "from": 0,
     *     "size": 1,
     *     "query": {
     *         "bool": {
     *             "must": [
     *                 {
     *                     "range": {
     *                         "aInt": {
     *                             "from": 50,
     *                             "to": null,
     *                             "include_lower": true,
     *                             "include_upper": true,
     *                             "boost": 1
     *                         }
     *                     }
     *                 }
     *             ],
     *             "adjust_pure_negative": true,
     *             "boost": 1
     *         }
     *     },
     *     "version": true
     * }
     */
    @Test
    void findByAIntGreaterThanEqual() {
        log.info(docRepository.findByaIntGreaterThanEqual(50).toString());
    }
}
