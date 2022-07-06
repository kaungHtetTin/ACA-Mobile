package com.madmax.acamobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.madmax.acamobile.adapters.OrderCreateProductAdapter;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.app.Initializer;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.models.MyGroupModel;
import com.madmax.acamobile.models.OrderModel;
import com.madmax.acamobile.models.PriceModel;
import com.madmax.acamobile.models.ProductModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

public class CreateOrderActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    Button bt_order_create,bt_confirm;
    OrderCreateProductAdapter adapter;
    TextView tv_quantity_total,tv_amount_total,tv_point_total,tv_saving_total,tv_profit_total,tv_total_foc,tv_profit_percent;

    TextView tv_group_name,tv_group_status,tv_profit_index,tv_price_header;
    ImageView iv_group_thumb;

    ArrayList<OrderModel> orders=new ArrayList<>();
    ArrayList<ProductModel> productModels=new ArrayList<>();
    CardView card_selected;


    public static MyGroupModel selectedGroup;
    Executor postExecutor;
    String currentUserId,authToken;
    SharedPreferences sharedPreferences;

    public static int price_index,SELECTED_PRICE_INDEX=13;
    double total_amount;
    int total_quantity;
    int brand_id;

    ProgressBar pb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_order);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserId=sharedPreferences.getString("userId",null);
        authToken=sharedPreferences.getString("authToken",null);
        brand_id=getIntent().getExtras().getInt("brand_id");
        selectedGroup=null;
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        price_index=0;
        postExecutor= ContextCompat.getMainExecutor(this);
        setUpView();
    }

    private void setUpView(){
        bt_order_create=findViewById(R.id.bt_create);
        bt_confirm=findViewById(R.id.bt_confirm);
        recyclerView=findViewById(R.id.recyclerView);
        tv_quantity_total=findViewById(R.id.tv_quantity_total);
        tv_amount_total=findViewById(R.id.tv_amount_total);
        tv_point_total=findViewById(R.id.tv_point_total);
        tv_saving_total=findViewById(R.id.tv_saving_total);
        tv_profit_total=findViewById(R.id.tv_profit_total);
        card_selected=findViewById(R.id.card_selected);
        tv_profit_index=findViewById(R.id.tv_profit_index);
        tv_total_foc=findViewById(R.id.tv_foc_total);
        tv_profit_percent=findViewById(R.id.tv_profit_percent);
        tv_price_header=findViewById(R.id.tv_price_header);
        tv_group_name=findViewById(R.id.tv_address);
        tv_group_status=findViewById(R.id.tv_group_status);
        iv_group_thumb=findViewById(R.id.iv_group_thumb);
        iv_group_thumb.setClipToOutline(true);

        pb=findViewById(R.id.pb);

        LinearLayoutManager lm=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new OrderCreateProductAdapter(this, productModels);
        recyclerView.setAdapter(adapter);



        bt_order_create.setOnClickListener(new View.OnClickListener() {  //this is calculate button
            @Override
            public void onClick(View view) {
                price_index=0;
                calculate();

            }
        });

        tv_profit_index.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               showPriceMenu(view);
            }
        });

        tv_price_header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectedPriceMenu(view);
            }
        });

        bt_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bt_confirm.setEnabled(false);
                sendOrder();
            }
        });


        for(int i=0;i<Initializer.products.size();i++){
            ProductModel model=Initializer.products.get(i);
            if(model.getBrand_id()==brand_id){
                productModels.add(model);
            }
        }

        adapter.notifyDataSetChanged();
    }


    private void sendOrder(){

        pb.setVisibility(View.VISIBLE);
        String price_edit;
        if(SELECTED_PRICE_INDEX==13){
            price_edit="0";
        }else{
            price_edit="1";
        }

        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {

                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"The Order has sent successfully",Toast.LENGTH_SHORT).show();
                        }
                    });
                    finish();

                }
                @Override
                public void onError(String msg) {
                    Log.e("orderErr ",msg);
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            pb.setVisibility(View.GONE);
                            bt_confirm.setEnabled(true);
                            Toast.makeText(getApplicationContext(),"Fail! Please try again later",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).url(Routing.SEND_ORDER)
                    .field("agent_id",currentUserId)
                    .field("auth_token",authToken)
                    .field("total_amount",total_amount+"")
                    .field("group_id",selectedGroup.getGroup_id())
                    .field("price_edit",price_edit)
                    .field("productJSON",makeOrderJSON());

            myHttp.runTask();
        }).start();
    }


    private void calculate(){
        ArrayList<ProductModel> temp=new ArrayList<>();
        temp.addAll(adapter.getData());
        adapter.notifyItemRangeChanged(0,temp.size());
        calculateOrder(temp);
    }


    public void calculateOrder(ArrayList<ProductModel> data) {

        double total_profit=0;
        double total_point=0;
        int total_foc=0;
        int count=0;
        total_amount=0;
        total_quantity=0;

        orders.clear();

        for(int i=0;i<data.size();i++){
            ProductModel model=data.get(i);
            PriceModel[] prices=model.getPrices();

            int quantity=model.getQuantity();                       // quantity
            float price=model.getSelectedPrice();
//            int discount=(int)model.getDiscount();
//            float discount_save=(float)(price/100.0)*discount;
//            price=price-discount_save;                              // selected Price

            // calculate amount
            int tempQty=quantity/model.getPack();
            int pack=quantity%model.getPack();
            float pack_price=pack*model.getPack_price();
            double amount=tempQty*price+pack_price;

            float point=model.getPoint()*tempQty;

            PriceModel retail=prices[price_index];

            int foc=model.getFoc();

            //this this for profit
            int focAndQty;
            if(price_index==0){
                focAndQty=foc+quantity;
            }else{
                focAndQty=quantity;
            }
            focAndQty=focAndQty/model.getPack();
            float tempPrice=focAndQty*retail.getPrice();
            float profit= (float) (tempPrice-amount);


            OrderModel order=new OrderModel();
            order.setPrice(price);
            order.setAmount(amount);
            order.setPoint(point);
            order.setProfit(profit);
            order.setProduct_id(model.getProduct_id());
            order.setDiscount((int)model.getDiscount());
            order.setQuantity(quantity);
            order.setFoc(foc);
            orders.add(order);

            total_amount+=amount;
            total_profit+=profit;
            total_point+=point;
            total_quantity+=quantity;
            count+=tempQty;
            total_foc+=foc;

        }

        tv_amount_total.setText(AppUtils.getTwoDecimalDouble(total_amount));
        tv_point_total.setText(AppUtils.getTwoDecimalDouble(total_point));
        tv_quantity_total.setText(count +" / "+total_quantity);
        tv_profit_total.setText(AppUtils.getTwoDecimalDouble(total_profit));
        tv_total_foc.setText(""+total_foc);

        if(total_amount==0){
            tv_profit_percent.setText("0%");
        }else{
            double profit_percent= (total_profit*(100/total_amount));
            tv_profit_percent.setText(AppUtils.getTwoDecimalDouble(profit_percent)+"%");
        }
    }

    private String makeOrderJSON(){
        JSONArray orderJSONArr=new JSONArray();
        for (OrderModel order : orders) {
            try {
                if (order.getQuantity() != 0 || order.getFoc()!=0) {
                    JSONObject jo = new JSONObject();
                    jo.put("product_id", order.getProduct_id());
                    jo.put("price", order.getPrice());
                    jo.put("discount", order.getDiscount());
                    jo.put("quantity", order.getQuantity());
                    jo.put("point", order.getPoint());
                    jo.put("amount", order.getAmount());
                    jo.put("foc",order.getFoc());

                    orderJSONArr.put(jo);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return orderJSONArr.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("ROTATE").setIcon(R.drawable.ic_baseline_screen_rotation_24)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add("DONE").setIcon(R.drawable.ic_baseline_done_24)
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
            }else if(item.getTitle().equals("DONE")){
                price_index=0;
                calculate();

                if(total_quantity>0){
                    Intent intent=new Intent(CreateOrderActivity.this,CreateOrderActivity2.class);
                    startActivity(intent);

                }else{
                    Toast.makeText(getApplicationContext(),"Please enter required quantities",Toast.LENGTH_SHORT).show();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        if(selectedGroup==null){
            card_selected.setVisibility(View.GONE);
            bt_confirm.setVisibility(View.GONE);
            bt_order_create.setVisibility(View.VISIBLE);
        }else{
            bt_order_create.setVisibility(View.GONE);
            bt_confirm.setVisibility(View.VISIBLE);
            card_selected.setVisibility(View.VISIBLE);
            setUpGroupCard();
        }
        super.onResume();
    }


    private void setUpGroupCard(){
        tv_group_name.setText(selectedGroup.getName());
        tv_group_status.setText(selectedGroup.getDescription());
        if(!selectedGroup.getImageUrl().equals(""))
            AppUtils.setPhotoFromRealUrl(iv_group_thumb, Routing.GROUP_COVER_URL+selectedGroup.getImageUrl());
    }


    @Override
    public void onBackPressed() {
        selectedGroup=null;
        super.onBackPressed();
    }

    private void showPriceMenu(View v){
        PopupMenu popup=new PopupMenu(this,v);
        popup.getMenuInflater().inflate(R.menu.price_index_menu,popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @SuppressLint("Recycle")
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id=item.getItemId();
                switch (id){
                    case R.id.menu_retail:
                        price_index=0;
                        break;
                    case R.id.menu_5:
                        price_index=1;
                        break;
                    case R.id.menu_10:
                        price_index=2;
                        break;
                    case R.id.menu_20:
                        price_index=3;
                        break;
                    case R.id.menu_50:
                        price_index=4;
                        break;
                    case R.id.menu_100:
                        price_index=5;
                        break;
                    case R.id.menu_200:
                        price_index=6;
                        break;
                    case R.id.menu_300:
                        price_index=7;
                        break;
                    case R.id.menu_500:
                        price_index=8;
                        break;
                    case R.id.menu_1000:
                        price_index=9;
                        break;
                    case R.id.menu_2000:
                        price_index=10;
                        break;
                    case R.id.menu_3000:
                        price_index=11;
                        break;
                    case R.id.menu_5000:
                        price_index=12;
                        break;
                }

                calculate();

                return true;
            }
        });
        popup.show();
    }

    private void showSelectedPriceMenu(View v){
        PopupMenu popup=new PopupMenu(this,v);
        popup.getMenuInflater().inflate(R.menu.price_menu,popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @SuppressLint("Recycle")
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id=item.getItemId();
                switch (id){
                    case R.id.menu_retail:
                        SELECTED_PRICE_INDEX=0;
                        tv_price_header.setText("Price(Retail)");
                        break;
                    case R.id.menu_5:
                        tv_price_header.setText("Price(5)");
                        SELECTED_PRICE_INDEX=1;
                        break;
                    case R.id.menu_10:
                        tv_price_header.setText("Price(10)");
                        SELECTED_PRICE_INDEX=2;
                        break;
                    case R.id.menu_20:
                        tv_price_header.setText("Price(20)");
                        SELECTED_PRICE_INDEX=3;
                        break;
                    case R.id.menu_50:
                        tv_price_header.setText("Price(50)");
                        SELECTED_PRICE_INDEX=4;
                        break;
                    case R.id.menu_100:
                        tv_price_header.setText("Price(100)");
                        SELECTED_PRICE_INDEX=5;
                        break;
                    case R.id.menu_200:
                        tv_price_header.setText("Price(200)");
                        SELECTED_PRICE_INDEX=6;
                        break;
                    case R.id.menu_300:
                        tv_price_header.setText("Price(300)");
                        SELECTED_PRICE_INDEX=7;
                        break;
                    case R.id.menu_500:
                        tv_price_header.setText("Price(500)");
                        SELECTED_PRICE_INDEX=8;
                        break;
                    case R.id.menu_1000:
                        tv_price_header.setText("Price (1000)");
                        SELECTED_PRICE_INDEX=9;
                        break;
                    case R.id.menu_2000:
                        tv_price_header.setText("Price(2000)");
                        SELECTED_PRICE_INDEX=10;
                        break;
                    case R.id.menu_3000:
                        tv_price_header.setText("Price(3000)");
                        SELECTED_PRICE_INDEX=11;
                        break;
                    case R.id.menu_5000:
                        tv_price_header.setText("Price(5000)");
                        SELECTED_PRICE_INDEX=12;
                        break;

                    case R.id.menu_auto:
                        SELECTED_PRICE_INDEX=13;
                        tv_price_header.setText("Price(auto)");
                        break;
                }

                calculate();
                return true;
            }
        });
        popup.show();
    }
}