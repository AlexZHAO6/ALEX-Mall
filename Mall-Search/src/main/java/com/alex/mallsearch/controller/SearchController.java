package com.alex.mallsearch.controller;

import com.alex.mallsearch.service.MallSearchService;
import com.alex.mallsearch.vo.SearchParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SearchController {
    @Autowired
    private MallSearchService mallSearchService;
    @GetMapping("/list.html")
    public String listPage(SearchParam searchParam){
        Object search = mallSearchService.search(searchParam);
        return null;
    }
}
