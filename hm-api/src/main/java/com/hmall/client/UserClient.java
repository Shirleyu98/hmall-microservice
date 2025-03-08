package com.hmall.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("user-service")
public interface UserClient {

//    void deductMoney(String pw, Integer totalFee);

    @PutMapping("/users/money/deduct")
    void deductMoney(@RequestParam("pw") String pw, @RequestParam("amount") Integer totalFee);
}
