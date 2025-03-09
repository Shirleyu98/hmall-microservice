package com.hmall.search.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.common.domain.PageDTO;
import com.hmall.search.domain.dto.ItemDTO;
import com.hmall.search.domain.po.Item;
import com.hmall.search.domain.query.ItemPageQuery;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ElasticsearchService{
    private static final String INDEX_NAME = "items";
    private final RestHighLevelClient client;

    public SearchResponse searchItems(ItemPageQuery query) throws IOException{
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);

        BoolQueryBuilder boolQueryBuilder = boolQueryBuilder(query);

        int from = (query.getPageNo() - 1) * query.getPageSize();

        searchRequest.source().from(from).size(query.getPageSize());

        configureSorting(searchRequest,query);

        FunctionScoreQueryBuilder functionSocreQuery = QueryBuilders.functionScoreQuery(
                boolQueryBuilder,
                new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                QueryBuilders.termQuery("isAD", true),
                                ScoreFunctionBuilders.weightFactorFunction(100)
                        )
                }
        ).boostMode(CombineFunction.MULTIPLY);

        searchRequest.source().query(functionSocreQuery);

        return client.search(searchRequest, RequestOptions.DEFAULT);

    }

    private BoolQueryBuilder boolQueryBuilder(ItemPageQuery query){
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        if(query.getKey() != null && !query.getKey().isEmpty()){
            boolQuery.must(QueryBuilders.matchQuery("name", query.getKey()));
        }

        if(query.getBrand() != null && !query.getBrand().isEmpty()){
            boolQuery.filter(QueryBuilders.termQuery("brand", query.getBrand()));
        }

        if(query.getCategory() != null && !query.getCategory().isEmpty()){
            boolQuery.filter(QueryBuilders.termQuery("brand", query.getCategory()));
        }

        if(query.getMaxPrice() != null){
            boolQuery.filter(QueryBuilders.rangeQuery("price").lte(query.getMaxPrice()));
        }

        if(query.getMinPrice() != null){
            boolQuery.filter(QueryBuilders.rangeQuery("price").gte(query.getMinPrice()));
        }

        return boolQuery;

    }

    private void configureSorting(SearchRequest request, ItemPageQuery query){
        SortOrder sortOrder = query.getIsAsc()? SortOrder.ASC: SortOrder.DESC;

        if(query.getSortBy() != null && !query.getSortBy().isEmpty()){
            request.source().sort(query.getSortBy(), sortOrder);
        }else {
            request.source().sort("updateTime", sortOrder);
        }
    }
}
