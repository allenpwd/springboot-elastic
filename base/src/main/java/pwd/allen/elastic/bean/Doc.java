package pwd.allen.elastic.bean;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.util.Date;


/**
 * 实体类要指定id字段，否则会报错
 */
@Data
@Document(indexName = "spring-boot-doc", type = "springboot")  //报错：elasticsearch高版本一个index不支持多个type，所以这里indexName改成allen1
public class Doc {
//    @Id
    private String id;
    private Integer aInt;
    private float aFloat;
    private Date date;
    private String textStand;
    @Field(type = FieldType.Text, analyzer = "ik_smart", searchAnalyzer = "ik_smart")
    private String textSmart;
    /**
     * 如果没有这个注解标识，则插入es中的数据是含有lon和lat属性的对象，而不是geo_point类型
     */
    @GeoPointField
    private GeoPoint geoPoint;
}
