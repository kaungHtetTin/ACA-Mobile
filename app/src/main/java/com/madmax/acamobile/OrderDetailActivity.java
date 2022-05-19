package com.madmax.acamobile;

import static com.madmax.acamobile.ProductLeftActivity.stocks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.madmax.acamobile.adapters.OrderDetailProductAdapter;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.models.OrderModel;
import com.madmax.acamobile.models.Product;
import com.madmax.acamobile.models.ProductModel;
import com.madmax.acamobile.models.StockModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

public class OrderDetailActivity extends AppCompatActivity {

    LinearLayout mLayout,agent_layout;
    ProgressBar pb;
    ImageView iv_profile;
    TextView tv_agent_name,tv_address,tv_phone;
    ImageButton ibt_call;

    //footer
    TextView tv_quantity_total,tv_foc_total,tv_amount_total,tv_point_total;
    TextView tv_left_header,tv_left_footer,tv_date;

    //main layer
    TextView tv_total_amount,tv_total_point,tv_voucher_id,tv_total_extra_cost,tv_order_by,
            tv_group_name,tv_order_detail,tv_transfer;
    Spinner sp_stock;
    Button bt_sent;
    RecyclerView recyclerView;
    ArrayList<OrderModel> orders=new ArrayList<>();

    ArrayList<String> stockNames=new ArrayList<>();
//    ArrayList<StockModel> stockArr=new ArrayList<>();
    int selectedStockId;
    OrderDetailProductAdapter adapter;

    Executor postExecutor;

    String voucher_id;

    String agent_id=null;
    String agent_phone=null;
    String agent_fcmToken=null;
    String currentUserId,authToken;
    SharedPreferences sharedPreferences;
    public static boolean isEnoughToSend=true;

    int initial_index=0,target_index=0;

