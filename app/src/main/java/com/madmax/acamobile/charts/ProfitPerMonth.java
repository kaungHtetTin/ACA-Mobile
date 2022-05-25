package com.madmax.acamobile.charts;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.renderer.YAxisRenderer;
import com.madmax.acamobile.R;
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

public class ProfitPerMonth {

    long start_time,final_time;
    String userId;
    private final LayoutInflater mInflater;
    private final Activity c;
    View view;
    Executor postExecutor;

    int currentYear,year;
    TextView tv_year;
    LineChart lineChart;
    ProgressBar pb;
    ImageButton ibt_previousYear,ibt_nextYear;

    ArrayList<Entry> sales=new ArrayList<>();
    ArrayList<Entry> orders=new ArrayList<>();
    ArrayList<Entry> profits=new ArrayList<>();

    TextView tv_total_investment,tv_total_sale,tv_total_profit;

    public ProfitPerMonth(String userId, Activity c) {
        this.userId = userId;
        this.c = c;
        this.mInflater= LayoutInflater.from(c);
        postExecutor= ContextCompat.getMainExecutor(c);
        initializeView();
        fetchData();
    }

    private void initializeView(){
        view=mInflater.inflate(R.layout.chart_profit_per_month,null);
        lineChart=view.findViewById(R.id.lineChart);
        pb=view.findViewById(R.id.pb);
        tv_year=view.findViewById(R.id.tv_year);
        ibt_nextYear=view.findViewById(R.id.ibt_nextYear);
        ibt_previousYear=view.findViewById(R.id.ibt_previousYear);

        tv_total_sale=view.findViewById(R.id.tv_total_sale);
        tv_total_investment=view.findViewById(R.id.tv_total_investment);
        tv_total_profit=view.findViewById(R.id.tv_total_profit);

        setDefaultDate();

        ibt_previousYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ibt_nextYear.setVisibility(View.VISIBLE);
                year--;
                updateDuration(year);
                tv_year.setText(""+year);

                fetchData();
            }
        });

        ibt_nextYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                year++;
                updateDuration(year);
                tv_year.setText(""+year);
                if(year==currentYear){
                    tv_year.setText("Current Year");
                    ibt_nextYear.setVisibility(View.GONE);
                }

                fetchData();
            }
        });
    }

    private void fetchData(){
        lineChart.setVisibility(View.INVISIBLE);
        pb.setVisibility(View.VISIBLE);
        ibt_nextYear.setEnabled(false);
        ibt_previousYear.setEnabled(false);

        tv_total_profit.setText("0");
        tv_total_investment.setText("0");
        tv_total_sale.setText("0");

        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            sales.clear();
                            orders.clear();
                            profits.clear();
                            ibt_nextYear.setEnabled(true);
                            ibt_previousYear.setEnabled(true);

                            setUpChart(response);
                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    Log.e("ErrOrders ",msg);


                }
            }).url(Routing.CHART_PROFIT_PER_MONTH+"?user_id="+userId+"&start_date="+start_time+"&end_date="+final_time);

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

            int total_investment=0,total_sale=0,total_profit=0;

            for(int i=0;i<12;i++){
                String month=(i+1)+"";

                int saleAmount=0;
                int investment=0;

                if(joSale!=null){
                    if(joSale.has(month)){
                        int sale=joSale.getJSONObject(month).getInt("total_amount");
                        int cost=joSale.getJSONObject(month).getInt("admin_extra_cost");
                        saleAmount=sale-cost;
                        sales.add(new Entry(i,saleAmount));
                    }else{
                        sales.add(new Entry(i,0));
                    }
                }else{
                    sales.add(new Entry(i,0));
                }

                if(joOrder!=null){
                    if(joOrder.has(month)){
                        int order=joOrder.getJSONObject(month).getInt("total_amount");
                        int cost=joOrder.getJSONObject(month).getInt("agent_extra_cost");
                        investment=order+cost;
                        orders.add(new Entry(i,investment));
                    }else{
                        orders.add(new Entry(i,0));
                    }

                }else{
                    orders.add(new Entry(i,0));
                }
                int profit=saleAmount-investment;

                if(profit>0) profits.add(new Entry(i,profit));
                else profits.add(new Entry(i,0));

                total_investment+=investment;
                total_sale+=saleAmount;
                total_profit+=profit;


            }

            if(total_profit<0)total_profit=0;

            tv_total_profit.setText(total_profit+"");
            tv_total_investment.setText(total_investment+"");
            tv_total_sale.setText(total_sale+"");


            LineDataSet setComp1 = new LineDataSet(sales, "Sale");
            setComp1.setColor(Color.BLUE);
            setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);

            LineDataSet setComp2 = new LineDataSet(orders, "Investment");
            setComp2.setColor(Color.RED);
            setComp2.setAxisDependency(YAxis.AxisDependency.LEFT);

            LineDataSet setComp3 = new LineDataSet(profits, "Profit");
            setComp3.setColor(Color.GREEN);
            setComp3.setAxisDependency(YAxis.AxisDependency.LEFT);

            // use the interface ILineDataSet
            List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(setComp1);
            dataSets.add(setComp2);
            dataSets.add(setComp3);

            LineData data = new LineData(dataSets);
            lineChart.setData(data);
            lineChart.invalidate(); // refresh

            // the labels that should be drawn on the XAxis

            ValueFormatter formatter = new ValueFormatter() {
                @Override
                public String getAxisLabel(float value, AxisBase axis) {
                    return AppUtils.month[(int) value];
                }
            };
            XAxis xAxis = lineChart.getXAxis();
            xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
            xAxis.setValueFormatter(formatter);

            YAxis yAxisLeft=lineChart.getAxisLeft();
            YAxis yAxisRight=lineChart.getAxisRight();

            yAxisLeft.setDrawLabels(false);
            yAxisRight.setDrawLabels(false);

            pb.setVisibility(View.GONE);
            lineChart.setVisibility(View.VISIBLE);
            lineChart.getDescription().setText("Profit Chart");

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
        currentYear=calendar.get(Calendar.YEAR);
        year=currentYear;
        calendar.set(currentYear,0,1,0,0,1);

        start_time=calendar.getTimeInMillis();
        ibt_nextYear.setVisibility(View.GONE);
    }

    public View getView() {
        return view;
    }

    private void updateDuration(int year){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(year,0,1,0,0,1);

        start_time=calendar.getTimeInMillis();
        Log.e("startTime ", AppUtils.formatTime(start_time));

        calendar.set(year,11,32,0,0,0);
        final_time=calendar.getTimeInMillis();

        Log.e("endTime",AppUtils.formatTime(final_time));


    }
}
