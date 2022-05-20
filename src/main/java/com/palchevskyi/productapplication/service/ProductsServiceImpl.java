package com.palchevskyi.productapplication.service;

import com.netflix.discovery.EurekaClient;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.palchevskyi.productapplication.model.Product;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.core.UriBuilder;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductsServiceImpl implements ProductsService {
    private final RestTemplate restTemplate;

    public ProductsServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @HystrixCommand(fallbackMethod = "getProductsFallBack")
    public List<Product> getProducts(String uniqIds, String sku) {
        List<Product> filteredProducts = callCatalogService(uniqIds, sku);
        Map<String, Boolean> availableProducts = callInventoryService(uniqIds);

        if (filteredProducts.size() == 0 || availableProducts.size() == 0) return new ArrayList<>();

        filteredProducts = filteredProducts
                .stream()
                .filter(product -> availableProducts.get(product.getUniqId()))
                .collect(Collectors.toList());

        return filteredProducts;
    }

    public List<Product> callCatalogService(String uniqIds, String sku) {
        String catalogUrl = "http://localhost:8080/products";

        //build query params
        if (StringUtils.hasText(uniqIds)) catalogUrl = UriBuilder.fromUri(catalogUrl).queryParam("uniqIds", uniqIds).build().toString();
        if (StringUtils.hasText(sku)) catalogUrl = UriBuilder.fromUri(catalogUrl).queryParam("sku", uniqIds).build().toString();

        return Arrays.asList(Objects.requireNonNull(restTemplate.getForObject(catalogUrl, Product[].class)));
    }

    private Map<String, Boolean> callInventoryService(String uniqIds) {
        String catalogUrl = "http://localhost:8079/products";

        //build query params
        if (StringUtils.hasText(uniqIds)) catalogUrl = UriBuilder.fromUri(catalogUrl).queryParam("uniqIds", uniqIds).build().toString();

        return Objects.requireNonNull(restTemplate.exchange(catalogUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Boolean>>() {
        }).getBody());
    }

    @SuppressWarnings("unused")
    public List<Product> getProductsFallBack(String uniqIds, String sku) {
        return new ArrayList<>();
    }
}
