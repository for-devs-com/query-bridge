package fordevs.dynamicqueryengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class DynamicQueryEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(DynamicQueryEngineApplication.class, args);
    }

}
