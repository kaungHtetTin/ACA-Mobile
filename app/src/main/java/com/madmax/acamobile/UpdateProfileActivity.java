package com.madmax.acamobile;


import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.app.AuthChecker;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;

import org.json.JSONObject;

import java.util.concurrent.Executor;

public class UpdateProfileActivity extends AppCompatActivity  {

    Toolbar toolbar;
    ImageView iv_profile_small,iv_collapse;
    CollapsingToolbarLayout toolbarLayout;
    RelativeLayout email_layout,phone_layout,name_layout,address_layout;
    TextView tv_email,tv_phone,tv_name,tv_address;
    FloatingActionButton fab;
    NestedScrollView nestedScrollView;

    Executor postExecutor;

    String userId;
    SharedPreferences sharedPreferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        setContentView(R.layout.activity_update_profile);

        postExecutor= ContextCompat.getMainExecutor(this);
        userId=sharedPreferences.getString("userId",null);

        setUpView();
        fetchProfileData();
    }

    private void setUpView(){
        email_layout=findViewById(R.id.item_layout);
        phone_layout=findViewById(R.id.phone_layout);
        name_layout=findViewById(R.id.name_layout);
        address_layout=findViewById(R.id.address_layout);
        nestedScrollView=findViewById(R.id.profile_layout);
        fab=findViewById(R.id.fab);

        tv_email=findViewById(R.id.tv_item_left);
        tv_phone=findViewById(R.id.tv_phone);
        tv_name=findViewById(R.id.tv_name);
        tv_address=findViewById(R.id.tv_address);

        toolbar=findViewById(R.id.toolbar);
        iv_profile_small=findViewById(R.id.iv_product_small);
        iv_collapse=findViewById(R.id.iv_collapse);
        toolbarLayout=findViewById(R.id.ctb);


        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        AppBarLayout mAppBarLayout =findViewById(R.id.app_bar_layout);
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                    iv_profile_small.setVisibility(View.GONE);

                }
                if (scrollRange + verticalOffset == 0) {
                    isShow = true;
                    iv_profile_small.setVisibility(View.VISIBLE);


                } else if (isShow) {
                    isShow = false;
                    iv_profile_small.setVisibility(View.GONE);

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


        email_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"Cannot change email",Toast.LENGTH_SHORT).show();
            }
        });

        phone_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToProfileContentUpdate("Update your phone number","Add your phone number so that your partner can contact you easily.","phone");
            }
        });

        name_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToProfileContentUpdate("Update your name","We recommend to type your name in English.","name");
            }
        });

        address_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToProfileContentUpdate("Update your address","Add your address so that your partner can contact you easily.We recommend to type your address in English.","address");
            }
        });


    }

    private void goToProfileContentUpdate(String hint,String message,String key){
        Intent intent=new Intent(UpdateProfileActivity.this,DataUpdateActivity.class);
        intent.putExtra("hint",hint);
        intent.putExtra("message",message);
        intent.putExtra("key",key);
        intent.putExtra("url",Routing.UPDATE_PROFILE_DATA);
        startActivity(intent);
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
                        Intent intent=new Intent(UpdateProfileActivity.this,UpdateImageActivity.class);
                        intent.putExtra("imageUri", uri.toString());
                        intent.putExtra("url",Routing.UPLOAD_PROFILE_IMAGE);
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


    private void fetchProfileData(){
        nestedScrollView.setVisibility(View.GONE);
        toolbarLayout.setTitle("Loading ...");
        if(userId!=null){
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
                        Log.e("Err profile ",msg);

                    }
                }).url(Routing.GET_PROFILE+"?user_id="+userId);

                myHttp.runTask();
            }).start();
        }else{
            new AuthChecker(this).logout();
        }
    }

    private  void setResult(String response){
        try {
            nestedScrollView.setVisibility(View.VISIBLE);
            JSONObject jo=new JSONObject(response);
            String name=jo.getString("name");
            String profileImage=Routing.PROFILE_URL+jo.getString("profile_image");
            String email=jo.getString("email");
            String phone=jo.getString("phone");
            String address=jo.getString("address");

            toolbarLayout.setTitle(name);
            tv_name.setText(name);
            tv_email.setText(email);
            tv_phone.setText(phone);

            if(address.equals("")){
                tv_address.setText("No Address is added.");
            }else{
                tv_address.setText(address);
            }

            AppUtils.setPhotoFromRealUrl(iv_profile_small,profileImage);
            AppUtils.setPhotoFromRealUrl(iv_collapse,profileImage);

        }catch (Exception e){}
    }

    @Override
    protected void onResume() {
        super.onResume();

        fetchProfileData();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}