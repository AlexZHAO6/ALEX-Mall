package com.alex.mallsearch.controller;

import com.alex.common.utils.R;
import com.alex.mallsearch.service.MallSearchService;
import com.alex.mallsearch.vo.SearchParam;
import com.alex.mallsearch.vo.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SearchController {
    @Autowired
    private MallSearchService mallSearchService;
    @GetMapping("/list.html")
    public String listPage(SearchParam searchParam, Model model){
        SearchResponse result = mallSearchService.search(searchParam);
        model.addAttribute("result", result);

        return "list";
    }
}
