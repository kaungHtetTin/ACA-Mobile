package com.madmax.acamobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;

import org.json.JSONObject;

import java.util.concurrent.Executor;

public class AboutGroupActivity extends AppCompatActivity {

    ImageView iv_group_small,iv_collapse,iv_profile;
    FloatingActionButton fab;
    Executor postExecutor;
    Toolbar toolbar;
    CollapsingToolbarLayout toolbarLayout;
    NestedScrollView nestedScrollView;
    TextView tv_name,tv_group_name,tv_group_description,tv_join_date,tv_create_date;

    String groupId,phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_group);

        postExecutor= ContextCompat.getMainExecutor(this);
        groupId=getIntent().getExtras().getString("groupId");
        setUpView();
        fetchAboutGroup();

    }

    private void setUpView(){
        tv_group_name=findViewById(R.id.tv_group_name);
        tv_group_description=findViewById(R.id.tv_group_description);
        tv_join_date=findViewById(R.id.tv_join_date);
        tv_create_date=findViewById(R.id.tv_create_date);
        tv_name=findViewById(R.id.tv_name);
        iv_group_small=findViewById(R.id.iv_group_small);
        iv_collapse=findViewById(R.id.iv_collapse);
        iv_profile=findViewById(R.id.iv_profile);
        fab=findViewById(R.id.fab);
        toolbar=findViewById(R.id.toolbar);
        nestedScrollView=findViewById(R.id.nested_layout);
        nestedScrollView.setVisibility(View.INVISIBLE);

        toolbarLayout=findViewById(R.id.ctb);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        AppBarLayout mAppBarLayout =findViewById(R.id.app_bar_layout);
        toolbarLayout.setTitle("Loading ... ");


        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                    iv_group_small.setVisibility(View.GONE);

                }
                if (scrollRange + verticalOffset == 0) {
                    isShow = true;
                    iv_group_small.setVisibility(View.VISIBLE);


                } else if (isShow) {
                    isShow = false;
                    iv_group_small.setVisibility(View.GONE);

                }
            }
        });
    }

    private void fetchAboutGroup(){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    Log.e("gp about ",response );
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            setResult(response);
                        }
                    });

                }
                @Override
                public void onError(String msg) {
                    Log.e("Err MY GP ",msg);

                }
            }).url(Routing.GET_ABOUT_GROUP+"?group_id="+groupId);

            myHttp.runTask();
        }).start();
    }

    private void setResult(String response){
        try {
            JSONObject jo=new JSONObject(response);
            String groupName=jo.getString("group_name");
            String groupDescription= jo.getString("group_description");
            String groupImage=jo.getString("group_image");
            long createAt=Long.parseLong(jo.getString("group_create_at"));

            String founderName=jo.getString("founder");
            String founderProfile=jo.getString("profile_image");
            phone=jo.getString("phone");
            long joinAt=Long.parseLong(jo.getString("join_at"));


            tv_group_name.setText(groupName);
            tv_group_description.setText(groupDescription);
            toolbarLayout.setTitle(groupName);
            AppUtils.setPhotoFromRealUrl(iv_collapse,Routing.GROUP_COVER_URL+groupImage);
            AppUtils.setPhotoFromRealUrl(iv_group_small,Routing.GROUP_COVER_URL+groupImage);
            tv_create_date.setText(AppUtils.formatTime(createAt));

            tv_name.setText(founderName);
            AppUtils.setPhotoFromRealUrl(iv_profile,Routing.PROFILE_URL+founderProfile);
            tv_join_date.setText(AppUtils.formatTime(joinAt));

            nestedScrollView.setVisibility(View.VISIBLE);

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(isPermissionGranted()){
                        callPhone(phone);
                    }else{
                        takePermission();
                    }
                }
            });

        }catch (Exception e){

        }

    }

    private void takePermission(){
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CALL_PHONE},101);
    }

    private boolean isPermissionGranted(){
        int  callPhone= ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
        return  callPhone== PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0){
            if(requestCode==101){
                boolean callPhone=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                if(callPhone){
                    callPhone(phone);
                }else {
                    takePermission();
                }
            }
        }
    }

    private void callPhone(String phone){
        phone=phone.replace("#","%23");
        Intent intent=new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+phone));
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}