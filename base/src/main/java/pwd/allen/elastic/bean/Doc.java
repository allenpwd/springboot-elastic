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
 * elasticsearch高版本一个index不支持多个type，将在8以上版本删除type
 * 索引名只能小写，具体看注解源码注释
 */
@Data
@Document(indexName = "spring-boot-doc", type = "已弃用")  // type已废弃，不需要指定
public class Doc {
    /**
     * 实体类要指定id（如果字段名不是id，需要加@Id指定），否则会报错：No id property found for entity class pwd.allen.elastic.bean.Doc
     */
    @Id
    private String myId;
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
