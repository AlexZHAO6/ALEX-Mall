package com.alex.mallsearch.vo;

import lombok.Data;

import java.util.List;

@Data
public class SearchParam {
    private String keyword;
    private Long catalog3Id;
    /**
     * sort = saleCount_asc/desc
     * sort = hotScore_asc/desc
     * sort = skuPrice_asc/desc
     */
    private String sort;
    private Integer hasStock;
    private String skuPrice;
    private List<Long> brandId;
    private List<String> attrs;
    private Integer pageNum;
}
