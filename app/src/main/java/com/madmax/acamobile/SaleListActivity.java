package com.madmax.acamobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.madmax.acamobile.adapters.MyOrderAdapter;
import com.madmax.acamobile.adapters.SaleAdapter;
import com.madmax.acamobile.app.AuthChecker;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.models.VoucherModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

public class SaleListActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ProgressBar pb;
    TextView tv_msg;

    ArrayList<VoucherModel> vouchers=new ArrayList<>();
    SaleAdapter adapter;

    int page=1;
    private boolean loading=true;
    int visibleItemCount,totalItemCount;
    public static int pastVisibleItems;

    SharedPreferences sharedPreferences;
    Executor postExecutor;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale_list);

        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Sales");

        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=sharedPreferences.getString("userId",null);
        postExecutor= ContextCompat.getMainExecutor(this);

        setUpView();
    }
    @Override
    protected void onResume() {
        page=1;
        vouchers.clear();
        fetchSales(page);
        super.onResume();
    }

    private void setUpView(){
        recyclerView=findViewById(R.id.recyclerView);
        pb=findViewById(R.id.pb);
        tv_msg=findViewById(R.id.tv_msg);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter=new SaleAdapter(this,vouchers);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                pastVisibleItems=lm.findFirstVisibleItemPosition();

                if(dy>0){
                    visibleItemCount=lm.getChildCount();
                    totalItemCount=lm.getItemCount();

                    if(loading){
                        if((visibleItemCount+pastVisibleItems)>=totalItemCount-7){
                            loading=false;
                            page++;
                            fetchSales(page);

                        }
                    }
                }
            }
        });

    }

    private void fetchSales(int page){
        pb.setVisibility(View.VISIBLE);
        if(userId!=null){
            new Thread(() -> {
                MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                    @Override
                    public void onResponse(String response) {
                        postExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                pb.setVisibility(View.GONE);
                                setResult(response);

                                if (vouchers.size()>0){
                                    tv_msg.setVisibility(View.GONE);
                                }else{
                                    tv_msg.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                    @Override
                    public void onError(String msg) {
                        Log.e("ErrOrders ",msg);
                        pb.setVisibility(View.GONE);

                    }
                }).url(Routing.GET_SALE+"?user_id="+userId+"&page="+page);

                myHttp.runTask();
            }).start();
        }else{
            new AuthChecker(this).logout();
        }
    }

    private void setResult(String response){
        try {

            JSONArray ja=new JSONObject(response).getJSONArray("sales");
            for (int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                long voucher_id=jo.getLong("voucher_id");
                double total_amount=jo.getDouble("total_amount");
                String customer_name=jo.getString("customer_name");
                boolean agent=jo.getInt("is_agent")==1;

                vouchers.add(new VoucherModel(voucher_id,total_amount,customer_name,agent));

            }
            adapter.notifyDataSetChanged();
            loading=true;

        }catch (Exception e){}
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}