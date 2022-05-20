package com.palchevskyi.productapplication.service;

import com.palchevskyi.productapplication.model.Product;

import java.util.List;

public interface ProductsService {
    List<Product> getProducts(String uniqIds, String sku);
}
