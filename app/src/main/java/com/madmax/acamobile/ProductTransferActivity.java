package com.madmax.acamobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.madmax.acamobile.adapters.ProductTransferAdapter;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.models.Product;
import com.madmax.acamobile.models.StockModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

public class ProductTransferActivity extends AppCompatActivity {


    RecyclerView recyclerView;
    ProgressBar pb;



    Executor postExecutor;
    ProductTransferAdapter adapter;
    SharedPreferences sharedPreferences;
    String userId,authToken;

    int initial_index,target_index,initialStockId,targetStockId;

    TextView tv_initial,tv_target;

    StockModel initialStock,targetStock;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_transfer);
        postExecutor= ContextCompat.getMainExecutor(this);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=sharedPreferences.getString("userId",null);
        authToken=sharedPreferences.getString("authToken",null);
        initial_index=getIntent().getExtras().getInt("initial_index");
        target_index=getIntent().getExtras().getInt("target_index");

        initialStock=ProductLeftActivity.stocks.get(initial_index);
        targetStock=ProductLeftActivity.stocks.get(target_index);

        initialStockId=initialStock.getStock_id();
        targetStockId=targetStock.getStock_id();

        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Transfer");
        setUpView();


    }


    private void setUpView(){
        recyclerView=findViewById(R.id.recyclerView);
        tv_initial=findViewById(R.id.tv_initial);
        tv_target=findViewById(R.id.tv_target);
        pb=findViewById(R.id.pb);

        tv_initial.setText(initialStock.getName());
        tv_target.setText(targetStock.getName());

        LinearLayoutManager lm=new LinearLayoutManager(ProductTransferActivity.this);
        recyclerView.setLayoutManager(lm);
        adapter=new ProductTransferAdapter(ProductTransferActivity.this,initialStock.getProducts(),targetStock.getProducts());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        pb.setVisibility(View.GONE);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("DONE")
                .setIcon(R.drawable.ic_baseline_check_24)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }else{
            if(item.getTitle().equals("DONE")){
                ArrayList<Product> initialProducts=adapter.getInitialProducts();
                ArrayList<Product> targetProducts= adapter.getTargetProducts();

                transferNow(makeProductUpdateJSON(initialProducts),makeProductUpdateJSON(targetProducts));

            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void transferNow(String initialJSON,String targetJSON){
        pb.setVisibility(View.VISIBLE);
        new Thread(() -> {
            MyHttp myHttp = new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {

                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject jo = new JSONObject(response);
                                boolean isSuccess = jo.getString("status").equals("success");
                                if (isSuccess) {
                                    finish();
                                } else {
                                    pb.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(), "Fail! Try again.", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Log.e("TxResJSONErr ",e.toString());
                            }
                        }
                    });

                }

                @Override
                public void onError(String msg) {
                    Log.e("stockTxErr ",msg);
                }
            }).url(Routing.TRANSFER_PRODUCT)
                    .field("user_id", userId)
                    .field("auth_token", authToken)
                    .field("initial_stock_id",initialStockId+"")
                    .field("target_stock_id",targetStockId+"")
                    .field("initial_json",initialJSON)
                    .field("target_json",targetJSON);

            myHttp.runTask();
        }).start();
    }


    private String makeProductUpdateJSON( ArrayList<Product> products){
        JSONArray JSONArr=new JSONArray();
        for(Product p:products){
           if(p.isQtyChange()){
               JSONObject jo = new JSONObject();
               try {
                   jo.put("product_id",p.getProduct_id());
                   jo.put("count",p.getCount());
                   JSONArr.put(jo);
               } catch (JSONException e) {
                   e.printStackTrace();
               }
           }
        }

        return JSONArr.toString();
    }



}