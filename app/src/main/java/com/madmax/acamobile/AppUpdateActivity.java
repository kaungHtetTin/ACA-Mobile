package com.madmax.acamobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.madmax.acamobile.adapters.AppAdapter;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.models.AppModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

public class AppUpdateActivity extends AppCompatActivity {

    TextView tv_currentVersion,tv_update;
    Button bt_download;
    ProgressBar pb_loading;
    RecyclerView recyclerView;

    Executor postExecutor;
    String link;
    float version;

    ArrayList<AppModel> apps=new ArrayList<>();
    AppAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_update);
        postExecutor= ContextCompat.getMainExecutor(this);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setUpView();
        setTitle("Update");
    }

    private void setUpView(){
        tv_currentVersion=findViewById(R.id.tv_current_version);
        tv_update=findViewById(R.id.tv_update);
        bt_download=findViewById(R.id.bt_download);
        pb_loading=findViewById(R.id.pb_loading);
        recyclerView=findViewById(R.id.recyclerView);

        tv_currentVersion.setText("Current Version is "+AppUtils.currentVersion);

        LinearLayoutManager lm=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lm);
        adapter=new AppAdapter(this,apps);
        recyclerView.setAdapter(adapter);

        getUpdateDataFromHostinger();
        getApps();

        bt_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                startActivity(browserIntent);
            }
        });
    }

    private void getUpdateDataFromHostinger(){
        pb_loading.setVisibility(View.VISIBLE);
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> {
                        getUpdateData(response);
                        pb_loading.setVisibility(View.GONE);
                    });

                }
                @Override
                public void onError(String msg) {}
            }).url(Routing.CHECK_UPDATE);
            myHttp.runTask();
        }).start();

    }

    private void getUpdateData(String response){
        pb_loading.setVisibility(View.GONE);
        try {
            JSONObject jo=new JSONObject(response);
            String status=jo.getString("status");
            version=(float) jo.getDouble("version");


            link=jo.getString("link");

            if(version> AppUtils.currentVersion){
                tv_update.setText(status);
                bt_download.setVisibility(View.VISIBLE);

            }else {
                tv_update.setText("No update version is available");
                bt_download.setVisibility(View.GONE);
            }

        }catch (Exception ignored){}
    }


    private void getApps(){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    Log.e("AppRes ",response);
                    postExecutor.execute(() -> {
                        setApps(response);
                    });

                }
                @Override
                public void onError(String msg) {
                    Log.e("App Err ",msg);
                }
            }).url("https://www.calamuseducation.com/calamus-v2/api/apps");
            myHttp.runTask();
        }).start();
    }

    private void setApps(String response){
        try {
            JSONArray ja=new JSONArray(response);
            for(int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                String name=jo.getString("name");
                String description=jo.getString("description");
                String icon=jo.getString("icon");
                String url=jo.getString("url");

                apps.add(new AppModel(name,description,icon,url));
            }
            adapter.notifyDataSetChanged();
        }catch (Exception e){
            Log.e("APPJSONERR ",e.toString());
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