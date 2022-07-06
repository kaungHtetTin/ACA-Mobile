package com.madmax.acamobile;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.madmax.acamobile.app.MyHttp;

import org.json.JSONObject;

import java.util.concurrent.Executor;

public class DataUpdateActivity extends AppCompatActivity {

    EditText et;
    TextView tv;
    ProgressBar pb;
    String userId,authToken;
    SharedPreferences sharedPreferences;
    String hint,message,key,url,contentId;
    Executor postExecutor;

    String extra1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_update);

        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        postExecutor= ContextCompat.getMainExecutor(this);

        authToken=sharedPreferences.getString("authToken",null);
        userId=sharedPreferences.getString("userId",null);

        hint=getIntent().getExtras().getString("hint");
        message=getIntent().getExtras().getString("message");
        key=getIntent().getExtras().getString("key");
        url=getIntent().getExtras().getString("url");
        contentId=getIntent().getExtras().getString("contentId","00");
        extra1=getIntent().getExtras().getString("extra1","");


        setUpView();


        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    private void setUpView(){
        et=findViewById(R.id.et_profile_data);
        tv=findViewById(R.id.tv_profile_data);
        pb=findViewById(R.id.pb);

        if(key.equals("phone")){
            et.setInputType(InputType.TYPE_CLASS_PHONE);
        }else if(key.equals("discount") || key.equals("initial") || key.equals("count") || key.equals("price")||
                key.equals("item_left")||key.equals("admin_extra_cost")){
            et.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
        et.setHint(hint);
        tv.setText(message);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Update")
                .setIcon(R.drawable.ic_baseline_check_24)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }else{
            if(item.getTitle().toString().equals("Update")){
                if(!TextUtils.isEmpty(et.getText().toString())){
                    updateData(et.getText().toString());
                }else{
                    Toast.makeText(getApplicationContext(), "Please enter a update value", Toast.LENGTH_SHORT).show();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateData(String data) {
        if (userId != null && authToken != null) {
            pb.setVisibility(View.VISIBLE);
            new Thread(() -> {
                MyHttp myHttp = new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("UpdateRes ",response);
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
                        .field("user_id", userId)
                        .field("auth_token", authToken)
                        .field("key",key)
                        .field("content_id",contentId)
                        .field("extra1",extra1)
                        .field("value",data);
                myHttp.runTask();
            }).start();
        }
    }
}