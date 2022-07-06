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

import com.madmax.acamobile.adapters.PartnerGroupAdapter;
import com.madmax.acamobile.app.AuthChecker;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.models.MyGroupModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

public class PartnerGroupListActivity extends AppCompatActivity {

    ProgressBar pb;
    RecyclerView recyclerView;
    TextView tv_message;

    SharedPreferences sharedPreferences;
    String userId,authToken;
    Executor postExecutor;

    int page=1;
    private boolean loading=true;
    int visibleItemCount,totalItemCount;
    public static int pastVisibleItems;

    PartnerGroupAdapter adapter;
    ArrayList<MyGroupModel> groups=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partner_group_list);


        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=sharedPreferences.getString("userId",null);
        authToken=sharedPreferences.getString("authToken",null);
        postExecutor= ContextCompat.getMainExecutor(this);

        setTitle("Leader Groups");
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setUpView();
        groups.clear();
        fetchMyGroup(1);

    }


    private void setUpView(){
        pb=findViewById(R.id.pb);
        recyclerView=findViewById(R.id.recyclerView);
        tv_message=findViewById(R.id.tv_msg);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lm);
        //recycler.addItemDecoration(new SpacingItemDecoration(2, XUtils.toPx(this, 2), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new PartnerGroupAdapter(this,groups);
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
                            fetchMyGroup(page);

                        }
                    }


                }
            }
        });

    }


    private void fetchMyGroup(int page){
        if(userId!=null){
            new Thread(() -> {
                MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                    @Override
                    public void onResponse(String response) {
                        postExecutor.execute(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    JSONObject jo=new JSONObject(response);
                                    String groups=jo.getString("groups");
                                    setResult(groups);

                                }catch (JSONException e){

                                }
                                pb.setVisibility(View.GONE);
                                if (groups.size()>0){
                                    tv_message.setVisibility(View.GONE);
                                }else{
                                    tv_message.setVisibility(View.VISIBLE);
                                }
                            }
                        });

                    }
                    @Override
                    public void onError(String msg) {
                        Log.e("Err Partner GP ",msg);

                    }
                }).url(Routing.GET_PARTNER_GROUP+"?user_id="+userId+"&page="+page);

                myHttp.runTask();
            }).start();
        }else{
            new AuthChecker(this).logout();
        }
    }


    private void setResult (String response) throws JSONException {


        JSONArray ja=new JSONArray(response);
        for(int i=0;i<ja.length();i++){
            JSONObject jo=ja.getJSONObject(i);
            String name=jo.getString("group_name");
            String group_id=jo.getString("group_id");
            String group_des=jo.getString("group_description");
            String imageUrl=jo.getString("group_image");
            groups.add(new MyGroupModel(group_id,name,group_des,imageUrl));
        }

        adapter.notifyDataSetChanged();
        loading=true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}