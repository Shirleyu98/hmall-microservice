package com.hmall.search.utils;

import cn.hutool.json.JSONUtil;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.utils.BeanUtils;
import com.hmall.search.domain.dto.ItemDTO;
import com.hmall.search.domain.po.ItemDoc;
import com.hmall.search.domain.query.ItemPageQuery;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ElasticsearchResponseMapper {

    public static PageDTO<ItemDTO> mapToPageDTO(SearchResponse response, ItemPageQuery query){
        List<ItemDoc> itemDocs = Arrays.stream(response.getHits().getHits())
                .map(ElasticsearchResponseMapper::mapHitToItemDoc)
                .collect(Collectors.toList());

        List<ItemDTO> items = BeanUtils.copyToList(itemDocs, ItemDTO.class);
        long total = response.getHits().getTotalHits().value;

        return new PageDTO<>(total, query.getPageSize().longValue(), items);
    }

    private static ItemDoc mapHitToItemDoc(SearchHit hit){
        Map<String, Object> sourceMap = hit.getSourceAsMap();
        String json = JSONUtil.toJsonStr(sourceMap);
        return JSONUtil.toBean(json, ItemDoc.class);


    }


}
