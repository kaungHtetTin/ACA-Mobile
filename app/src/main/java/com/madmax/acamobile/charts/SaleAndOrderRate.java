package com.madmax.acamobile.charts;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.madmax.acamobile.R;
import com.madmax.acamobile.StatisticalChartActivity;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.app.Initializer;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.models.ProductModel;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Executor;

public class SaleAndOrderRate{
    TextView tv_from,tv_to,tv_see_more;
    LineChart lineChart;
    ProgressBar pb;

    long start_time,final_time;

    ArrayList<Entry> saleProducts=new ArrayList<>();
    ArrayList<Entry> orderProducts=new ArrayList<>();
    ArrayList<Entry> profits=new ArrayList<>();

    private final LayoutInflater mInflater;
    private final Activity c;
    View view;
    String userId;
    FragmentManager fragmentManager;
    Executor postExecutor;
    String url;
    boolean group;

    public SaleAndOrderRate(Activity c,FragmentManager fragmentManager,String userId,String url,boolean group) {
        this.c = c;
        this.fragmentManager=fragmentManager;
        this.userId=userId;
        this.mInflater= LayoutInflater.from(c);
        postExecutor= ContextCompat.getMainExecutor(c);
        this.url=url;
        this.group=group;
        initializeView();
    }

    private void initializeView(){
        view=mInflater.inflate(R.layout.chart_sale_and_order_rate,null);
        tv_from=view.findViewById(R.id.tv_from);
        tv_to=view.findViewById(R.id.tv_to);
        lineChart=view.findViewById(R.id.lineChart);
        pb=view.findViewById(R.id.pb);
        tv_see_more=view.findViewById(R.id.tv_see_more);
        setUpChartView();
        fetchStatisticData();
    }

    private void setUpChartView(){

        setDefaultDate();
        tv_from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialDatePicker.Builder<Long> builder=MaterialDatePicker.Builder.datePicker();
                builder.setTitleText("Select An Initial Date");
                MaterialDatePicker<Long> materialDatePicker=builder.build();
                materialDatePicker.show(fragmentManager, System.currentTimeMillis()+"");

                materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                    @Override
                    public void onPositiveButtonClick(Long selection) {
                        start_time=selection;

                        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        calendar.setTimeInMillis(selection);
                        int day=calendar.get(Calendar.DAY_OF_MONTH);
                        int month=calendar.get(Calendar.MONTH);
                        int year=calendar.get(Calendar.YEAR);

                        tv_from.setText(AppUtils.month[month]+" "+day+", "+year);
                        fetchStatisticData();

                    }
                });
            }
        });

        tv_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialDatePicker.Builder<Long> builder2=MaterialDatePicker.Builder.datePicker();
                builder2.setTitleText("Select A Final Date");
                MaterialDatePicker<Long> materialDatePickerTarget=builder2.build();

                materialDatePickerTarget.show(fragmentManager,System.currentTimeMillis()+"");
                materialDatePickerTarget.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                    @Override
                    public void onPositiveButtonClick(Long selection) {

                        final_time=selection;

                        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        calendar.setTimeInMillis(selection);
                        int day=calendar.get(Calendar.DAY_OF_MONTH);
                        int month=calendar.get(Calendar.MONTH);
                        int year=calendar.get(Calendar.YEAR);

                        tv_to.setText(AppUtils.month[month]+" "+day+", "+year);
                        fetchStatisticData();
                    }
                });
            }
        });

        tv_see_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                c.startActivity(new Intent(c, StatisticalChartActivity.class));
            }
        });


    }


    private void fetchStatisticData(){
        lineChart.setVisibility(View.INVISIBLE);
        pb.setVisibility(View.VISIBLE);
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    Log.e("SaleAOrderRate ",response);
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            saleProducts.clear();
                            orderProducts.clear();
                            setUpChart(response);
                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    Log.e("ErrOrders ",msg);


                }
            }).url(url+"user_id="+userId+"&start_date="+start_time+"&end_date="+final_time);

            myHttp.runTask();
        }).start();
    }

    private void setUpChart(String response) {
        try {

            JSONObject joMain = new JSONObject(response);
            JSONObject joSale=null;
            JSONObject joOrder=null;
            if(joMain.has("orders")){
                joOrder=joMain.getJSONObject("orders");
            }

            if(joMain.has("sales")){
                joSale=joMain.getJSONObject("sales");
            }


            for (int i = 0; i < Initializer.products.size(); i++) {
                ProductModel model = Initializer.products.get(i);
                String productId = model.getProduct_id() + "";

                int saleAmount=0;
                int investment=0;

                if(joSale!=null){
                    if(joSale.has(productId)){
                        int count=joSale.getJSONObject(productId).getInt("count");
                        saleAmount=joSale.getJSONObject(productId).getInt("amount");
                        saleProducts.add(new Entry(i,count));
                    }else{
                        saleProducts.add(new Entry(i,0));
                    }
                }else{
                    saleProducts.add(new Entry(i,0));
                }

                if(joOrder!=null){
                    if (joOrder.has(productId)) {
                        int count = joOrder.getJSONObject(productId).getInt("count");
                        investment=joOrder.getJSONObject(productId).getInt("amount");
                        orderProducts.add(new Entry(i, count));
                    } else {
                        orderProducts.add(new Entry(i, 0));
                    }
                }else{
                    orderProducts.add(new Entry(i, 0));
                }

                int profit=saleAmount-investment;
                if(profit>0)profits.add(new Entry(i,profit));
                else profits.add(new Entry(i,0));
            }


            LineDataSet setComp1 = new LineDataSet(saleProducts, "Sale Rate");
            setComp1.setColor(Color.GREEN);
            setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);

            LineDataSet setComp2 = new LineDataSet(orderProducts, "My Order Rate");
            setComp2.setColor(Color.BLUE);
            setComp2.setAxisDependency(YAxis.AxisDependency.LEFT);

            // use the interface ILineDataSet
            List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(setComp1);
            if(!group)dataSets.add(setComp2);


            LineData data = new LineData(dataSets);
            lineChart.setData(data);
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
            lineChart.setVisibility(View.VISIBLE);
            if(group) lineChart.getDescription().setText("Sale");
            else  lineChart.getDescription().setText("Sale Vs Order");

        } catch (Exception e) {
            Log.e("TargetCharErr ", e.toString());
            pb.setVisibility(View.GONE);
            lineChart.setVisibility(View.VISIBLE);

        }
    }

    private void setDefaultDate(){

        final_time=System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(final_time);
        int day=calendar.get(Calendar.DAY_OF_MONTH);
        int month=calendar.get(Calendar.MONTH);
        int year=calendar.get(Calendar.YEAR);

        calendar.set(year,month,1,0,0,1);

        start_time=calendar.getTimeInMillis();

        tv_from.setText(AppUtils.month[month]+" "+1+", "+year);
        tv_to.setText(AppUtils.month[month]+" "+day+", "+year);
    }



    public View getView() {
        return view;
    }

    public void invisibleTVSeeMore(){
        this.tv_see_more.setVisibility(View.GONE);
    }


}
