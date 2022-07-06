package com.madmax.acamobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.madmax.acamobile.adapters.CustomerAdapter;
import com.madmax.acamobile.app.AuthChecker;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.models.CustomerModel;
import com.madmax.acamobile.models.VoucherModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

public class CustomersActivity extends AppCompatActivity {


    RecyclerView recyclerView;
    ProgressBar pb;
    TextView tv_msg;

    SharedPreferences sharedPreferences;
    String userId;

    CustomerAdapter adapter;
    ArrayList<CustomerModel> customers=new ArrayList<>();


    int page=1;
    private boolean loading=true;
    int visibleItemCount,totalItemCount;
    public static int pastVisibleItems;
    Executor postExecutor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customers);

        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Customers");

        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=sharedPreferences.getString("userId",null);
        postExecutor= ContextCompat.getMainExecutor(this);

        setUpView();
    }

    @Override
    protected void onResume() {
        page=1;
        customers.clear();
        fetchCustomers(page);
        adapter.notifyDataSetChanged();
        super.onResume();
    }

    private void setUpView(){
        recyclerView=findViewById(R.id.recyclerView);
        pb=findViewById(R.id.pb);
        tv_msg=findViewById(R.id.tv_msg);

        LinearLayoutManager lm=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lm);
        adapter=new CustomerAdapter(this,customers);
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
                            fetchCustomers(page);

                        }
                    }
                }
            }
        });
    }

    private void fetchCustomers(int page){
        pb.setVisibility(View.VISIBLE);
        if(userId!=null){
            new Thread(() -> {
                MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("Customers ",response);
                        postExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                pb.setVisibility(View.GONE);
                                setResult(response);

                                if (customers.size()>0){
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
                }).url(Routing.GET_CUSTOMERS+"?user_id="+userId+"&page="+page);

                myHttp.runTask();
            }).start();
        }else{
            new AuthChecker(this).logout();
        }
    }


    private void setResult(String response){
        try {

            JSONArray ja=new JSONArray(response);
            for (int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                String customer_name=jo.getString("customer_name");
                String phone=jo.getString("customer_phone");
                String address=jo.getString("customer_address");
                boolean agent=jo.getInt("is_agent")==1;
                customers.add(new CustomerModel(customer_name,phone,address,agent));

            }
            adapter.notifyDataSetChanged();
            loading=true;

        }catch (Exception e){}
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add("REFRESH")
                .setIcon(R.drawable.ic_baseline_settings_backup_restore_24)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);


        getMenuInflater().inflate(R.menu.too_menu, menu);
        MenuItem mSearch = menu.findItem(R.id.action_search);

        SearchView searchView=new SearchView(getSupportActionBar().getThemedContext());
        mSearch.setActionView(searchView);
        searchView.setQueryHint("Search");
        searchView.setIconifiedByDefault(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null)
                {
                    page=1;
                    customers.clear();
                    search(query);
                }

                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {

                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);

    }

    private void search(String search){
        pb.setVisibility(View.VISIBLE);
        if(userId!=null){
            new Thread(() -> {
                MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("CustomersSear ",response);
                        postExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                pb.setVisibility(View.GONE);
                                setResult(response);

                                if (customers.size()>0){
                                    tv_msg.setVisibility(View.GONE);
                                }else{
                                    tv_msg.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                    @Override
                    public void onError(String msg) {
                        Log.e("ErrSearch ",msg);
                        pb.setVisibility(View.GONE);

                    }
                }).url(Routing.SEARCH_CUSTOMERS+"?user_id="+userId+"&page="+page+"&search="+search);

                myHttp.runTask();
            }).start();
        }else{
            new AuthChecker(this).logout();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }else{
            if(item.getTitle().equals("REFRESH")){
                page=1;
                customers.clear();
                fetchCustomers(page);
            }
        }

        return super.onOptionsItemSelected(item);
    }

}