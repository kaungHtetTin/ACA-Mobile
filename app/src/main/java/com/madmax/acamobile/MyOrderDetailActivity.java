package com.madmax.acamobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.madmax.acamobile.adapters.MyOrderDetailProductAdapter;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.models.OrderModel;
import com.madmax.acamobile.models.ProductModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

public class MyOrderDetailActivity extends AppCompatActivity {


    ProgressBar pb;
    LinearLayout mLayout;
    //footer
    TextView tv_quantity_total,tv_foc_total,tv_amount_total,tv_point_total;
    TextView tv_date,tv_voucher_id,tv_group_name,tv_status,tv_price_edit;
    //main layer
    TextView tv_total_amount,tv_total_point,tv_total_extra_cost;
    Button bt_cancel,bt_received,bt_call;

    RecyclerView recyclerView;
    ArrayList<OrderModel> orders=new ArrayList<>();
    MyOrderDetailProductAdapter adapter;
    Executor postExecutor;

    String voucher_id;
    String admin_id=null;
    String admin_phone=null;
    String admin_fcmToken=null;
    String currentUserId,authToken;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_order_detail);

        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserId=sharedPreferences.getString("userId",null);
        authToken=sharedPreferences.getString("authToken",null);

        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        voucher_id=getIntent().getExtras().getString("voucher_id");

        setTitle("Details");
        postExecutor= ContextCompat.getMainExecutor(this);
        setUpView();

    }

    @Override
    protected void onResume() {
        orders.clear();
        pb.setVisibility(View.VISIBLE);
        mLayout.setVisibility(View.GONE);
        fetchDetail();
        super.onResume();
    }

    private void setUpView(){

        pb=findViewById(R.id.pb);
        mLayout=findViewById(R.id.order_layout);
        recyclerView=findViewById(R.id.recyclerView);
        LinearLayoutManager lm=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lm);
        tv_amount_total=findViewById(R.id.tv_amount_total);
        tv_foc_total=findViewById(R.id.tv_foc_total);
        tv_point_total=findViewById(R.id.tv_point_total);
        tv_quantity_total=findViewById(R.id.tv_quantity_total);
        tv_status=findViewById(R.id.tv_is_agent);
        tv_total_amount=findViewById(R.id.tv_total_amount);
        tv_total_point=findViewById(R.id.tv_total_point);
        tv_voucher_id=findViewById(R.id.tv_name);
        tv_voucher_id.setText("VoucherId - "+voucher_id);
        tv_total_extra_cost=findViewById(R.id.tv_total_extra_cost);
        tv_date=findViewById(R.id.tv_date);
        tv_group_name=findViewById(R.id.tv_address);
        tv_price_edit=findViewById(R.id.tv_price_edit);
        bt_cancel=findViewById(R.id.bt_cancel);
        bt_received=findViewById(R.id.bt_received);
        bt_call=findViewById(R.id.bt_call);

        tv_date.setText(AppUtils.formatTime(Long.parseLong(voucher_id)*1000));


        tv_total_extra_cost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MyOrderDetailActivity.this,DataUpdateActivity.class);
                intent.putExtra("hint","Enter Extra Cost");
                intent.putExtra("message","Update your extra cost for order of Voucher ID - "+voucher_id);
                intent.putExtra("key","agent_extra_cost");
                intent.putExtra("contentId",voucher_id+"");
                intent.putExtra("url", Routing.UPDATE_EXTRA_COST);
                startActivity(intent);
            }
        });

        bt_received.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bt_received.setEnabled(false);
                receivedOrCancelOrder(Routing.RECEIVED_ORDER);
            }
        });

        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bt_cancel.setEnabled(false);
                receivedOrCancelOrder(Routing.CANCEL_ORDER);
            }
        });
    }

    private void fetchDetail(){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            setResult(response);
                            pb.setVisibility(View.GONE);
                            mLayout.setVisibility(View.VISIBLE);
                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    Log.e("ErrOrders ",msg);
                    pb.setVisibility(View.GONE);

                }
            }).url(Routing.GET_ORDER_DETAILS+"?voucher_id="+voucher_id+"&user_id="+currentUserId);

            myHttp.runTask();
        }).start();
    }

    private void setResult(String response){
        try {
            JSONObject jo=new JSONObject(response);

            if(jo.has("details")){
                JSONArray jaDetail=jo.getJSONArray("details");
                int total_quantity=0;
                int price_count_quantity=0;
                float total_point=0;
                double total_amount=0;
                int total_foc=0;

                for(int i=0;i<jaDetail.length();i++){
                    JSONObject joProduct=jaDetail.getJSONObject(i);
                    int product_id=joProduct.getInt("product_id");
                    int quantity=joProduct.getInt("quantity");
                    int foc=  joProduct.getInt("foc");

                    //double amount=joProduct.getDouble("amount");

                    float price=(float) joProduct.getDouble("price");
                    int discount=joProduct.getInt("discount");
                    float point=(float) joProduct.getDouble("point");

                    double amount=quantity*price;

                    OrderModel o=new OrderModel(product_id,price,discount,point,quantity,amount,foc);
                    o.setProfit(0);
                    orders.add(o);

                    total_quantity+=quantity;
                    ProductModel p=AppUtils.findProductById(product_id);
                    if(p!=null){
                        int temp=quantity/p.getPack();
                        price_count_quantity+=temp;
                    }
                    total_point+=point;
                    total_amount+=amount;
                    total_foc+=foc;
                }

                tv_quantity_total.setText(price_count_quantity+" / "+total_quantity);
                tv_point_total.setText(total_point+"");
                tv_total_point.setText(total_point+"");
                tv_foc_total.setText(total_foc+"");
                String totalAmount=AppUtils.getTwoDecimalDouble(total_amount);
                tv_amount_total.setText(totalAmount);
                tv_total_amount.setText(totalAmount);
            }

            if(jo.has("admin")){
                JSONObject joAdmin=jo.getJSONObject("admin");
                admin_phone=joAdmin.getString("phone");

                bt_call.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if(isPermissionGranted()){
                            callPhone(admin_phone);
                        }else{
                            takePermission();
                        }
                    }
                });
            }

            if(jo.has("order")){
                JSONObject joOrder=jo.getJSONObject("order");
                boolean isSoldOut=joOrder.getInt("is_sold_out")==1;
                boolean isReceived=joOrder.getInt("is_received")==1;
                boolean seen=joOrder.getInt("seen")==1;
                boolean price_edit=joOrder.getInt("price_edit")==1;
                String extraCost=joOrder.getString("agent_extra_cost");
                tv_total_extra_cost.setText(extraCost);

                if(price_edit)tv_price_edit.setVisibility(View.VISIBLE);

                if(isSoldOut){
                    bt_cancel.setVisibility(View.GONE);
                    bt_received.setVisibility(View.VISIBLE);
                    tv_status.setText("Delivered by Admin");

                }else{
                    bt_received.setVisibility(View.GONE);
                    bt_cancel.setVisibility(View.VISIBLE);

                    if(seen)  tv_status.setText("Seen by Admin");
                    else tv_status.setText("Sent to Admin");
                }

                if(isReceived){
                    bt_cancel.setVisibility(View.GONE);
                    bt_received.setVisibility(View.GONE);
                    tv_status.setText("Received");
                }

            }

            if(jo.has("group")){
                JSONObject joGroup=jo.getJSONObject("group");
                String groupName=joGroup.getString("group_name");
                tv_group_name.setText(groupName);
            }

            adapter=new MyOrderDetailProductAdapter(this,orders);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();


        } catch (JSONException e) {
            Log.e("OrderDetailJSONErr ",e.toString());
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void takePermission(){
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CALL_PHONE},101);
    }

    private boolean isPermissionGranted(){
        int  callPhone= ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
        return  callPhone== PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0){
            if(requestCode==101){
                boolean callPhone=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                if(callPhone){
                    callPhone(admin_phone);
                }else {
                    takePermission();
                }
            }
        }
    }

    private void callPhone(String phone){
        phone=phone.replace("#","%23");
        Intent intent=new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+phone));
        startActivity(intent);
    }

    private void receivedOrCancelOrder(String url){
        if (currentUserId != null && authToken != null) {
            pb.setVisibility(View.VISIBLE);
            new Thread(() -> {
                MyHttp myHttp = new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("soldOut ",response);
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
                                        Toast.makeText(getApplicationContext(), "Update fail! Try again.", Toast.LENGTH_SHORT).show();
                                    }

                                } catch (Exception e) {
                                    Log.e("UpdateResJSONErr ",e.toString());
                                }
                            }
                        });

                    }

                    @Override
                    public void onError(String msg) {
                        Log.e("UpdateDataFail ",msg);

                    }
                }).url(url)
                        .field("user_id", currentUserId)
                        .field("auth_token", authToken)
                        .field("voucher_id",voucher_id);
                myHttp.runTask();
            }).start();
        }
    }
}