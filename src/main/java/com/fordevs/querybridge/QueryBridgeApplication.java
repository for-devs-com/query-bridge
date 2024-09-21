package com.fordevs.querybridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class QueryBridgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(QueryBridgeApplication.class, args);
    }

}
