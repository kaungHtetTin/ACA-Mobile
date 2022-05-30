package com.madmax.acamobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.madmax.acamobile.adapters.MainPanelAdapter;
import com.madmax.acamobile.adapters.ProductAdapter;
import com.madmax.acamobile.app.Initializer;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.models.PanelModel;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

public class ProductListActivity extends AppCompatActivity {

    /*
    * 20 agent
    * 50 special agent
    * 100 VIP
    * 200/300 dealer
    * 500 wholesaler
    * 1000 silver wholesaler
    * 2000 gold whole saler
    * 3000 platinum whole saler
    * 5000 CEO
    *
    * */


    RecyclerView recyclerView;
    ProductAdapter adapter;
    SharedPreferences sharedPreferences;

    TextView tv_200_price,tv_300_price,tv_500_price,tv_1000_price,tv_2000_price,tv_3000_price,tv_5000_price;
    int rank_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        rank_id=sharedPreferences.getInt("rank_id",1);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        setTitle("Products");
        setUpView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("ROTATE").setIcon(R.drawable.ic_baseline_screen_rotation_24)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }


    private void setUpView(){
        recyclerView=findViewById(R.id.recyclerView);
        tv_200_price=findViewById(R.id.tv_200_price);
        tv_300_price=findViewById(R.id.tv_300_price);
        tv_500_price=findViewById(R.id.tv_500_price);
        tv_1000_price=findViewById(R.id.tv_1000_price);
        tv_2000_price=findViewById(R.id.tv_2000_price);
        tv_3000_price=findViewById(R.id.tv_3000_price);
        tv_5000_price=findViewById(R.id.tv_5000_price);

        LinearLayoutManager lm=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lm);
        //recycler.addItemDecoration(new SpacingItemDecoration(2, XUtils.toPx(this, 2), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new ProductAdapter(this, Initializer.products,rank_id);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();


        if(rank_id>1){
            tv_200_price.setVisibility(View.VISIBLE);
        }

        if(rank_id>2){
            tv_500_price.setVisibility(View.VISIBLE);
        }

        if(rank_id>3){
            tv_1000_price.setVisibility(View.VISIBLE);
        }

        if(rank_id>4){
            tv_2000_price.setVisibility(View.VISIBLE);
        }

        if(rank_id>5){
            tv_3000_price.setVisibility(View.VISIBLE);
        }

        if(rank_id>6){
            tv_5000_price.setVisibility(View.VISIBLE);
        }
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
}