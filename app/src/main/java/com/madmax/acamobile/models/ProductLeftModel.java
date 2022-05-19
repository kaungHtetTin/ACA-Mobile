package com.madmax.acamobile.models;

public class ProductLeftModel {
    String product_id;
    String product_name;
    int count;

    public ProductLeftModel(String product_id, String product_name, int count) {
        this.product_id = product_id;
        this.product_name = product_name;
        this.count = count;
    }

    public String getProduct_id() {
        return product_id;
    }

    public String getProduct_name() {
        return product_name;
    }

    public int getCount() {
        return count;
    }
}
