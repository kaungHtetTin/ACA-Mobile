package com.madmax.acamobile;

import static com.madmax.acamobile.app.Initializer.stocks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.madmax.acamobile.adapters.StockAdapter;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.app.Initializer;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.interfaces.GetStockCompleteListener;
import com.madmax.acamobile.models.StockModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

public class StockListActivity extends AppCompatActivity {
    SwipeRefreshLayout swipe;
    RecyclerView recyclerView;

    StockAdapter adapter;
    Executor postExecutor;
    String userId;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_list);
        postExecutor= ContextCompat.getMainExecutor(this);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=sharedPreferences.getString("userId",null);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Stock");
        setUpView();

    }

    private void setUpView(){
        swipe=findViewById(R.id.swipe);
        recyclerView=findViewById(R.id.recyclerView);


        LinearLayoutManager lm=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lm);
        adapter=new StockAdapter(this,stocks);
        recyclerView.setAdapter(adapter);


        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getStock();
            }
        });

    }

    @Override
    protected void onResume() {
        getStock();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("ADD")
                .setIcon(R.drawable.ic_add)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {


        if(item.getItemId()==android.R.id.home){
            finish();
        }else{
            if(item.getTitle().equals("ADD")) {
                Intent intent = new Intent(StockListActivity.this, DataUpdateActivity.class);
                intent.putExtra("key", "Not Using This Value");
                intent.putExtra("url", Routing.ADD_NEW_STOCK);
                intent.putExtra("message", "Add new stock");
                intent.putExtra("hint", "Enter stock name");
                intent.putExtra("contentId", "Not Using This Value");
                startActivity(intent);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void getStock(){
        swipe.setRefreshing(true);
        Initializer initializer=new Initializer(userId);
        initializer.getStocks(new GetStockCompleteListener() {
            @Override
            public void onComplete() {
                postExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        swipe.setRefreshing(false);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

}