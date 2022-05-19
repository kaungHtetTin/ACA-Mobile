package com.madmax.acamobile.models;

public class PriceModel {
    int count;
    float price;

    public PriceModel(int count, float price) {
        this.count = count;
        this.price = price;
    }

    public int getCount() {
        return count;
    }

    public float getPrice() {
        return price;
    }
}
