package com.madmax.acamobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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

    Animation animOut;
    Animation animIn;

    Executor postExecutor;
    int checker=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
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
                    String email=et.getText().toString();
                    if(!TextUtils.isEmpty(email)){
                        checkEmail(email);
                    }else{
                        Toast.makeText(getApplicationContext(),"Please enter your email address",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
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
                                tv_info.setText(msg);
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

}