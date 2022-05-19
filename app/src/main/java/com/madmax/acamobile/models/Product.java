package com.madmax.acamobile.models;

public class Product{
    int product_id;
    String product_name;
    int count;

    boolean isQtyChange;  // this is used for product transfer among stocks

    public Product(int product_id, String product_name, int count) {
        this.product_id = product_id;
        this.product_name = product_name;
        this.count = count;
        isQtyChange=false;
    }

    public int getProduct_id() {
        return product_id;
    }

    public String getProduct_name() {
        return product_name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }


    public boolean isQtyChange() {
        return isQtyChange;
    }

    public void setQtyChange(boolean qtyChange) {
        isQtyChange = qtyChange;
    }
}