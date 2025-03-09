package com.hmall.search.Listener;

import cn.hutool.core.bean.BeanUtil;
import com.hmall.client.ItemClient;
import com.hmall.dto.ItemDTO;
import com.hmall.search.domain.po.ItemDoc;
import com.hmall.search.repository.ItemDocRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class ItemListener {

    @Autowired
    private ItemDocRepository itemDocRepository;

    @Autowired
    private ItemClient itemClient;


//
//    @Autowired
//    private ElasticsearchRestTemplate elasticsearchRestTemplate;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "item.save.queue", durable = "true"),
            exchange = @Exchange(value = "search-exchange"),
            key = {"item.save"}
    ))
    public void save(Long id){
        ItemDTO itemDTO = itemClient.queryItemById(id);
        ItemDoc itemDoc = BeanUtil.copyProperties(itemDTO, ItemDoc.class);
        log.info("ElasticSearch尝试新增商品: {}", itemDoc);
        itemDoc.setId(String.valueOf(itemDTO.getId()));
        itemDoc.setUpdateTime(LocalDateTime.now()); // 在保存前设置时间
        log.info("ElasticSearch尝试新增商品: {}", itemDoc);
        itemDocRepository.save(itemDoc);
        log.info("ElasticSearch新增商品: {}", itemDTO);
    }
}
