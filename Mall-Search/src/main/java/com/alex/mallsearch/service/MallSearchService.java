package com.alex.mallsearch.service;

import com.alex.mallsearch.vo.SearchParam;

public interface MallSearchService {
    /**
     * search function using elasticSearch
     */
    Object search(SearchParam searchParam);
}
