package com.madmax.acamobile.models;

public class SubProductModel {
    int product_id;
    int count_per_package;
    String sub_product_name;

    public SubProductModel(int product_id, int count_per_package, String sub_product_name) {
        this.product_id = product_id;
        this.count_per_package = count_per_package;
        this.sub_product_name = sub_product_name;
    }

    public int getProduct_id() {
        return product_id;
    }

    public int getCount_per_package() {
        return count_per_package;
    }

    public String getSub_product_name() {
        return sub_product_name;
    }
}
