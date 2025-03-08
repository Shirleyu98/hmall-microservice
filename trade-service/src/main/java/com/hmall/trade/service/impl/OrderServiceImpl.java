package com.hmall.trade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.hmall.client.CartClient;
import com.hmall.client.ItemClient;
import com.hmall.client.PayClient;
import com.hmall.common.utils.BeanUtils;
import com.hmall.dto.ItemDTO;
import com.hmall.dto.OrderDetailDTO;
import com.hmall.common.exception.BadRequestException;
import com.hmall.common.utils.UserContext;
import com.hmall.trade.constants.MqConstants;
import com.hmall.trade.domain.dto.OrderFormDTO;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.domain.po.OrderDetail;
import com.hmall.trade.mapper.OrderMapper;
import com.hmall.trade.service.IOrderDetailService;
import com.hmall.trade.service.IOrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2023-05-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    private final ItemClient itemClient;
    private final IOrderDetailService detailService;
    private final CartClient cartClient;
    private final RabbitTemplate rabbitTemplate;
    private final PayClient payClient;

    @Override
    @GlobalTransactional
    public Long createOrder(OrderFormDTO orderFormDTO) {
        // 1.订单数据
        Order order = new Order();
        // 1.1.查询商品
        List<OrderDetailDTO> detailDTOS = orderFormDTO.getDetails();
        // 1.2.获取商品id和数量的Map
        Map<Long, Integer> itemNumMap = detailDTOS.stream()
                .collect(Collectors.toMap(OrderDetailDTO::getItemId, OrderDetailDTO::getNum));
        Set<Long> itemIds = itemNumMap.keySet();
        // 1.3.查询商品
        List<ItemDTO> items = itemClient.queryItemByIds(itemIds);
        if (items == null || items.size() < itemIds.size()) {
            throw new BadRequestException("商品不存在");
        }
        // 1.4.基于商品价格、购买数量计算商品总价：totalFee
        int total = 0;
        for (ItemDTO item : items) {
            total += item.getPrice() * itemNumMap.get(item.getId());
        }
        order.setTotalFee(total);
        // 1.5.其它属性
        order.setPaymentType(orderFormDTO.getPaymentType());
        order.setUserId(UserContext.getUser());
        order.setStatus(1);
        // 1.6.将Order写入数据库order表中
        save(order);

        // 2.保存订单详情
        List<OrderDetail> details = buildDetails(order.getId(), items, itemNumMap);
        detailService.saveBatch(details);

        // 3.清理购物车商品
        cartClient.deleteCartItemByIds(itemIds);

        // 4.扣减库存
        try {
            itemClient.deductStock(detailDTOS);
        } catch (Exception e) {
            throw new RuntimeException("库存不足！");
        }

        // 5.发送延迟消息，检测订单支付状态
        rabbitTemplate.convertAndSend(
                MqConstants.DELAY_EXCHANGE_NAME,
                MqConstants.DELAY_ORDER_KEY,
                order.getId(),
                message -> {
                    message.getMessageProperties().setDelay(30000);
                    return message;
                });

        return order.getId();
    }

//    @Override
//    @GlobalTransactional
//    public Long createOrder(OrderFormDTO orderFormDTO) {
//
//        // 1.订单数据
//        Order order = new Order();
//        // 1.1.查询商品
//        List<OrderDetailDTO> detailDTOS = orderFormDTO.getDetails();
//        // 1.2.获取商品id和数量的Map
//        Map<Long, Integer> itemNumMap = detailDTOS.stream()
//                .collect(Collectors.toMap(OrderDetailDTO::getItemId, OrderDetailDTO::getNum));
//        Set<Long> itemIds = itemNumMap.keySet();
//        // 1.3.查询商品
//        List<ItemDTO> items = itemClient.queryItemByIds(itemIds);
//        if (items == null || items.size() < itemIds.size()) {
//            throw new BadRequestException("商品不存在");
//        }
//        // 1.4.基于商品价格、购买数量计算商品总价：totalFee
//        int total = 0;
//        for (ItemDTO item : items) {
//            total += item.getPrice() * itemNumMap.get(item.getId());
//        }
//        order.setTotalFee(total);
//        // 1.5.其它属性
//        order.setPaymentType(orderFormDTO.getPaymentType());
//        order.setUserId(UserContext.getUser());
//        order.setStatus(1);
//        // 1.6.将Order写入数据库order表中
//        save(order);
//
//        // 2.保存订单详情
//        List<OrderDetail> details = buildDetails(order.getId(), items, itemNumMap);
//        detailService.saveBatch(details);
//
//        // 3.清理购物车商品
////        cartClient.deleteCartItemByIds(itemIds);
//
//        try {
//            rabbitTemplate.convertAndSend("trade.topic", "order.create", itemIds, new MessagePostProcessor() {
//                @Override
//                public Message postProcessMessage(Message message) throws AmqpException {
//                    Long userId = UserContext.getUser();
//                    log.info("清理购物车商品信息已准备发送，用户id：{}", userId);
//                    message.getMessageProperties().setHeader("userId", userId);
//                    return message;
//                }
//            });
//        } catch (Exception e) {
//            log.error("清空购物车失败，订单号：{}", order.getId(), e);
//        }
//
//        rabbitTemplate.convertAndSend(
//                MqConstants.DELAY_EXCHANGE_NAME,
//                MqConstants.DELAY_ORDER_KEY,
//                order.getId(),
//                message -> {
//                    message.getMessageProperties().setDelay(10000);
//                    return message;
//                }
//        );
//
//        // 4.扣减库存
//        try {
//           itemClient.deductStock(detailDTOS);
//        } catch (Exception e) {
//            throw new RuntimeException("库存不足！");
//        }
//        return order.getId();
//    }

    @Override
    public void markOrderPaySuccess(Long orderId) {
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(2);
        order.setPayTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    public void cancelOrder(Long orderId) {
//        1.修改订单状态为5
        lambdaUpdate()
                .set(Order::getStatus, 5)
                .eq(Order::getId, orderId)
                .update();
//        2.修改交易单状态为2
        payClient.updateOrderStatusByBizOrderNo(orderId, 2);
//        3.call：tradeService 返回订单商品列表及数量
        List<OrderDetail> items = detailService.lambdaQuery().eq(OrderDetail::getOrderId, orderId).list();
//        4.call: ItemService 增加商品库存
        List<OrderDetailDTO> orderDetailDTOS = BeanUtils.copyToList(items, OrderDetailDTO.class);
        itemClient.restoreStock(orderDetailDTOS);

    }

    private List<OrderDetail> buildDetails(Long orderId, List<ItemDTO> items, Map<Long, Integer> numMap) {
        List<OrderDetail> details = new ArrayList<>(items.size());
        for (ItemDTO item : items) {
            OrderDetail detail = new OrderDetail();
            detail.setName(item.getName());
            detail.setSpec(item.getSpec());
            detail.setPrice(item.getPrice());
            detail.setNum(numMap.get(item.getId()));
            detail.setItemId(item.getId());
            detail.setImage(item.getImage());
            detail.setOrderId(orderId);
            details.add(detail);
        }
        return details;
    }
}
