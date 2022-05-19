package com.madmax.acamobile;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.RealPathUtil;
import com.madmax.acamobile.app.Routing;

import org.json.JSONObject;

import java.util.concurrent.Executor;

public class AddGroupActivity extends AppCompatActivity {

    ImageView iv_group,iv_group2;
    EditText et_name,et_description;
    FloatingActionButton fab_addGroup;
    ProgressBar pb;
    SharedPreferences sharedPreferences;
    String userId,authToken,category_id="9";
    String imagePath="";


    Executor postExecutor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        postExecutor= ContextCompat.getMainExecutor(this);
        userId=sharedPreferences.getString("userId",null);
        authToken=sharedPreferences.getString("authToken",null);


        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_add_group);
        setUpView();
    }


    private void setUpView(){
        iv_group=findViewById(R.id.iv_group);
        iv_group2=findViewById(R.id.iv_group2);
        et_name=findViewById(R.id.et_productName);
        et_description=findViewById(R.id.et_initialPrice);
        fab_addGroup=findViewById(R.id.fab_addGroup);
        pb=findViewById(R.id.pb);


        iv_group2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPermissionGranted()){
                    pickImageFromGallery();
                }else {
                    takePermission();
                }
            }
        });

        iv_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPermissionGranted()){
                    pickImageFromGallery();
                }else {
                    takePermission();
                }
            }
        });

        fab_addGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createGroup();
            }
        });
    }

    private void createGroup(){
        if(validated()){
            pb.setVisibility(View.VISIBLE);
            new Thread(() -> {
                MyHttp myHttp = new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("createGP ",response);
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
                                        Toast.makeText(getApplicationContext(), "Upload fail! Try again.", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                }
                            }
                        });

                    }

                    @Override
                    public void onError(String msg) {
                        Log.e("createGPErr ",msg);
                    }
                }).url(Routing.CREATE_GROUP)
                        .field("user_id", userId)
                        .field("auth_token", authToken)
                        .field("group_name",et_name.getText().toString())
                        .field("group_description",et_description.getText().toString())
                        .file("myfile",imagePath);
                myHttp.runTask();
            }).start();
        }
    }

    private boolean validated() {
        if(userId==null||authToken==null){
            Toast.makeText(getApplicationContext(),"Log out",Toast.LENGTH_SHORT).show();
            return false;
        }else if (TextUtils.isEmpty(et_name.getText().toString())){
            Toast.makeText(getApplicationContext(),"Please enter group name",Toast.LENGTH_SHORT).show();
            return  false;
        }else if(TextUtils.isEmpty(et_description.getText().toString())){
            Toast.makeText(getApplicationContext(), "Please enter a description", Toast.LENGTH_SHORT).show();
            return  false;
        }else if(imagePath.equals("")){
            Toast.makeText(getApplicationContext(), "Please select a group cover photo", Toast.LENGTH_SHORT).show();
            return  false;
        }else{
            return  true;
        }
    }


    private boolean isPermissionGranted(){
        int  readExternalStorage= ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
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
                        iv_group2.setVisibility(View.GONE);
                        iv_group.setImageURI(uri);
                        imagePath= RealPathUtil.getRealPath(AddGroupActivity.this,uri);
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}