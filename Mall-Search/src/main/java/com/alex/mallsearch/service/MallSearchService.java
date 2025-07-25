package com.alex.mallsearch.service;

import com.alex.mallsearch.vo.SearchParam;
import com.alex.mallsearch.vo.SearchResponse;

public interface MallSearchService {
    /**
     * search function using elasticSearch
     */
    SearchResponse search(SearchParam searchParam);
}
