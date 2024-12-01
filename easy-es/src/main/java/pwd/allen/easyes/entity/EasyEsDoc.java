package pwd.allen.easyes.entity;

import cn.easyes.annotation.IndexField;
import cn.easyes.annotation.IndexId;
import cn.easyes.annotation.IndexName;
import cn.easyes.annotation.rely.Analyzer;
import cn.easyes.annotation.rely.FieldType;
import cn.easyes.annotation.rely.IdType;
import lombok.Data;
import org.elasticsearch.common.geo.GeoPoint;

import java.util.Date;

/**
 * 启动项目,由Easy-Es自动帮您创建索引
 *
 * 如果没法通过@TableField指定分词器，则可能是easyes的版本过低
 *
 * @author 门那粒沙
 * @create 2022-05-04 13:25
 **/
@IndexName(value = "easy_es_doc")
@Data
public class EasyEsDoc {

    /**
     * 默认id为es自动生成的id
     * CUSTOMIZE：需要自己指定id，否则报错：the entity id must not be null；如果用户指定的id在es中已存在记录,则自动更新该id对应的记录.
     */
    @IndexId(type = IdType.CUSTOMIZE)
    private String id;
    @IndexField(fieldType = FieldType.TEXT)
    private String textStand;
    private Integer aInt;
    private Float aFloat;
    private Date date;
    /**
     * 经纬度 格式：lat,lon
     * 字段类型推荐使用String,因为wkt文本格式就是String；这里如果用GeoPoint类型会报json解析相关的错误
     *
     * (GeoBoundingBox,GeoDistance,GeoPolygon)字段索引类型必须为geo_point
     * GeoShape字段索引类型必须为geo_shape
     */
    @IndexField(fieldType = FieldType.GEO_POINT)
    private String geoPoint;
    @IndexField(fieldType= FieldType.TEXT,analyzer= Analyzer.IK_SMART,searchAnalyzer=Analyzer.IK_SMART)
    private String textSmart;
    @IndexField(fieldType= FieldType.TEXT,analyzer= Analyzer.IK_MAX_WORD,searchAnalyzer=Analyzer.IK_MAX_WORD)
    private String textMaxWord;
}
