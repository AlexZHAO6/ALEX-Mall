package com.alex.mallsearch.service.impl;

import com.alex.common.to.es.SkuEsModel;
import com.alex.mallsearch.config.ElasticSearchConfig;
import com.alex.mallsearch.constant.EsConstant;
import com.alex.mallsearch.service.MallSearchService;
import com.alex.mallsearch.vo.SearchParam;
import com.alex.mallsearch.vo.SearchResponse;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService {
    @Autowired
    private RestHighLevelClient client;
    @Override
    public SearchResponse search(SearchParam searchParam) {
        SearchRequest searchRequest = buildSearchRequest(searchParam);

        SearchResponse result = null;
        try {
            org.elasticsearch.action.search.SearchResponse response = client.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
            result = buildSearchResponse(response, searchParam);
        } catch (IOException e) {
           e.printStackTrace();
        }

        return result;
    }

    private SearchResponse buildSearchResponse(org.elasticsearch.action.search.SearchResponse response, SearchParam searchParam) {
        SearchResponse searchResponse = new SearchResponse();
        SearchHits hits = response.getHits();
        Aggregations aggregations = response.getAggregations();
        //1. set all the searched products
        List<SkuEsModel> esModelList = new ArrayList<>();
        if(hits.getHits() != null && hits.getHits().length > 0){
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                esModelList.add(skuEsModel);
            }
        }
        searchResponse.setProducts(esModelList);
        //2. set all the attr info
        List<SearchResponse.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attrAgg = aggregations.get("attr_agg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            SearchResponse.AttrVo attrVo = new SearchResponse.AttrVo();
            long attrId = bucket.getKeyAsNumber().longValue();
            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            List<String> attrValues = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream()
                    .map(item -> item.getKeyAsString()).collect(Collectors.toList());

            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValues(attrValues);
            attrVos.add(attrVo);
        }
        searchResponse.setAttrs(attrVos);
        //3. set all the brand info
        List<SearchResponse.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brandAgg = aggregations.get("brand_agg");
        for (Terms.Bucket bucket : brandAgg.getBuckets()) {
            SearchResponse.BrandVo brandVo = new SearchResponse.BrandVo();
            brandVo.setBrandId(bucket.getKeyAsNumber().longValue());

            String brandImg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg"))
                    .getBuckets().get(0).getKeyAsString();


            String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg"))
                    .getBuckets().get(0).getKeyAsString();

            brandVo.setBrandImg(brandImg);
            brandVo.setBrandName(brandName);
            brandVos.add(brandVo);
        }
        searchResponse.setBrands(brandVos);
        //4. set all the cataLog info
        List<SearchResponse.CatalogVo> catalogVos = new ArrayList<>();
        ParsedLongTerms catalogAgg = aggregations.get("catalog_agg");
        List<? extends Terms.Bucket> buckets = catalogAgg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResponse.CatalogVo catalogVo = new SearchResponse.CatalogVo();
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(keyAsString));

            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalogName = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);
            catalogVos.add(catalogVo);
        }
        searchResponse.setCatalogs(catalogVos);
        //5. set the paging info
        long total = hits.getTotalHits().value;
        searchResponse.setTotal(total);
        searchResponse.setTotalPage((int) (total % EsConstant.PRODUCT_PAGESIZE == 0 ? total / EsConstant.PRODUCT_PAGESIZE
                        : total / EsConstant.PRODUCT_PAGESIZE + 1));
        searchResponse.setPageNum(searchParam.getPageNum());

        return null;
    }

    private SearchRequest buildSearchRequest(SearchParam searchParam) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        /**
         * fuzzy matching & filter
         */
        //1. filter query
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //1.1 must match
        if(!StringUtils.isEmpty(searchParam.getKeyword())){
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", searchParam.getKeyword()));
        }
        //1.2 filter by catalogId
        if(searchParam.getCatalog3Id() != null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", searchParam.getCatalog3Id()));
        }
        //1.3 filter by brandIds
        if(searchParam.getBrandIds() != null && searchParam.getBrandIds().size() > 0){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", searchParam.getBrandIds()));
        }
        //1.4 filter by attrs
        if(searchParam.getAttrs() != null && searchParam.getAttrs().size() > 0){
            for(String attr : searchParam.getAttrs()){
                //each attr should generate a nested query!!
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();

                String[] s = attr.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));

                NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQueryBuilder.filter(nestedQueryBuilder);
            }
        }
        //1.5 filter by Stock
        if(searchParam.getHasStock() != null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", searchParam.getHasStock() == 1));
        }

        //1.6 filter by price range
        if(!StringUtils.isEmpty(searchParam.getSkuPrice())){
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = searchParam.getSkuPrice().split("_");
            if(s.length == 2){
                rangeQuery.gte(s[0]).lte(s[1]);
            }
            else if(s.length == 1){
                if(searchParam.getSkuPrice().startsWith("_")){
                    rangeQuery.lte(s[0]);
                }
                else {
                    rangeQuery.gte(s[0]);
                }
            }
            boolQueryBuilder.filter(rangeQuery);
        }


        searchSourceBuilder.query(boolQueryBuilder);

        //2. sort query
        //2.1 sort
        if(!StringUtils.isEmpty(searchParam.getSort())){
            String sort = searchParam.getSort();
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            searchSourceBuilder.sort(s[0], order);
        }

        //2.2 paging
        searchSourceBuilder.from((searchParam.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

        //2.3 highLight
        if(!StringUtils.isEmpty(searchParam.getKeyword())){
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }

        //3. aggregation
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg");
        brandAgg.field("brandId");
        brandAgg.size(50);
        TermsAggregationBuilder brandNameAgg = AggregationBuilders.terms("brand_name_agg").field("brandName").size(1);
        TermsAggregationBuilder brandImgAgg = AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1);
        brandAgg.subAggregation(brandNameAgg);
        brandAgg.subAggregation(brandImgAgg);

        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg");
        catalogAgg.field("catalogId");
        catalogAgg.size(50);
        catalogAgg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));

        NestedAggregationBuilder attrAgg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");

        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));

        attrAgg.subAggregation(attrIdAgg);

        searchSourceBuilder.aggregation(brandAgg);
        searchSourceBuilder.aggregation(catalogAgg);
        searchSourceBuilder.aggregation(attrAgg);

        String string = searchSourceBuilder.toString();
        System.out.println("result: " + string);


        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX},searchSourceBuilder);



        return searchRequest;
    }
}
