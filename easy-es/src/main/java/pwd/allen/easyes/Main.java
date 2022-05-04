package pwd.allen.easyes;

import com.xpc.easyes.autoconfig.annotation.EsMapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author 门那粒沙
 * @create 2022-05-04 13:22
 **/
@SpringBootApplication
@EsMapperScan("pwd.allen.easyes.mapper")
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
