package com.madmax.acamobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;

import org.json.JSONObject;

import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {

    EditText et_email,et_password;
    Button bt_login;
    TextView tv_forgetPassword,tv_register,tv_error;
    ProgressBar pb;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String fcmToken;
    Executor postExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
        fcmToken=sharedPreferences.getString("fcmToken","abcdef");
        postExecutor= ContextCompat.getMainExecutor(this);

        setContentView(R.layout.activity_login);
        setUpView();
    }

    private void setUpView(){
        et_email=findViewById(R.id.et_email);
        et_password=findViewById(R.id.et_password);
        tv_forgetPassword=findViewById(R.id.tv_forget_password);
        tv_register=findViewById(R.id.tv_register);
        tv_error=findViewById(R.id.tv_error);
        bt_login=findViewById(R.id.bt_login);
        pb=findViewById(R.id.pb);

        tv_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(LoginActivity.this,SignUpActivity.class);
                startActivity(intent);
            }
        });

        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Login(et_email.getText().toString(),et_password.getText().toString());
            }
        });

    }

    private void Login(String email,String password){
        pb.setVisibility(View.VISIBLE);
        tv_error.setVisibility(View.GONE);
        bt_login.setEnabled(false);
        if(!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(password)){
            new Thread(() -> {
                MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                    @Override
                    public void onResponse(String response) {
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
                                        Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }else{
                                        String error=jo.getString("error");
                                        tv_error.setText(error);
                                        tv_error.setVisibility(View.VISIBLE);
                                        bt_login.setEnabled(true);
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
                                bt_login.setEnabled(true);
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
            bt_login.setEnabled(true);
        }
    }
}