package com.hmall.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient("trade-service")
public interface TradeClient {

//        @PutMapping("/{orderId}")
//    public void markOrderPaySuccess(@PathVariable("orderId") Long orderId) {
//        orderService.markOrderPaySuccess(orderId);
//    }

    @PutMapping("/orders/{orderId}")
    void updateById(@PathVariable("orderId") Long orderId);
}
