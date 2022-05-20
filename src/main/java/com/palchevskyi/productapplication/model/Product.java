package com.palchevskyi.productapplication.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder({"uniqId", "sku", "nameTitle", "description", "listPrice", "salePrice", "category", "categoryTree",
        "averageProductRating", "productUrl", "productImageUrls", "brand", "totalNumberReviews", "reviews"})
public class Product {
    private String uniqId;
    private String sku;
    private String nameTitle;
    private String description;
    private String listPrice;
    private String salePrice;
    private String category;
    private String categoryTree;
    private String averageProductRating;
    private String productUrl;
    private String productImageUrls;
    private String brand;
    private String totalNumberReviews;
    private String reviews;
}
