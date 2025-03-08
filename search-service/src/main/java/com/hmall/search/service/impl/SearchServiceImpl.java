package com.hmall.search.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.search.domain.po.Item;
import com.hmall.search.mapper.SearchMapper;
import com.hmall.search.service.ISearchService;
import org.springframework.stereotype.Service;

@Service
public class SearchServiceImpl extends ServiceImpl<SearchMapper, Item> implements ISearchService {
}
