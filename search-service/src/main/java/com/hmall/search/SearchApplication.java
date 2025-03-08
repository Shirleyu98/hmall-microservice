package com.hmall.search;

import com.hmall.client.ItemClient;
import com.hmall.config.DefaultFeignConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("com.hmall.search.mapper")
@EnableFeignClients(clients = {ItemClient.class}, defaultConfiguration = DefaultFeignConfig.class)
@SpringBootApplication
public class SearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(SearchApplication.class, args);
    }

}
