package com.hmall.search.repository;

import com.hmall.search.domain.po.ItemDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemDocRepository extends ElasticsearchRepository<ItemDoc, String> {
}
