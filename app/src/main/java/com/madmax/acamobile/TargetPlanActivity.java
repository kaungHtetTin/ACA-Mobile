package com.madmax.acamobile;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.madmax.acamobile.adapters.TargetPlanAdapter;
import com.madmax.acamobile.app.AuthChecker;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.models.MemberModel;
import com.madmax.acamobile.models.TargetPlanModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

public class TargetPlanActivity extends AppCompatActivity {

    TextView tv_msg;
    ProgressBar pb;
    RecyclerView recyclerView;
    FloatingActionButton fab;

    ArrayList<TargetPlanModel> plans=new ArrayList<>();
    TargetPlanAdapter adapter;

    int page=1;
    private boolean loading=true;
    int visibleItemCount,totalItemCount;
    public static int pastVisibleItems;

    Executor postExecutor;

    SharedPreferences sharedPreferences;
    String userId,authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=sharedPreferences.getString("userId",null);
        authToken=sharedPreferences.getString("authToken",null);

        setContentView(R.layout.activity_target_plan);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        postExecutor= ContextCompat.getMainExecutor(this);
        setTitle("Target Plans");
        setUpView();

    }

    private void setUpView(){
        pb=findViewById(R.id.pb);
        tv_msg=findViewById(R.id.tv_msg);
        recyclerView=findViewById(R.id.recyclerView);
        fab=findViewById(R.id.fab);

        LinearLayoutManager lm=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lm);
        adapter=new TargetPlanAdapter(this,plans);
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
                            fetchPlans(page);
                        }
                    }
                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(TargetPlanActivity.this,AddTargetPlanActivity.class);
                intent.putExtra("group",false);
                intent.putExtra("groupId","");
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        page=1;
        loading=true;
        plans.clear();
        fetchPlans(1);
        super.onResume();
    }

    private void fetchPlans(int page) {
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

                                if (plans.size()>0){
                                    tv_msg.setVisibility(View.GONE);
                                }else{
                                    tv_msg.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                    @Override
                    public void onError(String msg) {
                        Log.e("Err MY GP ",msg);
                        pb.setVisibility(View.GONE);

                    }
                }).url(Routing.GET_TARGET_PLAN+"?user_id="+userId+"&page="+page);

                myHttp.runTask();
            }).start();
        }else{
            new AuthChecker(this).logout();
        }
    }

    private void setResult(String response) {
        try {
            JSONArray ja=new JSONObject(response).getJSONArray("plans");
            for (int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                int target_plan_id=jo.getInt("target_plan_id");
                long start_date=jo.getLong("start_date");
                long end_date=jo.getLong("end_date");

                plans.add(new TargetPlanModel(target_plan_id,start_date,end_date));
            }
            adapter.notifyDataSetChanged();
            loading=true;

        }catch (Exception e){

        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}