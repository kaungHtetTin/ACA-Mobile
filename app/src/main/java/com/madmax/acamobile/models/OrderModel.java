package com.madmax.acamobile.models;

public class OrderModel {

    int product_id;
    float price;
    int discount;
    float point;
    int quantity;
    float Profit;
    double amount;
    int foc;

    String productName;

    public OrderModel(){}

    public OrderModel(int product_id, float price, int discount, float point, int quantity, double amount, int foc) {

        this.product_id = product_id;
        this.price = price;
        this.discount = discount;
        this.point = point;
        this.quantity = quantity;
        this.amount = amount;
        this.foc = foc;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getFoc() {
        return foc;
    }

    public void setFoc(int foc) {
        this.foc = foc;
    }

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public float getPoint() {
        return point;
    }

    public void setPoint(float point) {
        this.point = point;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public float getProfit() {
        return Profit;
    }

    public void setProfit(float profit) {
        Profit = profit;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
