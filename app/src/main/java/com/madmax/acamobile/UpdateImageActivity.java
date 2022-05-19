package com.madmax.acamobile;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.RealPathUtil;

import org.json.JSONObject;

import java.util.concurrent.Executor;

public class UpdateImageActivity extends AppCompatActivity{

    Uri uri;
    ImageView iv;
    FloatingActionButton fab;
    ProgressBar pb;
    SharedPreferences sharedPreferences;
    String userId,authToken,contentId;
    String imagePath,url;
    Executor postExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_image);

        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        postExecutor= ContextCompat.getMainExecutor(this);
        authToken=sharedPreferences.getString("authToken",null);
        userId=sharedPreferences.getString("userId",null);
        uri= Uri.parse(getIntent().getExtras().getString("imageUri"));
        imagePath= RealPathUtil.getRealPath(this,uri);
        contentId=getIntent().getExtras().getString("contentId","000");
        url=getIntent().getExtras().getString("url");
        setUpView();
        getSupportActionBar().hide();
    }

    private void setUpView(){
        iv=findViewById(R.id.iv_profile);
        fab=findViewById(R.id.floatingActionButton);
        pb=findViewById(R.id.pb);

        iv.setImageURI(uri);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
                fab.setEnabled(false);
            }
        });

    }

    private void uploadImage(){

        if(userId!=null && authToken!=null){
            pb.setVisibility(View.VISIBLE);
            new Thread(() -> {
                MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("imageUpload ",response);
                        postExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject jo=new JSONObject(response);
                                    boolean isSuccess=jo.getString("status").equals("success");
                                    if(isSuccess){
                                        finish();
                                    }else{
                                        pb.setVisibility(View.GONE);
                                        fab.setEnabled(true);
                                        Toast.makeText(getApplicationContext(),"Upload fail! Try again.",Toast.LENGTH_SHORT).show();
                                    }
                                }catch (Exception e){}
                            }
                        });

                    }
                    @Override
                    public void onError(String msg) {
                        Log.e("imageUploadErr ",msg);
                    }
                }).url(url)
                        .field("user_id",userId)
                        .field("auth_token",authToken)
                        .field("content_id",contentId)
                        .file("myfile",imagePath);
                myHttp.runTask();
            }).start();
        }
    }
}