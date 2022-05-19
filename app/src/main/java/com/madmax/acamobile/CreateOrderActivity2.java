package com.madmax.acamobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.madmax.acamobile.adapters.OrderCreateGroupAdapter;
import com.madmax.acamobile.app.AuthChecker;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.interfaces.GroupSelectListener;
import com.madmax.acamobile.models.MyGroupModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executor;

public class CreateOrderActivity2 extends AppCompatActivity {

    ProgressBar pb;
    RecyclerView recyclerView;
    TextView tv_message,tv_select;

    OrderCreateGroupAdapter adapter;
    ArrayList<MyGroupModel> groups=new ArrayList<>();

    SharedPreferences sharedPreferences;
    String userId,authToken;
    Executor postExecutor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_order2);

        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=sharedPreferences.getString("userId",null);
        authToken=sharedPreferences.getString("authToken",null);
        postExecutor= ContextCompat.getMainExecutor(this);

        setUpView();
        fetchGroups();
    }



    private void setUpView(){
        pb=findViewById(R.id.pb);
        recyclerView=findViewById(R.id.recyclerView);
        tv_message=findViewById(R.id.tv_message);
        tv_select=findViewById(R.id.tv_select);


        LinearLayoutManager lm=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lm);
        GroupSelectListener mListener=new GroupSelectListener() {
            @Override
            public void onSelected(MyGroupModel model) {
               CreateOrderActivity.selectedGroup=model;
               finish();
            }
        };
        adapter=new OrderCreateGroupAdapter(this,groups,mListener);
        recyclerView.setAdapter(adapter);
    }

    private void fetchGroups(){
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
                                    tv_message.setText("No Groups to send orders");
                                }
                            }
                        });

                    }
                    @Override
                    public void onError(String msg) {
                        Log.e("Err Partner GP ",msg);

                    }
                }).url(Routing.GET_ORDER_GROUPS+"?user_id="+userId);

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
            String admin=jo.getString("admin");
            String group_des="Founded by "+admin;
            String imageUrl=jo.getString("group_image");
            groups.add(new MyGroupModel(group_id,name,group_des,imageUrl));
        }

        adapter.notifyDataSetChanged();


    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}