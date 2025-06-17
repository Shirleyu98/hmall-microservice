package com.hmall.search.service;

import co.elastic.clients.elasticsearch._types.aggregations.AggregateBuilders;
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
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;
import org.elasticsearch.search.aggregations.AggregationBuilders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchService{
    private static final String INDEX_NAME = "items";
    private final RestHighLevelClient client;

    public SearchResponse searchItems(ItemPageQuery query) throws IOException{
//        log.info("querySearch"+query.toString());
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);

        BoolQueryBuilder boolQueryBuilder = boolQueryBuilder(query);

        int from = (query.getPageNo() - 1) * query.getPageSize();

        searchRequest.source().from(from).size(query.getPageSize());

        configureSorting(searchRequest,query);

        FunctionScoreQueryBuilder functionScoreQuery = QueryBuilders.functionScoreQuery(
                boolQueryBuilder,
                new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                QueryBuilders.termQuery("isAD", true),
                                ScoreFunctionBuilders.weightFactorFunction(100)
                        )
                }
        ).boostMode(CombineFunction.MULTIPLY);

        searchRequest.source().query(functionScoreQuery);

//        log.info("search Request Complete"+searchRequest.source().toString());

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
            boolQuery.filter(QueryBuilders.termQuery("category", query.getCategory()));
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
        }

        //TODO: 如果即使选中了价格&销量，也要让广告商品排前呢？
//        else {
//            request.source().sort("updateTime", sortOrder);
//        }


    }

    public Map<String, List<String>> getFilters(ItemPageQuery query) throws IOException{
        Map<String, List<String>> results = new HashMap<>();

//        log.info("Filter query"+query.toString());

//      build the query with filters
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        BoolQueryBuilder boolQueryBuilder = boolQueryBuilder(query);

//        confine results to certain conditions
        int from = (query.getPageNo()-1) * query.getPageSize();
        searchRequest.source().from(from).size(query.getPageSize());
        searchRequest.source().query(boolQueryBuilder).size(0);

        //add aggregations
        searchRequest.source().aggregation(
                AggregationBuilders.terms("brand_agg").field("brand").size(10)
        );
        searchRequest.source().aggregation(
                AggregationBuilders.terms("category_agg").field("category").size(10)
        );
//        log.info(boolQueryBuilder.toString());
//        log.info("Complete Filter Request: " + searchRequest.source().toString());
        //send the query request
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        //analyse the aggregations of the result
        Aggregations aggregations = response.getAggregations();

        //get aggregations of the brand
        Terms brandTerms = aggregations.get("brand_agg");
        List<? extends  Terms.Bucket> brandBuckets = brandTerms.getBuckets();

        //get aggregations of the category
        Terms categoryTerms = aggregations.get("category_agg");
        List<? extends Terms.Bucket> categoryBuckets = categoryTerms.getBuckets();

        //get the keys of each aggregation
        List<String> brands = new ArrayList<>();
        List<String> categories = new ArrayList<>();

        for (Terms.Bucket bucket : brandBuckets){
            brands.add(bucket.getKeyAsString());
//            log.info("brand "+bucket.getKeyAsString());
        }

        for(Terms.Bucket bucket: categoryBuckets){
            categories.add(bucket.getKeyAsString());
//            log.info("category "+bucket.getKeyAsString());
        }

        results.put("brand", brands);
        results.put("category", categories);
        log.info("results"+ results);

        return results;
    }
}
