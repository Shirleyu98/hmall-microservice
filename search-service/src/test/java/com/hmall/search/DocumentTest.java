package com.hmall.search;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.hmall.client.ItemClient;
import com.hmall.dto.ItemDTO;
import com.hmall.search.domain.po.Item;
import com.hmall.search.domain.po.ItemDoc;
import com.hmall.search.repository.ItemDocRepository;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@SpringBootTest(properties = "spring.profiles.active=local")
public class DocumentTest {

    @Autowired
    private ItemDocRepository itemDocRepository;

    @Autowired
    private ItemClient itemClient;

    @Test
    void testAddDocument() throws IOException{
//        1.根据id查询商品数据
//        Item item = itemService.getById(100002644680L);
        ItemDTO itemDTO = itemClient.queryItemById(100002672309L);
//        2。转换为文档类型
        ItemDoc itemDoc = BeanUtil.copyProperties(itemDTO, ItemDoc.class);

        itemDoc.setUpdateTime(LocalDateTime.now());

        itemDocRepository.save(itemDoc);
    }

    @Test
    void testGetDocument() throws IOException{
        itemDocRepository.findById("317580");
    }
}
