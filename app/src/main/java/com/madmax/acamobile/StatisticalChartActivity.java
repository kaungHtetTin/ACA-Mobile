package com.madmax.acamobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.madmax.acamobile.charts.ProfitPerItem;
import com.madmax.acamobile.charts.ProfitPerMonth;
import com.madmax.acamobile.charts.RetailAndAgentRate;
import com.madmax.acamobile.charts.SaleAndOrderRate;
import com.madmax.acamobile.charts.SaleRateInAProduct;
import com.madmax.acamobile.fragments.mygroup.FragmentMember;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

public class StatisticalChartActivity extends AppCompatActivity {

    Executor postExecutor;
    String userId;
    SharedPreferences sharedPreferences;
    LinearLayout mLayout;
    FragmentManager fragmentManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistical_chart);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);

        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userId=sharedPreferences.getString("userId",null);
        postExecutor= ContextCompat.getMainExecutor(this);
        fragmentManager=getSupportFragmentManager();

        setUpView();
    }

    private void setUpView(){
        mLayout=findViewById(R.id.mLayout);

        SaleAndOrderRate saleAndOrderRate=new SaleAndOrderRate(this,fragmentManager,userId);
        saleAndOrderRate.invisibleTVSeeMore();
        mLayout.addView(saleAndOrderRate.getView());

        RetailAndAgentRate retailAndAgentRate=new RetailAndAgentRate(this,userId,fragmentManager);
        mLayout.addView(retailAndAgentRate.getView());

        ProfitPerItem profitPerItem=new ProfitPerItem(this,fragmentManager,userId);
        mLayout.addView(profitPerItem.getView());

        ProfitPerMonth profitPerMonth=new ProfitPerMonth(userId,this);
        mLayout.addView(profitPerMonth.getView());

        SaleRateInAProduct saleRateInAProduct=new SaleRateInAProduct(userId,this);
        mLayout.addView(saleRateInAProduct.getView());

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}