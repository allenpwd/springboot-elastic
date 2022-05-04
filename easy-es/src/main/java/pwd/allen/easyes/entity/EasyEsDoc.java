package pwd.allen.easyes.entity;

import com.xpc.easyes.core.anno.TableField;
import com.xpc.easyes.core.anno.TableId;
import com.xpc.easyes.core.anno.TableName;
import com.xpc.easyes.core.enums.Analyzer;
import com.xpc.easyes.core.enums.FieldType;
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
     * es中的唯一id
     */
    @TableId
    private String id;
    private String textStand;
    private Integer aInt;
    private Float aFloat;
    private Date date;
    /**
     * 经纬度 lon经度 lat纬度
     */
    @TableField(fieldType = FieldType.GEO_POINT)
    private GeoPoint geoPoint;
    @TableField(fieldType= FieldType.TEXT,analyzer= Analyzer.IK_SMART,searchAnalyzer=Analyzer.IK_SMART)
    private String textSmart;
    @TableField(fieldType= FieldType.TEXT,analyzer= Analyzer.IK_MAX_WORD,searchAnalyzer=Analyzer.IK_MAX_WORD)
    private String textMaxWord;
}
