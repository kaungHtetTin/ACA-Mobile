package com.madmax.acamobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.madmax.acamobile.adapters.StockProductAdapter;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.models.Product;
import com.madmax.acamobile.models.StockModel;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

public class ProductLeftActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    String userId;
    public static ArrayList<StockModel> stocks=new ArrayList<>();
    int initial_index=1,target_index=1;
    ArrayList<String> stockNames=new ArrayList<>();

    RecyclerView recyclerView;
    StockProductAdapter adapter;
    Executor postExecutor;
    ProgressBar pb;

    Button bt_transfer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_left);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=sharedPreferences.getString("userId",null);
        postExecutor= ContextCompat.getMainExecutor(this);

        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Product Left");

        setUpView();

    }

    @Override
    protected void onResume() {
        getProductLeftByStocks();
        super.onResume();
    }

    private void setUpView(){

        recyclerView=findViewById(R.id.recyclerView);
        bt_transfer=findViewById(R.id.bt_transfer);
        pb=findViewById(R.id.progressBar);
        adapter=new StockProductAdapter(this,stocks);
        LinearLayoutManager lm=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(adapter);

        bt_transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 showStockSelectorDialog();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("ROTATE").setIcon(R.drawable.ic_baseline_screen_rotation_24)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }else{
            if(item.getTitle().equals("ROTATE")){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
        return super.onOptionsItemSelected(item);
    }


    private void getProductLeftByStocks(){
        pb.setVisibility(View.VISIBLE);
        new Thread(() -> {
            MyHttp myHttp = new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    stocks.clear();
                    stockNames.clear();
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONArray ja=new JSONArray(response);
                                stocks.add(0,new StockModel(0,"Stocks", (String) null));
                                for(int i=0;i<ja.length();i++){
                                    JSONObject jo=ja.getJSONObject(i);
                                    int stock_id=jo.getInt("stock_id");
                                    String name=jo.getString("name");
                                    String items=jo.getString("items");
                                    JSONArray ja1=new JSONArray(items);

                                    ArrayList<Product> products=new ArrayList<>();

                                    for(int j=0;j<ja1.length();j++) {
                                        JSONObject jo1 = ja1.getJSONObject(j);
                                        int product_id=jo1.getInt("product_id");
                                        String product_name = jo1.getString("product_name");
                                        int count=jo1.getInt("count");
                                        products.add(new Product(product_id,product_name,count));
                                    }

                                    stocks.add(new StockModel(stock_id,name,products));
                                    stockNames.add(name);
                                }


                                pb.setVisibility(View.GONE);

                            }catch (Exception e){
                                Toast.makeText(getApplicationContext(),"count total : "+e.toString(),Toast.LENGTH_SHORT).show();
                            }
                            getTotalCount();
                        }
                    });

                }

                @Override
                public void onError(String msg) {
                    Log.e("stockReqErrr ",msg);

                }
            }).url(Routing.PRODUCT_LEFT_BY_STOCK+"?owner_id="+userId);
            myHttp.runTask();
        }).start();
    }

    private void getTotalCount(){
        StockModel s=stocks.get(1);
        ArrayList<Product> model=s.getProducts();
        ArrayList<Product> newModel=new ArrayList<>();
        int length=model.size();
        int[] totals=new int[length];
        for(int i=0;i<length;i++){
            for(int j=1;j<stocks.size();j++){
                ArrayList<Product> products=stocks.get(j).getProducts();
                for(int k=0;k<products.size();k++){
                    if(k==i){
                        totals[i]+=products.get(i).getCount();
                    }
                }
            }
            Product product=model.get(i);
            newModel.add(new Product(product.getProduct_id(), product.getProduct_name(),totals[i]));

        }

        stocks.add(new StockModel(0,"Total",newModel));
        adapter.notifyDataSetChanged();

    }

    private void showStockSelectorDialog(){

        Spinner sp_initial,sp_target;
        Button bt_continue;


        View v = getLayoutInflater().inflate(R.layout.dialog_stock_selector, null);
        sp_initial=v.findViewById(R.id.sp_initial);
        sp_target=v.findViewById(R.id.sp_target);
        bt_continue=v.findViewById(R.id.bt_continue);

        ArrayAdapter<String> stockAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,stockNames);
        sp_initial.setAdapter(stockAdapter);
        sp_initial.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                initial_index=position+1;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        sp_target.setAdapter(stockAdapter);
        sp_target.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                target_index=position+1;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(v);
        final AlertDialog ad = builder.create();


        bt_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(initial_index==target_index){
                    Toast.makeText(getApplicationContext(), "Please Select the target stock", Toast.LENGTH_SHORT).show();
                }else{
                    Intent intent=new Intent(ProductLeftActivity.this,ProductTransferActivity.class);
                    intent.putExtra("initial_index",initial_index);
                    intent.putExtra("target_index",target_index);
                    startActivity(intent);
                    ad.dismiss();
                }
            }
        });

        ad.show();
    }

}
