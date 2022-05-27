package com.madmax.acamobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.app.Initializer;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.charts.FilteringChart;
import com.madmax.acamobile.charts.TargetPlanAndOrderRate;
import com.madmax.acamobile.models.ProductModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.Executor;

public class PlanAndOrderActivity extends AppCompatActivity {

    String groupId,memberId;
    LinearLayout mLayout;
    boolean filtering;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_and_order);

        groupId=getIntent().getExtras().getString("groupId");
        memberId=getIntent().getExtras().getString("memberId");
        filtering=getIntent().getExtras().getBoolean("filtering",false);



        setTitle("Details");
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setUpView();
    }

    private void setUpView(){
        mLayout=findViewById(R.id.mLayout);

        if(filtering){
            long start_date=getIntent().getExtras().getLong("start_date");
            long end_date=getIntent().getExtras().getLong("end_date");

            FilteringChart filteringChart=new FilteringChart(groupId,memberId,start_date,end_date,this);
            mLayout.addView(filteringChart.getView());

        }else{
            TargetPlanAndOrderRate targetPlanAndOrderRate=new TargetPlanAndOrderRate(this,groupId,memberId);
            mLayout.addView(targetPlanAndOrderRate.getView());
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