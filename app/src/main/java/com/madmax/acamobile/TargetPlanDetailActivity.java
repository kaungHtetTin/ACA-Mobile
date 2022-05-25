package com.madmax.acamobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.madmax.acamobile.adapters.TargetPlanProductAdapter;
import com.madmax.acamobile.app.Initializer;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.models.ProductModel;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

public class TargetPlanDetailActivity extends AppCompatActivity {

    int targetPlanId;
    long startDate,endDate;
    RecyclerView recyclerView;
    LineChart lineChart;
    RelativeLayout chart_layout;
    ProgressBar pb;
    Executor postExecutor;
    ArrayList<Entry> chartProducts=new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target_plan_detail);
        postExecutor= ContextCompat.getMainExecutor(this);

        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        targetPlanId=getIntent().getExtras().getInt("targetPlanID");
        startDate=getIntent().getExtras().getLong("startDate");
        endDate=getIntent().getExtras().getLong("endDate");

        setTitle("Details");
        setUpView();
    }

    private void setUpView(){
        chart_layout=findViewById(R.id.chart_layout);
        pb=findViewById(R.id.pb);
        lineChart=findViewById(R.id.lineChart);
        recyclerView=findViewById(R.id.recyclerView);
        GridLayoutManager gm=new GridLayoutManager(this,3);
        recyclerView.setLayoutManager(gm);
        TargetPlanProductAdapter adapter=new TargetPlanProductAdapter(this, Initializer.products,targetPlanId);
        recyclerView.setAdapter(adapter);

    }

    @Override
    protected void onResume() {
        fetchPlanProduct();
        super.onResume();
    }

    private void fetchPlanProduct(){
        chart_layout.setVisibility(View.GONE);
        pb.setVisibility(View.VISIBLE);
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {

                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            chartProducts.clear();
                            setUpChart(response);
                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    Log.e("ErrOrders ",msg);


                }
            }).url(Routing.GET_TARGET_PLAN_DETAILS+"?target_plan_id="+targetPlanId);

            myHttp.runTask();
        }).start();
    }

    private void setUpChart(String response){
        try {
            int totalCount=0;
            JSONObject jo=new JSONObject(response);
            for(int i=0;i<Initializer.products.size();i++){
                ProductModel model=Initializer.products.get(i);
                String productId=model.getProduct_id()+"";
                if(jo.has(productId)){
                    int count=jo.getJSONObject(productId).getInt("count");
                    chartProducts.add(new Entry(i,count));
                    int temp=count/model.getPack();
                    totalCount+=temp;
                }else{
                    chartProducts.add(new Entry(i,0));
                }
            }


            LineDataSet setComp1 = new LineDataSet(chartProducts, "Target Plan");
            setComp1.setColor(Color.RED);
            setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);

            LineData data = new LineData(setComp1);
            lineChart.setData(data);
            lineChart.getDescription().setText(totalCount+" price items");
            lineChart.invalidate(); // refresh

            // the labels that should be drawn on the XAxis

            ValueFormatter formatter = new ValueFormatter() {
                @Override
                public String getAxisLabel(float value, AxisBase axis) {
                    return Initializer.products.get((int) value).getProduct_name();
                }
            };
            XAxis xAxis = lineChart.getXAxis();
            xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
            xAxis.setValueFormatter(formatter);

            pb.setVisibility(View.GONE);
            chart_layout.setVisibility(View.VISIBLE);

        }catch (Exception e){
            Log.e("TargetCharErr ",e.toString());
            pb.setVisibility(View.GONE);
            chart_layout.setVisibility(View.VISIBLE);

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