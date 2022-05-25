package com.madmax.acamobile;

import static com.madmax.acamobile.VoucherActivity.voucherOrders;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.madmax.acamobile.adapters.InvoiceProductAdapter;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.models.OrderModel;
import com.madmax.acamobile.models.VoucherModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.Executor;

public class InvoiceActivity extends AppCompatActivity {

    float total_point=0;
    double total_amount=0;
    int total_foc=0;
    int total_quantity=0;
    String voucher_type;
    String customer_phone,customer_address,customer_name,logo_extension;
    String stock_id,stock_name;

    TextView tv_total_amount,tv_total_foc,tv_total_quantity;
    TextView tv_customer_name,tv_customer_phone,tv_customer_address;
    TextView tv_company_name,tv_company_phone,tv_company_address;
    TextView tv_voucher_type,tv_voucher_id,tv_voucher_stock,tv_voucher_date,tv_total_point;
    FrameLayout invoiceLayout;

    RecyclerView recyclerView;

    InvoiceProductAdapter adapter;

    SharedPreferences sharedPreferences;
    String invoice_name,invoice_phone,invoice_address;

    ImageView iv_logo;

    long voucher_id;

    ProgressBar pb;
    Executor postExecutor;
    String currentUserId,authToken;
    boolean sale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);
        postExecutor=ContextCompat.getMainExecutor(this);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        invoice_address=sharedPreferences.getString("invoice_address","No Address");
        invoice_name=sharedPreferences.getString("invoice_name","No Name");
        invoice_phone=sharedPreferences.getString("invoice_phone","No Phone");
        logo_extension=sharedPreferences.getString("logo_extension","");
        currentUserId=sharedPreferences.getString("userId",null);
        authToken=sharedPreferences.getString("authToken",null);

        total_point=getIntent().getExtras().getFloat("total_point",0);
        total_amount=getIntent().getExtras().getDouble("total_amount",0);
        total_foc=getIntent().getExtras().getInt("total_foc",0);
        total_quantity=getIntent().getExtras().getInt("total_quantity",0);
        voucher_type=getIntent().getExtras().getString("type","");
        customer_name=getIntent().getExtras().getString("customer_name","");
        customer_phone=getIntent().getExtras().getString("customer_phone","");
        customer_address=getIntent().getExtras().getString("customer_address","");
        sale=getIntent().getExtras().getBoolean("sale",false);

        stock_name=getIntent().getExtras().getString("stock_name","");
        stock_id=getIntent().getExtras().getString("stock_id","");

        if(sale){
            voucher_id=getIntent().getExtras().getLong("voucher_id",0);
            setTitle("Details");
        } else {
            voucher_id=System.currentTimeMillis()/1000;
            setTitle("Invoice");
        }


        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setUpView();
    }

    private void setUpView(){
        invoiceLayout=findViewById(R.id.invoiceLayout);
        tv_total_amount=findViewById(R.id.tv_total_amount);
        tv_total_foc=findViewById(R.id.tv_total_foc);
        tv_total_quantity=findViewById(R.id.tv_total_quantity);
        recyclerView=findViewById(R.id.recyclerView);
        tv_customer_address=findViewById(R.id.tv_customer_address);
        tv_customer_name=findViewById(R.id.tv_customer_name);
        tv_customer_phone=findViewById(R.id.tv_customer_phone);
        tv_voucher_type=findViewById(R.id.tv_voucher_type);
        tv_voucher_id=findViewById(R.id.tv_voucher_id);
        tv_voucher_stock=findViewById(R.id.tv_voucher_stock);
        tv_voucher_date=findViewById(R.id.tv_voucher_date);
        tv_total_point=findViewById(R.id.tv_total_point);

        tv_company_address=findViewById(R.id.tv_company_address);
        tv_company_name=findViewById(R.id.tv_company_name);
        tv_company_phone=findViewById(R.id.tv_company_phone);
        iv_logo=findViewById(R.id.iv_logo);
        pb=findViewById(R.id.pb);

        LinearLayoutManager lm=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lm);
        adapter=new InvoiceProductAdapter(this, voucherOrders);
        recyclerView.setAdapter(adapter);


        tv_total_amount.setText(AppUtils.getTwoDecimalDouble(total_amount));
        tv_voucher_id.setText("ID - "+voucher_id);
        tv_company_phone.setText(invoice_phone);
        tv_company_name.setText(invoice_name);
        tv_company_address.setText(invoice_address);


        if(sale){
            getSaleDetail();
        }else {
            tv_total_foc.setText(""+total_foc);
            tv_total_quantity.setText(""+total_quantity);
            tv_voucher_type.setText("Type - "+voucher_type);
            tv_customer_name.setText(customer_name);
            tv_customer_phone.setText(customer_phone);
            tv_customer_address.setText(customer_address);

            tv_voucher_stock.setText("Stock - "+stock_name);
            tv_voucher_date.setText("Date - "+getDate(System.currentTimeMillis()));
            tv_total_point.setText("Total rewarded point - "+total_point);
        }


        setLogo();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("EDIT")
                .setIcon(R.drawable.ic_baseline_edit_24)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add("SAVE")
                .setIcon(R.drawable.ic_baseline_save_alt_24)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        if(!sale)  menu.add("DONE")
                .setIcon(R.drawable.ic_baseline_check_24)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }else{
            if(item.getTitle().equals("EDIT")){
                Intent intent=new Intent(InvoiceActivity.this,InvoiceEditActivity.class);
                startActivity(intent);
            }else if(item.getTitle().equals("DONE")){
                saveOnCloud();
            }else if(item.getTitle().equals("SAVE")){

                Bitmap imageBitmap=convertViewToBitmap(invoiceLayout);
                if(isPermissionGranted()){
                    if(imageBitmap!=null) {

                        try {
                            saveImage(imageBitmap,"my_voucher_"+System.currentTimeMillis());
                        } catch (IOException e) {

                        }
                    }else{
                        Toast.makeText(getApplicationContext(),"Can't save.Please try again.",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    takePermission();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private String getDate( long time){
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy");
        Date resultdate = new Date(time);

        return sdf.format(resultdate);

    }

    private void setLogo(){
        Bitmap bitmap;
        byte[] art=getFileByte("invoice_logo."+logo_extension,getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath());
        if(art!=null){
            bitmap = BitmapFactory.decodeByteArray(art,0,art.length);
            iv_logo.setImageBitmap(bitmap);
        }
    }

     private byte[] getFileByte(String title, String dir){
        byte [] buffer;

        try {
            InputStream is=new BufferedInputStream(new FileInputStream(dir+"/"+title));
            int size=is.available();
            buffer=new byte[size];
            is.read(buffer);
            is.close();

            return  buffer;

        }catch (Exception e){
            return null;
        }
    }


    public Bitmap convertViewToBitmap(FrameLayout view) {

        view.setDrawingCacheEnabled(true);

        view.buildDrawingCache();

        return view.getDrawingCache();
    }

    private boolean isPermissionGranted(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return  true;
        }else{
            int  writeExternalStorage= ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return  writeExternalStorage== PackageManager.PERMISSION_GRANTED;
        }

    }

    private void takePermission(){
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0){
            if(requestCode==101){
                boolean writeExternalStorage=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                if(writeExternalStorage){
                    Bitmap imageBitmap=convertViewToBitmap(invoiceLayout);
                    if(imageBitmap!=null) {
                        try {
                            saveImage(imageBitmap,"Easy_English_"+System.currentTimeMillis());
                        } catch (IOException e) {
                            Log.e("photoSave ",e.toString());
                        }
                    }else{
                        Toast.makeText(getApplicationContext(),"Can't save.Please try again.",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    takePermission();
                }
            }
        }
    }

    private void saveImage(Bitmap bitmap, @NonNull String name) throws IOException {
        OutputStream fos;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            ContentResolver resolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, name + ".jpg");
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

            Uri imageUri = resolver.insert(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues);

            try {
                ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(imageUri, "w",null);
                fos = new FileOutputStream(pfd.getFileDescriptor());
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                Objects.requireNonNull(fos).close();
                pfd.close();
            }catch (Exception e){
                Log.e("photoErr ",e.toString());
                Toast.makeText(getApplicationContext(),"Can't save.Please try again.",Toast.LENGTH_SHORT).show();
            }

        } else {
            String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
            File image = new File(imagesDir, name + ".jpg");
            fos = new FileOutputStream(image);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Objects.requireNonNull(fos).close();
        }

        Toast.makeText(getApplicationContext(),"Saved",Toast.LENGTH_SHORT).show();
    }

    private void saveOnCloud(){
        pb.setVisibility(View.VISIBLE);
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {

                    Log.e("Sale REs ",response);
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
                    Log.e("saleErr ",msg);
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            pb.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(),"Fail! Please try again later",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).url(Routing.ADD_SALE)
                    .field("admin_id",currentUserId)
                    .field("auth_token",authToken)
                    .field("total_amount",total_amount+"")
                    .field("voucher_id",voucher_id+"")
                    .field("stock_id",stock_id)
                    .field("is_agent",voucher_type.equals("Agent")?"1":"0")
                    .field("customer_name",customer_name)
                    .field("customer_phone",customer_phone)
                    .field("customer_address",customer_address)
                    .field("productJSON",makeOrderJSON());

            myHttp.runTask();
        }).start();
    }



    private String makeOrderJSON(){
        JSONArray orderJSONArr=new JSONArray();
        for (OrderModel order : voucherOrders) {
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

    private void getSaleDetail(){
        pb.setVisibility(View.VISIBLE);
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {

                    Log.e("Sale REs ",response);
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                           setUpSaleDetail(response);
                           pb.setVisibility(View.GONE);
                        }
                    });


                }
                @Override
                public void onError(String msg) {
                    Log.e("saleErr ",msg);
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            pb.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(),"Fail! Please try again later",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).url(Routing.GET_SALE_DETAIL+"?user_id="+currentUserId+"&voucher_id="+voucher_id);
            myHttp.runTask();
        }).start();
    }

    private void setUpSaleDetail(String response){
        voucherOrders.clear();

        try {
            JSONObject joMain=new JSONObject(response);
//            JSONObject joVoucher=joMain.getJSONObject("voucher");

            stock_name=joMain.getString("stock_name");  // use this

            JSONObject joSale=joMain.getJSONObject("sale");
            boolean agent=joSale.getInt("is_agent")==1;
            voucher_type=agent?"Agent":"Retail";
            customer_address=joSale.getString("customer_address");
            customer_phone=joSale.getString("customer_phone");
            customer_name=joSale.getString("customer_name");

            int total_foc=0;
            int total_quantity=0;
            float total_point=0;

            JSONArray ja=joMain.getJSONArray("details");
            for(int i=0;i<ja.length();i++){
                JSONObject joDetail=ja.getJSONObject(i);
                String productName=joDetail.getString("product_name");
                float price=(float) joDetail.getDouble("price");
                double amount=joDetail.getDouble("amount");
                float point=(float) joDetail.getDouble("point");
                int productId=joDetail.getInt("product_id");
                int discount=joDetail.getInt("discount");
                int quantity=joDetail.getInt("quantity");
                int foc=joDetail.getInt("foc");

                total_foc+=foc;
                total_quantity+=quantity;
                total_point+=point;

                OrderModel order=new OrderModel();
                order.setProductName(productName);
                order.setPrice(price);
                order.setAmount(amount);
                order.setPoint(point);
                order.setProduct_id(productId);
                order.setDiscount(discount);
                order.setQuantity(quantity);
                order.setFoc(foc);
                voucherOrders.add(order);
            }

            adapter.notifyDataSetChanged();



            tv_total_foc.setText(""+total_foc);
            tv_total_quantity.setText(""+total_quantity);
            tv_voucher_type.setText("Type - "+voucher_type);
            tv_customer_name.setText(customer_name);
            tv_customer_phone.setText(customer_phone);
            tv_customer_address.setText(customer_address);

            tv_voucher_stock.setText("Stock - "+stock_name);
            tv_voucher_date.setText("Date - "+getDate(voucher_id*1000));
            tv_total_point.setText("Total rewarded point - "+total_point);


        }catch (Exception e){
            Log.e("saleDetailErrr ",e.toString());
        }
    }

    @Override
    public void onBackPressed() {
        voucherOrders.clear();
        super.onBackPressed();
    }
}