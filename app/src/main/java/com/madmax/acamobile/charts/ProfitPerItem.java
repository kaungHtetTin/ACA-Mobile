package com.madmax.acamobile.charts;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
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

public class ProfitPerItem {
    TextView tv_from,tv_to;
    TextView tv_total_investment,tv_total_sale,tv_total_cost,tv_total_profit;
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

    public  ProfitPerItem(Activity c,FragmentManager fragmentManager,String userId){
        this.c = c;
        this.fragmentManager=fragmentManager;
        this.userId=userId;
        this.mInflater= LayoutInflater.from(c);
        postExecutor= ContextCompat.getMainExecutor(c);
        initializeView();
    }

    private void initializeView(){
        view=mInflater.inflate(R.layout.chart_profit_on_each_item_per_month,null);
        tv_from=view.findViewById(R.id.tv_from);
        tv_to=view.findViewById(R.id.tv_to);

        tv_total_cost=view.findViewById(R.id.tv_total_cost);
        tv_total_sale=view.findViewById(R.id.tv_total_sale);
        tv_total_investment=view.findViewById(R.id.tv_total_investment);
        tv_total_profit=view.findViewById(R.id.tv_total_profit);

        lineChart=view.findViewById(R.id.lineChart);
        pb=view.findViewById(R.id.pb);
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
    }


    private void fetchStatisticData(){
        lineChart.setVisibility(View.INVISIBLE);
        pb.setVisibility(View.VISIBLE);
        tv_total_profit.setText("0");
        tv_total_investment.setText("0");
        tv_total_cost.setText("0");
        tv_total_sale.setText("0");

        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            saleProducts.clear();
                            orderProducts.clear();
                            profits.clear();
                            setUpChart(response);
                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    Log.e("ErrOrders ",msg);


                }
            }).url(Routing.CHART_SALE_AND_ORDER+"?user_id="+userId+"&start_date="+start_time+"&end_date="+final_time);

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


            int total_investment=0,total_sale=0,total_cost=0,total_profit=0;

            for (int i = 0; i < Initializer.products.size(); i++) {
                ProductModel model = Initializer.products.get(i);
                String productId = model.getProduct_id() + "";

                int saleAmount=0;
                int investment=0;

                if(joSale!=null){
                    if(joSale.has(productId)){
                      //  int count=joSale.getJSONObject(productId).getInt("count");
                        saleAmount=joSale.getJSONObject(productId).getInt("amount");
                        saleProducts.add(new Entry(i,saleAmount));
                    }else{
                        saleProducts.add(new Entry(i,0));
                    }
                }else{
                    saleProducts.add(new Entry(i,0));
                }

                if(joOrder!=null){
                    if (joOrder.has(productId)) {
                      //  int count = joOrder.getJSONObject(productId).getInt("count");
                        investment=joOrder.getJSONObject(productId).getInt("amount");
                        orderProducts.add(new Entry(i, investment));
                    } else {
                        orderProducts.add(new Entry(i, 0));
                    }
                }else{
                    orderProducts.add(new Entry(i, 0));
                }

                int profit=saleAmount-investment;
                if(profit>0)profits.add(new Entry(i,profit));
                else profits.add(new Entry(i,0));

                total_investment+=investment;
                total_sale+=saleAmount;

            }


            total_cost=joMain.getInt("extra_cost");

            total_profit=total_sale-total_investment-total_cost;

            if(total_profit<0)total_profit=0;

            tv_total_profit.setText(total_profit+"");
            tv_total_investment.setText(total_investment+"");
            tv_total_cost.setText(total_cost+"");
            tv_total_sale.setText(total_sale+"");


            LineDataSet setComp1 = new LineDataSet(profits, "Profit");
            setComp1.setColor(Color.GREEN);
            setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);

            LineDataSet setComp2 = new LineDataSet(saleProducts, "Sale");
            setComp2.setColor(Color.BLUE);
            setComp2.setAxisDependency(YAxis.AxisDependency.LEFT);

            LineDataSet setComp3 = new LineDataSet(orderProducts, "Investment");
            setComp3.setColor(Color.RED);
            setComp3.setAxisDependency(YAxis.AxisDependency.LEFT);

            // use the interface ILineDataSet
            List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();

            dataSets.add(setComp2);
            dataSets.add(setComp3);
            dataSets.add(setComp1);


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

            YAxis yAxisLeft=lineChart.getAxisLeft();
            YAxis yAxisRight=lineChart.getAxisRight();

            yAxisLeft.setDrawLabels(false);
            yAxisRight.setDrawLabels(false);

            pb.setVisibility(View.GONE);
            lineChart.setVisibility(View.VISIBLE);
            lineChart.getDescription().setText("Profit On Item");

        } catch (Exception e) {

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

}
