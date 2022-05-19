package com.madmax.acamobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.madmax.acamobile.adapters.BrandAdapter;
import com.madmax.acamobile.app.Initializer;

import java.util.Objects;

public class BrandListActivity extends AppCompatActivity {


    RecyclerView recyclerView;
    String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brand_list);
        key=getIntent().getExtras().getString("key");
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Brands");
        setUpView();
    }

    private void setUpView(){
        recyclerView=findViewById(R.id.recyclerView);

        LinearLayoutManager lm=new LinearLayoutManager(this);
        BrandAdapter adapter=new BrandAdapter(this, Initializer.brands,key);
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}