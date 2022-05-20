package com.palchevskyi.productapplication.controller;

import com.palchevskyi.productapplication.model.Product;
import com.palchevskyi.productapplication.service.ProductsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("products")
public class ProductsController {
    private final ProductsService productsService;

    public ProductsController(ProductsService productsService) {
        this.productsService = productsService;
    }

    @GetMapping
    public List<Product> getProducts(@RequestParam(required = false) String uniqIds, @RequestParam(required = false) String sku) {
        return productsService.getProducts(uniqIds, sku);
    }
}
