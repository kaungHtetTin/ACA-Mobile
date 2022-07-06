package com.madmax.acamobile.models;

public class InvestmentModel {
    String productName;
    int product_id;
    int quantity;
    int totalAmount;

    public InvestmentModel(String productName, int product_id, int quantity, int totalAmount) {
        this.productName = productName;
        this.product_id = product_id;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
    }

    public String getProductName() {
        return productName;
    }

    public int getProduct_id() {
        return product_id;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getTotalAmount() {
        return totalAmount;
    }
}
