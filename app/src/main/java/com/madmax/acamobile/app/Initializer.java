package com.madmax.acamobile.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.madmax.acamobile.interfaces.GetStockCompleteListener;
import com.madmax.acamobile.models.BrandModel;
import com.madmax.acamobile.models.PriceModel;
import com.madmax.acamobile.models.ProductModel;
import com.madmax.acamobile.models.StockModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Initializer {
    Context context;
    String userId;

    public static ArrayList<ProductModel> products=new ArrayList<>();
    public static ArrayList<BrandModel> brands=new ArrayList<>();
    public static ArrayList<StockModel> stocks=new ArrayList<>();

    public Initializer(Context context) {
        this.context = context;
        this.userId=userId;

    }

    public Initializer(String userId) {
        this.userId = userId;
    }

    public void initialize(){
        getProducts();
    }



    public void getStocks(GetStockCompleteListener listener){
        new Thread(() -> {
            MyHttp myHttp = new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    stocks.clear();
                    try {
                        JSONArray ja=new JSONArray(response);
                        for(int i=0;i<ja.length();i++){
                            JSONObject jo=ja.getJSONObject(i);
                            String name=jo.getString("name");
                            int stock_id=jo.getInt("stock_id");
                            stocks.add(new StockModel(stock_id,userId,name));
                        }

                        listener.onComplete();
                    }catch (Exception e){

                    }

                }

                @Override
                public void onError(String msg) {
                    Log.e("stockReqErrr ",msg);

                }
            }).url(Routing.GET_STOCKS+"?owner_id="+userId);
            myHttp.runTask();
        }).start();
    }

    public void getProducts(){

        Thread t=new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    try{
                        products.clear();
                        brands.clear();
                        JSONObject jo=new JSONObject(response);
                        JSONArray mainProductJSONArr=jo.getJSONArray("main_product");
                        for(int i=0;i<mainProductJSONArr.length();i++){
                            JSONObject jo2=mainProductJSONArr.getJSONObject(i);
                            String product_name=jo2.getString("product_name");
                            float point=(float)jo2.getDouble("point");
                            float discount=(float)jo2.getDouble("discount");
                            int product_id=jo2.getInt("product_id");
                            int brand_id=jo2.getInt("brand_id");
                            int pack=jo2.getInt("pack");
                            float pack_price=(float)jo2.getDouble("pack_price");

                            ProductModel model=new ProductModel(product_id,brand_id,product_name,point,discount,pack,pack_price);
                            model.addPrice(Price.retail_price,new PriceModel(0,(float)jo2.getDouble("retail_price")));
                            model.addPrice(Price.five_price,new PriceModel(5,(float)jo2.getDouble("five_price")));
                            model.addPrice(Price.ten_price,new PriceModel(10,(float)jo2.getDouble("ten_price")));
                            model.addPrice(Price.twenty_price,new PriceModel(20,(float)jo2.getDouble("twenty_price")));
                            model.addPrice(Price.fifty_price,new PriceModel(50,(float)jo2.getDouble("fifty_price")));
                            model.addPrice(Price.hundred_price,new PriceModel(100,(float)jo2.getDouble("hundred_price")));
                            model.addPrice(Price.two_hundred_price,new PriceModel(200,(float)jo2.getDouble("two_hundred_price")));
                            model.addPrice(Price.three_hundred_price,new PriceModel(300,(float)jo2.getDouble("three_hundred_price")));
                            model.addPrice(Price.five_hundred_price,new PriceModel(500,(float)jo2.getDouble("five_hundred_price")));
                            model.addPrice(Price.thousand_price,new PriceModel(1000,(float)jo2.getDouble("thousand_price")));
                            model.addPrice(Price.two_thousand_price,new PriceModel(2000,(float)jo2.getDouble("two_thousand_price")));
                            model.addPrice(Price.three_thousand_price,new PriceModel(3000,(float)jo2.getDouble("three_thousand_price")));
                            model.addPrice(Price.five_thousand_price,new PriceModel(5000,(float)jo2.getDouble("five_thousand_price")));

                            products.add(model);

                        }

                        JSONArray brandArr=jo.getJSONArray("brands");
                        for(int i=0;i<brandArr.length();i++){
                            JSONObject jo2=brandArr.getJSONObject(i);
                            int brand_id=jo2.getInt("id");
                            String brand_name=jo2.getString("brand_name");

                            brands.add(new BrandModel(brand_id,brand_name));

                        }



                    }catch (Exception e){
                        Log.e("Prouct JSON Errr ",e.toString());
                    }
                }
                @Override
                public void onError(String msg) {
                    Log.e("Product Req Err ",msg);

                }
            }).url(Routing.GET_PRODUCT_LIST);
            myHttp.runTask();
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
