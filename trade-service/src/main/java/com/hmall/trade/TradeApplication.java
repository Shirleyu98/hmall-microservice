package com.hmall.trade;

import com.hmall.client.CartClient;
import com.hmall.client.ItemClient;
import com.hmall.client.PayClient;
import com.hmall.config.DefaultFeignConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("com.hmall.trade.mapper")
@EnableFeignClients(clients = {ItemClient.class, CartClient.class, PayClient.class}, defaultConfiguration = DefaultFeignConfig.class)
@SpringBootApplication
public class TradeApplication {
    public static void main(String[] args) {
        SpringApplication.run(TradeApplication.class, args);
    }
}
