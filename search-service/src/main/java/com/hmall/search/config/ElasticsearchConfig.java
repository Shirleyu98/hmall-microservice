package com.hmall.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;

@Configuration
public class ElasticsearchConfig {

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClient(
                RestClient.builder(
                        HttpHost.create("http://192.168.255.130:9200")
                )
        );
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate(RestHighLevelClient client,
                                                         ElasticsearchConverter converter) {
        return new ElasticsearchRestTemplate(client, converter);
    }
}
