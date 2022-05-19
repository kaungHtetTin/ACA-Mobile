package com.madmax.acamobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.madmax.acamobile.adapters.StockProductUpdateAdapter;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.models.ProductLeftModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

public class StockProductUpdateActivity extends AppCompatActivity {


    SwipeRefreshLayout swipe;
    RecyclerView recyclerView;
    String stock_id;
    Executor postExecutor;

    ArrayList<ProductLeftModel> products=new ArrayList<>();
    StockProductUpdateAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_product_update);
        stock_id=getIntent().getExtras().getString("stock_id");
        setTitle(getIntent().getExtras().getString("stock_name")+ " Stock");

        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        postExecutor= ContextCompat.getMainExecutor(this);
        setUpView();

    }




    private void setUpView(){
        swipe=findViewById(R.id.swipe);
        recyclerView=findViewById(R.id.recyclerView);
        adapter=new StockProductUpdateAdapter(this,products);
        LinearLayoutManager lm=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(adapter);


        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getProductCountByOneStock();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        getProductCountByOneStock();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getProductCountByOneStock(){
        swipe.setRefreshing(true);
        new Thread(() -> {
            MyHttp myHttp = new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    products.clear();
                    swipe.setRefreshing(false);
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                             try{
                                 JSONArray ja=new JSONArray(response);
                                 for(int i=0;i<ja.length();i++){
                                     JSONObject jo=ja.getJSONObject(i);
                                     String id=jo.getString("id"); //this is not actual product id .Row ID
                                     String name=jo.getString("product_name");
                                     int count=jo.getInt("count");

                                     products.add(new ProductLeftModel(id,name,count));

                                 }

                                 adapter.notifyDataSetChanged();
                             }catch (Exception e){
                                 Log.e("StockOneJson ",e.toString());
                             }
                        }
                    });

                }

                @Override
                public void onError(String msg) {
                    Log.e("stockReqErrr ",msg);

                }
            }).url(Routing.PRODUCT_LEFT_BY_ONE_STOCK+"?stock_id="+stock_id);
            myHttp.runTask();
        }).start();
    }
}