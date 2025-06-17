package com.hmall.search.controller;

import com.hmall.common.domain.PageDTO;
import com.hmall.search.domain.query.ItemPageQuery;
import com.hmall.search.domain.dto.ItemDTO;
import com.hmall.search.service.ElasticsearchService;
import com.hmall.search.utils.ElasticsearchResponseMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import org.elasticsearch.action.search.SearchResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Api(tags = "搜索相关接口")
@RestController
@Slf4j
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final ElasticsearchService searchService;

    @ApiOperation("搜索商品")
    @GetMapping("/list")
    public PageDTO<ItemDTO> search(ItemPageQuery query) {
//        // 分页查询
//        Page<Item> result = searchService.lambdaQuery()
//                .like(StrUtil.isNotBlank(query.getKey()), Item::getName, query.getKey())
//                .eq(StrUtil.isNotBlank(query.getBrand()), Item::getBrand, query.getBrand())
//                .eq(StrUtil.isNotBlank(query.getCategory()), Item::getCategory, query.getCategory())
//                .eq(Item::getStatus, 1)
//                .between(query.getMaxPrice() != null, Item::getPrice, query.getMinPrice(), query.getMaxPrice())
//                .page(query.toMpPage("update_time", false));
//        // 封装并返回
//        return PageDTO.of(result, ItemDTO.class);

         try{
             SearchResponse searchResponse = searchService.searchItems(query);
             return ElasticsearchResponseMapper.mapToPageDTO(searchResponse, query);
         }catch (IOException e){
            log.error("Error searching items", e);
            return new PageDTO<>(0L, query.getPageSize().longValue(), List.of());
         }
    }

    @ApiOperation("根据过滤条件查询")
    @PostMapping("/filters")
    public Map<String, List<String>> getFilters(@RequestBody ItemPageQuery query) throws IOException {

        return searchService.getFilters(query);

    }
}
