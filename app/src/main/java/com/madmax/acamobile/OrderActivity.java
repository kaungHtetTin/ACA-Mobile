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
import com.madmax.acamobile.adapters.OrderAdapter;
import com.madmax.acamobile.app.AuthChecker;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.models.VoucherModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

public class OrderActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ProgressBar pb;
    TextView tv_msg;

    ArrayList<VoucherModel> vouchers=new ArrayList<>();
    OrderAdapter adapter;

    int page=1;
    private boolean loading=true;
    int visibleItemCount,totalItemCount;
    public static int pastVisibleItems;

    SharedPreferences sharedPreferences;
    Executor postExecutor;
    String userId,agent_id,group_id;
    int isSoldOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Orders");

        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=sharedPreferences.getString("userId",null);
        postExecutor= ContextCompat.getMainExecutor(this);
        isSoldOut=getIntent().getExtras().getInt("isSoldOut");
        agent_id=getIntent().getExtras().getString("agent_id","0");
        group_id=getIntent().getExtras().getString("group_id","0");

        setUpView();
        if(!agent_id.equals("0")){
            fetchOrdersByAgent(page);
            Log.e("selector ","agent");
        }else{
            fetchOrders(page);
            Log.e("selector ","not agent");
        }
    }


    private void setUpView(){
        recyclerView=findViewById(R.id.recyclerView);
        pb=findViewById(R.id.pb);
        tv_msg=findViewById(R.id.tv_msg);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter=new OrderAdapter(this,vouchers);
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
                            if(agent_id.equals("0")){
                                fetchOrdersByAgent(page);
                            }else{
                                fetchOrders(page);
                            }

                        }
                    }
                }
            }
        });

    }

    private void fetchOrders(int page){
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
                }).url(Routing.GET_ORDERS+"?user_id="+userId+"&is_sold_out="+isSoldOut+"&page="+page);

                myHttp.runTask();
            }).start();
        }else{
            new AuthChecker(this).logout();
        }
    }

    private void fetchOrdersByAgent(int page){
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
                                Log.e("AgentRes ",response);

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
                }).url(Routing.GET_ORDER_BY_AGENT+"?user_id="+userId+"&is_sold_out="+isSoldOut+"&page="+page+"&agent_id="+agent_id+"&group_id="+group_id);

                myHttp.runTask();
            }).start();
        }else{
            new AuthChecker(this).logout();
        }
    }


    private void setResult(String response){
        try {

            JSONArray ja=new JSONObject(response).getJSONArray("orders");
            for (int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                long voucher_id=jo.getLong("voucher_id");
                double total_amount=jo.getDouble("total_amount");
                String group_name=jo.getString("group_name");
                String group_image=jo.getString("group_image");
                boolean seen=jo.getInt("seen")==1;
                boolean received=jo.getInt("is_received")==1;
                boolean is_sold_out=jo.getInt("is_sold_out")==1;
                vouchers.add(new VoucherModel(voucher_id,total_amount,group_name,Routing.GROUP_COVER_URL+group_image,seen,received,is_sold_out));
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