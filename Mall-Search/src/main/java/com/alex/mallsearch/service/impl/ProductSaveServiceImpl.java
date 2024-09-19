package com.alex.mallsearch.service.impl;

import com.alex.common.to.es.SkuEsModel;
import com.alex.mallsearch.constant.EsConstant;
import com.alex.mallsearch.service.ProductSaveService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

import static com.alex.mallsearch.config.ElasticSearchConfig.COMMON_OPTIONS;

@Service
@Slf4j
public class ProductSaveServiceImpl implements ProductSaveService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Override
    public boolean productStatusUp(List<SkuEsModel> models) throws IOException {
        //save data into ES
        BulkRequest bulkRequest = new BulkRequest();
        for(SkuEsModel model : models){
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(model.getSkuId().toString());
            String jsonString = JSON.toJSONString(model);
            indexRequest.source(jsonString, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulkItemResponses = restHighLevelClient.bulk(bulkRequest, COMMON_OPTIONS);

        //TODO: deal with Es errors
        if(bulkItemResponses.hasFailures()){
            log.error("some item onboard to es failed");
            return false;
        }

        return true;
    }
}
