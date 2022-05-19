package com.madmax.acamobile.models;

import java.util.ArrayList;

public class StockModel {
    int stock_id;
    String owner_id;
    String name;

    ArrayList<Product> products=new ArrayList<>();

    public StockModel(int stock_id, String owner_id, String name) {
        this.stock_id = stock_id;
        this.owner_id = owner_id;
        this.name = name;
    }

    public ArrayList<Product> getProducts() {
        return products;
    }

    public StockModel(int stock_id, String name, ArrayList<Product> products ) {

        this.stock_id = stock_id;
        this.name = name;
        this.products=products;
    }

    public int getStock_id() {
        return stock_id;
    }

    public String getOwner_id() {
        return owner_id;
    }

    public String getName() {
        return name;
    }
}
