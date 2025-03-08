package com.hmall.client;


import com.hmall.client.fallback.ItemClientFallback;
import com.hmall.config.DefaultFeignConfig;
import com.hmall.dto.ItemDTO;
import com.hmall.dto.OrderDetailDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

@FeignClient(value = "item-service",
        configuration = DefaultFeignConfig.class,
        fallbackFactory = ItemClientFallback.class
                )
public interface ItemClient {

    @GetMapping("/items")
    List<ItemDTO> queryItemByIds(@RequestParam("ids") Collection<Long> ids);

    @PutMapping("/items/stock/deduct")
    void deductStock(@RequestBody Collection<OrderDetailDTO> items);

    @ApiOperation("批量增加库存")
    @PutMapping("/items/stock/restore")
    void restoreStock(@RequestBody List<OrderDetailDTO> items);
}
