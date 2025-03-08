package com.hmall.cart.listener;

import com.hmall.cart.service.ICartService;
import com.hmall.common.utils.UserContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@AllArgsConstructor
public class deleteCartItemsListener {

    private final ICartService cartService;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "cart.clear.queue", durable = "true"),
            exchange = @Exchange(name = "trade.topic"),
            key = "order.create"
    ))
    public void listenOrderPlaced(@Payload Set<Long> itemIds, Message message){
        Long userId = message.getMessageProperties().getHeader("userId");
        log.info("订单已创建，正在清理购物车");
        UserContext.setUser(userId);
        cartService.removeByItemIds(itemIds);
    }
}
