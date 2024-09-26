package pwd.allen.easyes.entity;

import com.xpc.easyes.core.anno.TableField;
import com.xpc.easyes.core.anno.TableId;
import com.xpc.easyes.core.anno.TableName;
import com.xpc.easyes.core.enums.Analyzer;
import com.xpc.easyes.core.enums.FieldType;
import com.xpc.easyes.core.enums.IdType;
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
@TableName(value = "easy_es_doc")
@Data
public class EasyEsDoc {

    /**
     * 默认id为es自动生成的id
     * CUSTOMIZE：需要自己指定id，否则报错：the entity id must not be null；如果用户指定的id在es中已存在记录,则自动更新该id对应的记录.
     */
    @TableId(type = IdType.CUSTOMIZE)
    private String id;
    @TableField(fieldType = FieldType.TEXT)
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
    @TableField(fieldType = FieldType.GEO_POINT)
    private String geoPoint;
    @TableField(fieldType= FieldType.TEXT,analyzer= Analyzer.IK_SMART,searchAnalyzer=Analyzer.IK_SMART)
    private String textSmart;
    @TableField(fieldType= FieldType.TEXT,analyzer= Analyzer.IK_MAX_WORD,searchAnalyzer=Analyzer.IK_MAX_WORD)
    private String textMaxWord;
}
