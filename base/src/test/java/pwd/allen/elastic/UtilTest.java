package pwd.allen.elastic;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.junit.Test;

import java.io.IOException;

public class UtilTest {

    @Test
    public void json() throws IOException {
        XContentBuilder xContentBuilder = XContentFactory.jsonBuilder()
                .startObject()
                .startObject("message").field("type", "text").endObject()
                .endObject();

    }
}
