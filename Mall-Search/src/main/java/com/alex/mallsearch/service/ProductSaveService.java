package com.alex.mallsearch.service;

import com.alex.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

public interface ProductSaveService {
    boolean productStatusUp(List<SkuEsModel> models) throws IOException;
}
