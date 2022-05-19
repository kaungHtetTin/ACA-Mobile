package com.madmax.acamobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import java.util.Objects;

public class BusinessActivity extends AppCompatActivity {

     CardView cv_order,cv_sent_order,cv_new_order,cv_received_order,cv_voucher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("My Business");
        setContentView(R.layout.activity_business);

        setUpView();
    }

    private void setUpView(){
        cv_order=findViewById(R.id.card_order);
        cv_sent_order=findViewById(R.id.card_sent_order);
        cv_new_order=findViewById(R.id.card_new_order);
        cv_received_order=findViewById(R.id.card_received_order);
        cv_voucher=findViewById(R.id.card_vouchers);

        cv_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(BusinessActivity.this,OrderActivity.class);
                intent.putExtra("isSoldOut",0);
                startActivity(intent);
            }
        });

        cv_sent_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(BusinessActivity.this,OrderActivity.class);
                intent.putExtra("isSoldOut",1);
                startActivity(intent);
            }
        });

        cv_new_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(BusinessActivity.this,MyOrderActivity.class);
                intent.putExtra("isReceived",0);
                startActivity(intent);
            }
        });

        cv_received_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(BusinessActivity.this,MyOrderActivity.class);
                intent.putExtra("isReceived",1);
                startActivity(intent);
            }
        });

        cv_voucher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(BusinessActivity.this,SaleListActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}