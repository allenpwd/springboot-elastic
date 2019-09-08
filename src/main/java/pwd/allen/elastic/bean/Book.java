package pwd.allen.elastic.bean;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Document(indexName = "allen1", type = "book")  //报错：elasticsearch高版本一个index不支持多个type，所以这里indexName改成allen1
public class Book {
    private Integer id;
    private String bookName;
    private String author;

}
