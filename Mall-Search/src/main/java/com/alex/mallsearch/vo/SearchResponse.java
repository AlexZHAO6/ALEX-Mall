package com.alex.mallsearch.vo;

import com.alex.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.List;

@Data
public class SearchResponse {
    private List<SkuEsModel> products;
    private Integer pageNum;
    //total number of records
    private Long total;
    private Integer totalPage;
    private List<BrandVo> brands;
    private List<AttrVo> attrs;
    private List<CatalogVo> catalogs;

    @Data
    public static class BrandVo{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }
    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValues;
    }
    @Data
    public static class CatalogVo{
        private Long catalogId;
        private String catalogName;
    }
}