    String stockJSON;
    boolean isSoldOut=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
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
        mLayout.setVisibility(View.GONE);
        orders.clear();
        stocks.clear();
        stockNames.clear();
        getProductLeftByStocks();
        super.onResume();
    }

    private void setUpView(){
        mLayout=findViewById(R.id.order_layout);
        pb=findViewById(R.id.pb);
        agent_layout=findViewById(R.id.agent_layout);
        iv_profile=findViewById(R.id.iv_profile);
        tv_agent_name=findViewById(R.id.tv_name);
        tv_address=findViewById(R.id.tv_address);
        tv_phone=findViewById(R.id.tv_phone);
        ibt_call=findViewById(R.id.ibt_call);
        recyclerView=findViewById(R.id.recyclerView);
        LinearLayoutManager lm=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lm);

        tv_amount_total=findViewById(R.id.tv_amount_total);
        tv_foc_total=findViewById(R.id.tv_foc_total);
        tv_point_total=findViewById(R.id.tv_point_total);
        tv_quantity_total=findViewById(R.id.tv_quantity_total);

        tv_total_amount=findViewById(R.id.tv_total_amount);
        tv_total_point=findViewById(R.id.tv_total_point);
        tv_voucher_id=findViewById(R.id.tv_voucher_id);
        tv_voucher_id.setText("VoucherId - "+voucher_id);
        tv_left_footer=findViewById(R.id.tv_left_footer);
        tv_left_header=findViewById(R.id.tv_left_header);
        sp_stock=findViewById(R.id.sp_stock);
        bt_sent=findViewById(R.id.bt_sent_order);
        tv_total_extra_cost=findViewById(R.id.tv_total_extra_cost);
        tv_date=findViewById(R.id.tv_date);
        tv_order_by=findViewById(R.id.tv_order_by);
        tv_group_name=findViewById(R.id.tv_group_name);
        tv_order_detail=findViewById(R.id.tv_order_detail);
        tv_transfer=findViewById(R.id.tv_transfer);

        tv_date.setText(AppUtils.formatTime(Long.parseLong(voucher_id)*1000));

        bt_sent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isEnoughToSend){
                    soldOutOrder();
                }else {
                    Toast.makeText(getApplicationContext(), "Not enough product in the stock", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tv_total_extra_cost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(OrderDetailActivity.this,DataUpdateActivity.class);
                intent.putExtra("hint","Enter Extra Cost");
                intent.putExtra("message","Update your extra cost for order of Voucher ID - "+voucher_id);
                intent.putExtra("key","admin_extra_cost");
                intent.putExtra("contentId",voucher_id+"");
                intent.putExtra("url", Routing.UPDATE_EXTRA_COST);
                startActivity(intent);
            }
        });

        tv_transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showStockSelectorDialog();
            }
        });
    }

    private void getProductLeftByStocks(){
        Thread t=new Thread(() -> {
            MyHttp myHttp = new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    stockJSON=response;
                    stocks.clear();
                    stockNames.clear();
                    fetchDetail();
                }
                @Override
                public void onError(String msg) {
                    Log.e("stockReqErrr ",msg);
                }
            }).url(Routing.PRODUCT_LEFT_BY_STOCK+"?owner_id="+currentUserId);
            myHttp.runTask();
        }
        );
        t.start();
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
               setOrderDetails(jo);
               setStock();
            }

            if(jo.has("order")){
               setOrderStatus(jo);
            }

            if(jo.has("agent")){
               setAgent(jo);
            }else{
                agent_layout.setVisibility(View.GONE);
                tv_order_by.setVisibility(View.GONE);
                tv_order_detail.setText("Retail Order");
            }

            if(jo.has("group")){
                JSONObject joGroup=jo.getJSONObject("group");
                String groupName=joGroup.getString("group_name");
                tv_group_name.setText(groupName);
            }

            adapter=new OrderDetailProductAdapter(this,orders,stocks.get(0),isSoldOut);
            selectedStockId=stocks.get(0).getStock_id();
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();


        } catch (JSONException e) {
            Log.e("OrderDetailJSONErr ",e.toString());
        }
    }


    private void setOrderDetails (JSONObject jo) throws JSONException{
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
            double amount=joProduct.getDouble("amount");
            float price=(float) joProduct.getDouble("price");
            int discount=joProduct.getInt("discount");
            float point=(float) joProduct.getDouble("point");
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

   private void setStock() throws JSONException{
       JSONArray ja=new JSONArray(stockJSON);
       for(int i=0;i<ja.length();i++){
           JSONObject joStock=ja.getJSONObject(i);
           int stock_id=joStock.getInt("stock_id");
           String name=joStock.getString("name");
           String items=joStock.getString("items");
           JSONArray ja1=new JSONArray(items);
           ArrayList<Product> products=new ArrayList<>();
           for(int j=0;j<ja1.length();j++) {
               JSONObject jo1 = ja1.getJSONObject(j);
               int product_id=jo1.getInt("product_id");
               String product_name = jo1.getString("product_name");
               int count=jo1.getInt("count");
               for(OrderModel o:orders){
                   if(o.getProduct_id()==product_id) products.add(new Product(product_id,product_name,count));
               }

           }
           stocks.add(new StockModel(stock_id,name,products));
           stockNames.add(name);
       }
   }


   private void setOrderStatus(JSONObject jo) throws JSONException{
       JSONObject joOrder=jo.getJSONObject("order");
       isSoldOut=joOrder.getInt("is_sold_out")==1;
       String extraCost=joOrder.getString("admin_extra_cost");
       tv_total_extra_cost.setText(extraCost);
       if(isSoldOut){
           sp_stock.setVisibility(View.GONE);
           tv_left_header.setVisibility(View.GONE);
           tv_left_footer.setVisibility(View.GONE);
           bt_sent.setEnabled(false);
       }else{
           setStockSpinner();
       }
   }

   private void setAgent(JSONObject jo) throws JSONException{
       JSONObject joAgent=jo.getJSONObject("agent");
       String name=joAgent.getString("name");
       String profile_image=joAgent.getString("profile_image");
       String address=joAgent.getString("address");
       agent_phone=joAgent.getString("phone");
       agent_fcmToken=joAgent.getString("fcm_token");
       agent_id=joAgent.getString("user_id");
       tv_address.setText(address);
       tv_phone.setText(agent_phone);
       tv_agent_name.setText(name);
       if(!profile_image.equals("")) AppUtils.setPhotoFromRealUrl(iv_profile,Routing.PROFILE_URL+profile_image);

       ibt_call.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               if(isPermissionGranted()){
                   callPhone(agent_phone);
               }else{
                   takePermission();
               }
           }
       });
   }

    private void setStockSpinner(){
        ArrayAdapter<String> stockAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,stockNames);
        sp_stock.setAdapter(stockAdapter);
        sp_stock.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                isEnoughToSend=true;
                StockModel model=stocks.get(position);
                selectedStockId=model.getStock_id();
                adapter.setStock(model);
                adapter.notifyItemRangeChanged(0,orders.size());

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
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
                    callPhone(agent_phone);
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void soldOutOrder(){
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
                }).url(Routing.SOLD_OUT_ORDER)
                        .field("user_id", currentUserId)
                        .field("auth_token", authToken)
                        .field("stock_id",selectedStockId+"")
                        .field("voucher_id",voucher_id);
                myHttp.runTask();
            }).start();
        }
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
                initial_index=position;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        sp_target.setAdapter(stockAdapter);
        sp_target.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                target_index=position;
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

                    Intent intent=new Intent(OrderDetailActivity.this,ProductTransferActivity.class);
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
