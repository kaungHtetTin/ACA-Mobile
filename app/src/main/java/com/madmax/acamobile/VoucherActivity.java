package com.madmax.acamobile;
import static com.madmax.acamobile.ProductLeftActivity.stocks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.madmax.acamobile.adapters.VoucherProductAdapter;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.app.Initializer;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Price;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.models.OrderModel;
import com.madmax.acamobile.models.PriceModel;
import com.madmax.acamobile.models.Product;
import com.madmax.acamobile.models.ProductModel;
import com.madmax.acamobile.models.StockModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

public class VoucherActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    Spinner sp_stock;
    TextView tv_transfer;
    Button bt_create;
    RadioGroup rg;
    EditText et_customer_name,et_customer_phone,et_customer_address;

    ArrayList<ProductModel> voucherProducts=new ArrayList<>();
    ArrayList<String> stockNames=new ArrayList<>();
    public static ArrayList<OrderModel> voucherOrders=new ArrayList<>();
    VoucherProductAdapter adapter;

    String currentUserId,authToken;
    SharedPreferences sharedPreferences;
    Executor postExecutor;
    int selectedStockId;
    String selectedStockName;
    int initial_index=0,target_index=0;

    int brand_id;


    String customer_name;
    String customer_address;
    String customer_phone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voucher);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserId=sharedPreferences.getString("userId",null);
        authToken=sharedPreferences.getString("authToken",null);
        postExecutor= ContextCompat.getMainExecutor(this);

        brand_id=getIntent().getExtras().getInt("brand_id");

        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Create Invoice");

        setUpView();
        voucherOrders.clear();

    }

    private void setUpView(){
        recyclerView=findViewById(R.id.recyclerView);
        sp_stock=findViewById(R.id.sp_stock);
        tv_transfer=findViewById(R.id.tv_transfer);
        bt_create=findViewById(R.id.bt_create);
        rg=findViewById(R.id.rg_type);
        et_customer_address=findViewById(R.id.et_customer_address);
        et_customer_name=findViewById(R.id.et_customer_name);
        et_customer_phone=findViewById(R.id.et_customer_phone);

        LinearLayoutManager lm=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lm);

        setProductList();

        tv_transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showStockSelectorDialog();
            }
        });

        bt_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createInvoice();

            }
        });

        getProductLeftByStocks();

    }

    @Override
    public void onBackPressed() {
        voucherOrders.clear();
        voucherProducts.clear();
        super.onBackPressed();
    }

    @Override
    protected void onResume() {

        super.onResume();
    }

    private void setProductList(){
        for(int i=0; i<Initializer.products.size();i++){
            ProductModel p=Initializer.products.get(i);
            if(p.getBrand_id()==brand_id){
                voucherProducts.add(p);
            }
        }
    }

    private void createInvoice(){
        ArrayList<ProductModel> temp=adapter.getSelectedProducts();
        customer_name=et_customer_name.getText().toString();
        customer_address=et_customer_address.getText().toString();
        customer_phone=et_customer_phone.getText().toString();
        if(!OrderDetailActivity.isEnoughToSend){
            Toast.makeText(getApplicationContext(),"Not enough item quantity in selected stock",Toast.LENGTH_SHORT).show();
            return;
        }
        if(temp.size()<=0){
            Toast.makeText(getApplicationContext(),"Enter required item quantity",Toast.LENGTH_SHORT).show();
            return;
        }

        if(customer_name.equals("")){
            Toast.makeText(getApplicationContext(),"Enter customer name",Toast.LENGTH_SHORT).show();
            return;
        }

        if(customer_phone.equals("")){
            Toast.makeText(getApplicationContext(),"Enter customer phone",Toast.LENGTH_SHORT).show();
            return;
        }

        if(customer_address.equals("")){
            Toast.makeText(getApplicationContext(),"Enter customer address",Toast.LENGTH_SHORT).show();
            return;
        }

        calculateOrder(temp,rg.getCheckedRadioButtonId()==R.id.rb_retail);
        temp.clear();
    }

    public void calculateOrder(ArrayList<ProductModel> data,boolean isRetail) {

        float total_point=0;
        double total_amount=0;
        int total_foc=0;
        int total_quantity=0;


        voucherOrders.clear();

        for(int i=0;i<data.size();i++){
            ProductModel model=data.get(i);

            int quantity=model.getQuantity(); // quantity
            float price;
            if(isRetail){
                price=model.getPrice(Price.retail_price);
            }else{
                price=model.getSelectedPrice();
            }



//            int discount=(int)model.getDiscount();
//            float discount_save=(float)(price/100.0)*discount;
//            price=price-discount_save;                              // selected Price

            // calculate amount
            int tempQty=quantity/model.getPack();
            int pack=quantity%model.getPack();
            float pack_price=pack*model.getPack_price();
            double amount=tempQty*price+pack_price;

            float point=model.getPoint()*tempQty;

            int foc=model.getFoc();

            String productName=data.get(i).getProduct_name();

            OrderModel order=new OrderModel();
            order.setProductName(productName);
            order.setPrice(price);
            order.setAmount(amount);
            order.setPoint(point);
            order.setProduct_id(model.getProduct_id());
            order.setDiscount((int)model.getDiscount());
            order.setQuantity(quantity);
            order.setFoc(foc);
            voucherOrders.add(order);

            total_point+=point;
            total_foc+=foc;
            total_amount+=amount;
            total_quantity+=quantity;
        }

        Intent intent=new Intent(VoucherActivity.this,InvoiceActivity.class);
        intent.putExtra("total_point",total_point);
        intent.putExtra("total_foc",total_foc);
        intent.putExtra("total_amount",total_amount);
        intent.putExtra("total_quantity",total_quantity);
        intent.putExtra("type",isRetail?"Retail":"Agent");
        intent.putExtra("customer_name",customer_name);
        intent.putExtra("customer_phone",customer_phone);
        intent.putExtra("customer_address",customer_address);
        intent.putExtra("stock_id",selectedStockId+"");
        intent.putExtra("stock_name",selectedStockName);
        startActivity(intent);

    }
    private void getProductLeftByStocks(){
        Thread t=new Thread(() -> {
            MyHttp myHttp = new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {

                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            stocks.clear();
                            stockNames.clear();
                            try {
                                setStock(response);
                            }catch (Exception e){}
                            setStockSpinner();
                        }
                    });

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

    private void setStock(String stockJSON) throws JSONException {
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
                products.add(new Product(product_id,product_name,count));

            }
            stocks.add(new StockModel(stock_id,name,products));
            stockNames.add(name);
        }

        selectedStockId=stocks.get(0).getStock_id();
        selectedStockName=stocks.get(0).getName();
        adapter=new VoucherProductAdapter(this,voucherProducts,stocks.get(0));
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();


    }

    private void setStockSpinner(){
        ArrayAdapter<String> stockAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,stockNames);
        sp_stock.setAdapter(stockAdapter);
        sp_stock.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                OrderDetailActivity.isEnoughToSend=true;
                voucherOrders.clear();
                voucherProducts.clear();
                setProductList();
                StockModel model=stocks.get(position);
                selectedStockId=model.getStock_id();
                selectedStockName=model.getName();
                adapter.setStock(model);
                adapter.notifyDataSetChanged();

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
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

                    Intent intent=new Intent(VoucherActivity.this,ProductTransferActivity.class);
                    intent.putExtra("initial_index",initial_index);
                    intent.putExtra("target_index",target_index);
                    startActivity(intent);
                    ad.dismiss();
                }
            }
        });

        ad.show();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


}