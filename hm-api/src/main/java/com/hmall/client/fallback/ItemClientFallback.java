package com.hmall.client.fallback;

import com.hmall.client.ItemClient;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.utils.CollUtils;
import com.hmall.dto.ItemDTO;
import com.hmall.dto.OrderDetailDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collection;
import java.util.List;


@Slf4j
public class ItemClientFallback implements FallbackFactory<ItemClient> {
    @Override
    public ItemClient create(Throwable cause) {
        return new ItemClient() {
            @Override
            public ItemDTO queryItemById(Long id) {
                log.error("根据Id查询商品失败，商品号{}", id);
                return null;
            }

            @Override
            public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
                log.error("远程调用ItemClient#queryItemByIds方法出现异常，参数：{}", ids, cause);
                return CollUtils.emptyList();
            }

            @Override
            public void deductStock(Collection<OrderDetailDTO> items) {
                //扣除失败需要触发事务回滚，查询失败，抛出异常
                throw new BizIllegalException(cause);
            }

            @Override
            public void restoreStock(List<OrderDetailDTO> items) {
                log.error("远程调用ItemClient#restoreStock方法出现异常，参数：{}", items, cause);
            }
        };
    }
}
