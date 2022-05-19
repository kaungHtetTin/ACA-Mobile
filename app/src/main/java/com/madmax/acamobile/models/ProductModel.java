package com.madmax.acamobile.models;

public class ProductModel {
    int product_id;
    int brand_id;
    String product_name;
    float point;
    float discount;
    PriceModel [] prices=new PriceModel[13];

    int pack;
    float pack_price;
    int quantity=0;
    float selectedPrice=0;
    int foc=0;


    public ProductModel(int product_id,int brand_id, String product_name, float point, float discount,int pack,float pack_price) {
        this.product_id = product_id;
        this.product_name = product_name;
        this.point = point;
        this.discount = discount;
        this.pack=pack;
        this.pack_price=pack_price;
        this.brand_id=brand_id;

    }

    public int getBrand_id() {
        return brand_id;
    }

    public int getPack() {
        return pack;
    }

    public int getFoc() {
        return foc;
    }

    public void setFoc(int foc) {
        this.foc = foc;
    }

    public float getPack_price() {
        return pack_price;
    }

    public float getSelectedPrice() {
        return selectedPrice;
    }

    public void setSelectedPrice(float selectedPrice) {
        this.selectedPrice = selectedPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getProduct_id() {
        return product_id;
    }

    public String getProduct_name() {
        return product_name;
    }

    public float getPoint() {
        return point;
    }

    public float getDiscount() {
        return discount;
    }

    public PriceModel[] getPrices() {
        return prices;
    }

    public float getPrice(int type){
        return  prices[type].getPrice();
    }

    public void addPrice(int position,PriceModel p){
        if(position==0)selectedPrice=p.getPrice();
        prices[position]=p;
    }


}
