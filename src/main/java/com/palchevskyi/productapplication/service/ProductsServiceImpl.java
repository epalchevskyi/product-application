package com.palchevskyi.productapplication.service;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.palchevskyi.productapplication.model.Product;
import com.palchevskyi.productapplication.util.HttpUtil;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.core.UriBuilder;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductsServiceImpl implements ProductsService {
    private final RestTemplate restTemplate;
    private final EurekaClient eurekaClient;

    public ProductsServiceImpl(RestTemplateBuilder restTemplateBuilder, EurekaClient eurekaClient) {
        this.restTemplate = restTemplateBuilder.build();
        this.eurekaClient = eurekaClient;
    }

    @HystrixCommand(fallbackMethod = "getProductsFallBack")
    public List<Product> getProducts(String uniqIds, String sku) {
        List<Product> filteredProducts = callCatalogService(uniqIds, sku);
        if (CollectionUtils.isEmpty(filteredProducts)) return new ArrayList<>();

        String filteredUniqIds = filteredProducts.stream().map(Product::getUniqId).collect(Collectors.joining(","));

        Map<String, Boolean> availableProducts = callInventoryService(filteredUniqIds);
        if (CollectionUtils.isEmpty(availableProducts)) return new ArrayList<>();

        filteredProducts = filteredProducts
                .stream()
                .filter(product -> availableProducts.get(product.getUniqId()))
                .collect(Collectors.toList());

        return filteredProducts;
    }

    public List<Product> callCatalogService(String uniqIds, String sku) {
        List<InstanceInfo> catalogInstances = eurekaClient.getApplication("catalog-application").getInstances();
        if (CollectionUtils.isEmpty(catalogInstances)) return new ArrayList<>();

        InstanceInfo catalogInstanceInfo = catalogInstances.get(0);
        String catalogUrl = HttpUtil.buildUrl(catalogInstanceInfo.getHostName(), Integer.toString(catalogInstanceInfo.getPort())) + "/products";

        //build query params
        if (StringUtils.hasText(uniqIds)) catalogUrl = UriBuilder.fromUri(catalogUrl).queryParam("uniqIds", uniqIds).build().toString();
        if (StringUtils.hasText(sku)) catalogUrl = UriBuilder.fromUri(catalogUrl).queryParam("sku", sku).build().toString();

        return Arrays.asList(Objects.requireNonNull(restTemplate.getForObject(catalogUrl, Product[].class)));
    }

    public Map<String, Boolean> callInventoryService(String uniqIds) {
        List<InstanceInfo> inventoryInstances = eurekaClient.getApplication("inventory-application").getInstances();
        if (CollectionUtils.isEmpty(inventoryInstances)) return new HashMap<>();

        InstanceInfo inventoryInstanceInfo = inventoryInstances.get(0);
        String inventoryUrl = HttpUtil.buildUrl(inventoryInstanceInfo.getHostName(), Integer.toString(inventoryInstanceInfo.getPort())) + "/products";;

        //build query params
        if (StringUtils.hasText(uniqIds)) inventoryUrl = UriBuilder.fromUri(inventoryUrl).queryParam("uniqIds", uniqIds).build().toString();

        return Objects.requireNonNull(restTemplate.exchange(inventoryUrl,
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
