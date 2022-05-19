package com.madmax.acamobile;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.app.MyDialog;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;

import org.json.JSONObject;

import java.util.concurrent.Executor;

public class GroupSettingActivity extends AppCompatActivity {

    ImageView iv_group_small,iv_collapse;
    LinearLayout name_layout,delete_layout,date_layout,description_layout;
    TextView tv_name,tv_description,tv_date;
    FloatingActionButton fab;
    Executor postExecutor;
    Toolbar toolbar;
    CollapsingToolbarLayout toolbarLayout;
    SharedPreferences sharedPreferences;

    String userId,authToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_setting);

        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=sharedPreferences.getString("userId",null);
        authToken=sharedPreferences.getString("authToken",null);
        postExecutor= ContextCompat.getMainExecutor(this);

        setUpView();
    }

    private void setUpView(){
        iv_group_small=findViewById(R.id.iv_group_small);
        iv_collapse=findViewById(R.id.iv_collapse);
        name_layout=findViewById(R.id.name_layout);
        delete_layout=findViewById(R.id.delete_layout);
        date_layout=findViewById(R.id.date_layout);
        description_layout=findViewById(R.id.description_layout);
        tv_name=findViewById(R.id.tv_name);
        tv_description=findViewById(R.id.tv_description);
        tv_date=findViewById(R.id.tv_date);
        fab=findViewById(R.id.fab);
        toolbar=findViewById(R.id.toolbar);

        toolbarLayout=findViewById(R.id.ctb);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        AppBarLayout mAppBarLayout =findViewById(R.id.app_bar_layout);

        toolbarLayout.setTitle(MyGroupDetailActivity.name);
        AppUtils.setPhotoFromRealUrl(iv_group_small,MyGroupDetailActivity.imageUrl);
        AppUtils.setPhotoFromRealUrl(iv_collapse,MyGroupDetailActivity.imageUrl);


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


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPermissionGranted()){
                    pickImageFromGallery();
                }else {
                    takePermission();
                }
            }
        });

        name_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToContentUpdate("Enter group name","Update your group name ("+MyGroupDetailActivity.name+")","group_name");
            }
        });

        description_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToContentUpdate("Enter group description","Update a description for your group.\n("+MyGroupDetailActivity.description+")","group_description");
            }
        });

        delete_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyDialog myDialog=new MyDialog(GroupSettingActivity.this, "Delete Group!!", "Your group will be permanently removed", new MyDialog.ConfirmClick() {
                    @Override
                    public void onConfirmClick() {
                        disableGroup();
                    }
                });
                myDialog.showMyDialog();
            }
        });
    }

    private void goToContentUpdate(String hint,String message,String key){
        Intent intent=new Intent(GroupSettingActivity.this,DataUpdateActivity.class);
        intent.putExtra("hint",hint);
        intent.putExtra("message",message);
        intent.putExtra("key",key);
        intent.putExtra("contentId",MyGroupDetailActivity.groupId);
        intent.putExtra("url", Routing.UPDATE_GROUP);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isPermissionGranted(){
        int  readExternalStorage=ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        return  readExternalStorage== PackageManager.PERMISSION_GRANTED;
    }

    private void takePermission(){
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},101);
    }


    private void pickImageFromGallery(){
        mGetContent.launch("image/*");
    }

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if(uri!=null){
                        Intent intent=new Intent(GroupSettingActivity.this,UpdateImageActivity.class);
                        intent.putExtra("imageUri", uri.toString());
                        intent.putExtra("url", Routing.UPDATE_GROUP_IMAGE);
                        intent.putExtra("contentId",MyGroupDetailActivity.groupId);
                        startActivity(intent);
                    }else{
                        Toast.makeText(getApplicationContext(),"No photo is selected!",Toast.LENGTH_SHORT).show();
                    }
                }
            });


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0){
            if(requestCode==101){
                boolean readExternalStorage=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                if(readExternalStorage){
                    pickImageFromGallery();
                }else {
                    takePermission();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchGroupData();
    }

    private void fetchGroupData(){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
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
            }).url(Routing.GET_GROUP_DETAIL+"?group_id="+MyGroupDetailActivity.groupId);

            myHttp.runTask();
        }).start();
    }

    private void setResult(String response){
        try {
            JSONObject jo=new JSONObject(response);
            String name=jo.getString("group_name");
            String description=jo.getString("group_description");
            String imageUrl=jo.getString("group_image");
            long time=Long.parseLong(jo.getString("time"));

            MyGroupDetailActivity.name=name;
            MyGroupDetailActivity.description=description;
            MyGroupDetailActivity.imageUrl= Routing.GROUP_COVER_URL+imageUrl;

            tv_name.setText(name);
            tv_description.setText(description);
            AppUtils.setPhotoFromRealUrl(iv_group_small,MyGroupDetailActivity.imageUrl);
            AppUtils.setPhotoFromRealUrl(iv_collapse,MyGroupDetailActivity.imageUrl);

            tv_date.setText(AppUtils.formatTime(time));
            toolbarLayout.setTitle(name);

        }catch (Exception e){}
    }

    private void disableGroup(){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jo=new JSONObject(response);
                        boolean success=jo.getString("status").equals("success");
                        if(success){
                            MyGroupDetailActivity.isDisable=true;
                            finish();
                        }
                    }catch (Exception e){}

                }
                @Override
                public void onError(String msg) {
                    Log.e("Err MY GP ",msg);

                }
            }).url(Routing.DISABLE_GROUP)
                    .field("user_id",userId)
                    .field("group_id",MyGroupDetailActivity.groupId)
                    .field("auth_token",authToken);
            myHttp.runTask();
        }).start();
    }
}