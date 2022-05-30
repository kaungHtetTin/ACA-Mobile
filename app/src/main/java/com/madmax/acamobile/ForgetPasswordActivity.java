package com.madmax.acamobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Objects;
import java.util.concurrent.Executor;

public class ForgetPasswordActivity extends AppCompatActivity {

    ProgressBar pb;
    EditText et;
    Button bt;
    TextView tv_error,tv_info;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    Animation animOut;
    Animation animIn;

    Executor postExecutor;
    int checker=0;

    String email,password;
    String otp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();

        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Reset Password");
        animOut= AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        animIn= AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        postExecutor= ContextCompat.getMainExecutor(this);


        setUpView();


    }

    private void setUpView(){
        pb=findViewById(R.id.pb_loading);
        et=findViewById(R.id.et);
        bt=findViewById(R.id.bt);
        tv_error=findViewById(R.id.tv_error);
        tv_info=findViewById(R.id.tv_info);


        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checker==0){
                    email=et.getText().toString();
                    if(!TextUtils.isEmpty(email)){
                        checkEmail(email);
                    }else{
                        Toast.makeText(getApplicationContext(),"Please enter your email address",Toast.LENGTH_SHORT).show();
                    }
                }

                if(checker==1){
                    otp=et.getText().toString();
                    if(!TextUtils.isEmpty(otp)){
                        setOTP(otp);
                    }else{
                        Toast.makeText(getApplicationContext(),"Please enter OTP",Toast.LENGTH_SHORT).show();
                    }
                }

                if(checker==2){
                    password=et.getText().toString();
                    if(!TextUtils.isEmpty(password)){
                        resetPassword(password);
                    }else{
                        Toast.makeText(getApplicationContext(),"Please enter your new password",Toast.LENGTH_SHORT).show();
                    }
                }

                if(checker==3){
                    Login(email,password);
                }
            }
        });
    }

    private void resetPassword(String password){
        bt.setEnabled(false);
        pb.setVisibility(View.VISIBLE);
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    Log.e("set OTP ",response);
                    postExecutor.execute(() -> {
                        pb.setVisibility(View.INVISIBLE);
                        bt.setEnabled(true);
                        try {
                            JSONObject jo=new JSONObject(response);
                            String code=jo.getString("code");
                            String msg=jo.getString("msg");

                            if(code.equals("50")){
                                tv_error.setText("");
                                bt.setText("Login");
                                et.setVisibility(View.INVISIBLE);
                                tv_info.setText(msg);
                                checker++;
                            }else {
                                tv_error.setText(msg);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(() -> {
                        pb.setVisibility(View.INVISIBLE);
                        bt.setEnabled(true);
                        tv_error.setVisibility(View.VISIBLE);
                        tv_error.setText("An unexpected error! Connect to help center");
                    });
                }
            }).url(Routing.RESET_PASSWORD_WITH_OTP)
                    .field("otp",otp)
                    .field("email",email)
                    .field("new_password",password);

            myHttp.runTask();
        }).start();
    }


    private void setOTP(String otp){
        bt.setEnabled(false);
        pb.setVisibility(View.VISIBLE);
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    Log.e("set OTP ",response);
                    postExecutor.execute(() -> {
                        pb.setVisibility(View.INVISIBLE);
                        bt.setEnabled(true);
                        try {
                            JSONObject jo=new JSONObject(response);
                            String code=jo.getString("code");
                            String msg=jo.getString("msg");

                            if(code.equals("50")){
                                tv_error.setText("");
                                bt.setText("Reset");
                                et.setHint("Please enter new password");
                                et.setText("");
                                tv_info.setText(msg);
                                checker++;
                            }else {
                                tv_error.setText(msg);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(() -> {
                        pb.setVisibility(View.INVISIBLE);
                        bt.setEnabled(true);
                        tv_error.setVisibility(View.VISIBLE);
                        tv_error.setText("An unexpected error! Connect to help center");
                    });
                }
            }).url(Routing.CHECK_OTP)
                    .field("otp",otp)
                    .field("email",email);

            myHttp.runTask();
        }).start();
    }

    private void checkEmail(String email){
        bt.setEnabled(false);
        pb.setVisibility(View.VISIBLE);
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    Log.e("forget password ",response);
                    postExecutor.execute(() -> {
                        pb.setVisibility(View.INVISIBLE);
                        bt.setEnabled(true);
                        try {
                            JSONObject jo=new JSONObject(response);
                            String code=jo.getString("code");
                            String msg=jo.getString("msg");

                            if(code.equals("50")){
                                tv_error.setText("");
                                bt.setText("SEND");
                                et.setHint("Please enter code here");
                                et.setText("");
                                tv_info.setText(msg);
                                checker++;
                            }else {
                                tv_error.setText(msg);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(() -> {
                        pb.setVisibility(View.INVISIBLE);
                        bt.setEnabled(true);
                        tv_error.setVisibility(View.VISIBLE);
                        tv_error.setText("An unexpected error! Connect to help center");
                    });
                }
            }).url(Routing.GENERATE_OTP)
                    .field("email",email);

            myHttp.runTask();
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void Login(String email,String password){
        pb.setVisibility(View.VISIBLE);
        tv_error.setVisibility(View.GONE);
        bt.setEnabled(false);
        if(!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(password)){
            new Thread(() -> {
                MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("login ",response);
                        postExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                pb.setVisibility(View.GONE);
                                try {
                                    JSONObject jo=new JSONObject(response);
                                    String auth=jo.getString("auth");
                                    if(auth.equals("success")){
                                        String userId = jo.getString("user_id");
                                        String authToken = jo.getString("auth_token");
                                        String profileImage=jo.getString("profile_image");
                                        editor.putString("profileImage",profileImage);
                                        editor.putString("authToken", authToken);
                                        editor.putString("userId", userId);
                                        editor.apply();
                                        Intent intent=new Intent(ForgetPasswordActivity.this,MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }else{
                                        String error=jo.getString("error");
                                        tv_error.setText(error);
                                        tv_error.setVisibility(View.VISIBLE);
                                        bt.setEnabled(true);
                                    }
                                }catch (Exception e){
                                    Log.e("Login Json : ",e.toString());
                                }
                            }
                        });

                    }
                    @Override
                    public void onError(String msg) {
                        postExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                pb.setVisibility(View.GONE);
                                tv_error.setText("Unexpected Error! Please connect to help center.");
                                tv_error.setVisibility(View.VISIBLE);
                                bt.setEnabled(true);
                                Log.e("Login Err ",msg);
                            }
                        });

                    }
                }).url(Routing.LOGIN)
                        .field("email",email)
                        .field("password",password);
                myHttp.runTask();
            }).start();
        }else{
            pb.setVisibility(View.GONE);
            tv_error.setText("Please complete all fields.");
            tv_error.setVisibility(View.VISIBLE);
            bt.setEnabled(true);
        }
    }

}