package com.madmax.acamobile.models;

public class BrandModel {
    int brand_id;
    String brand_name;

    public BrandModel(int brand_id, String brand_name) {
        this.brand_id = brand_id;
        this.brand_name = brand_name;
    }

    public int getBrand_id() {
        return brand_id;
    }

    public String getBrand_name() {
        return brand_name;
    }
}
