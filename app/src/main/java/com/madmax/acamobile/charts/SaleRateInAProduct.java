package com.madmax.acamobile.charts;

import static com.madmax.acamobile.ProductLeftActivity.stocks;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import com.madmax.acamobile.R;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.app.Initializer;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.models.ProductModel;
import com.madmax.acamobile.models.StockModel;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Executor;

public class SaleRateInAProduct {

    LineChart lineChart;
    ProgressBar pb;

    String userId;
    private final LayoutInflater mInflater;
    private final Activity c;
    View view;
    Executor postExecutor;

    int currentYear,year;
    long start_time,final_time;

    Spinner sp_product,sp_year;
    TextView tv_dashboard;

    ArrayList<String> products=new ArrayList<>();
    ArrayList<Integer> productIds=new ArrayList<>();
    ArrayList<Integer> years=new ArrayList<>();
    ArrayList<Entry> sales=new ArrayList<>();
    ArrayList<Entry> orders=new ArrayList<>();

    int product_id;

    public SaleRateInAProduct(String userId, Activity c){
        this.userId = userId;
        this.c = c;
        this.mInflater= LayoutInflater.from(c);
        postExecutor= ContextCompat.getMainExecutor(c);

        setDefaultDate();
        initializeSpinnerArr();

        initializeView();
        fetchData();
    }

    private void initializeView(){
        view=mInflater.inflate(R.layout.sale_rate_in_a_product,null);
        lineChart=view.findViewById(R.id.lineChart);
        pb=view.findViewById(R.id.pb);
        sp_product=view.findViewById(R.id.sp_product);
        sp_year=view.findViewById(R.id.sp_year);
        tv_dashboard=view.findViewById(R.id.tv_dashboard);
        setSpinner();
    }


    private void fetchData(){
        lineChart.setVisibility(View.INVISIBLE);
        pb.setVisibility(View.VISIBLE);
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    Log.e("chartSaleRate ",response);
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            sales.clear();
                            orders.clear();
                            setUpChart(response);
                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    Log.e("ErrSaleRate ",msg);


                }
            }).url(Routing.CHART_SALE_RATE_IN_A_PRODUCT+"?user_id="+userId+"&start_date="+start_time+"&end_date="+final_time+"&product_id="+product_id);

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

            for(int i=0;i<12;i++){
                String month=(i+1)+"";

                int saleQty=0;
                int orderQty=0;

                if(joSale!=null){
                    if(joSale.has(month)){
                        int sale=joSale.getJSONObject(month).getInt("quantity");
                        int foc=joSale.getJSONObject(month).getInt("foc");

                        saleQty=sale+foc;
                        sales.add(new Entry(i,saleQty));
                    }else{
                        sales.add(new Entry(i,0));
                    }
                }else{
                    sales.add(new Entry(i,0));
                }

                if(joOrder!=null){
                    if(joOrder.has(month)){
                        int order=joOrder.getJSONObject(month).getInt("quantity");
                        int foc=joOrder.getJSONObject(month).getInt("foc");
                        orderQty=order+foc;
                        orders.add(new Entry(i,orderQty));
                    }else{
                        orders.add(new Entry(i,0));
                    }

                }else{
                    orders.add(new Entry(i,0));
                }

            }

            LineDataSet setComp1 = new LineDataSet(sales, "Sale");
            setComp1.setColor(Color.BLUE);
            setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);

            LineDataSet setComp2 = new LineDataSet(orders, "My Order");
            setComp2.setColor(Color.RED);
            setComp2.setAxisDependency(YAxis.AxisDependency.LEFT);

            // use the interface ILineDataSet
            List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(setComp2);
            dataSets.add(setComp1);


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



    public View getView() {
        return view;
    }

    private void setDefaultDate(){

        final_time=System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(final_time);
        currentYear=calendar.get(Calendar.YEAR);
        year=currentYear;
        calendar.set(currentYear,0,1,0,0,1);
        start_time=calendar.getTimeInMillis();


    }


    private void initializeSpinnerArr(){

        product_id=Initializer.products.get(0).getProduct_id();

        for(int i=0;i< Initializer.products.size();i++){
            ProductModel p=Initializer.products.get(i);
            products.add(p.getProduct_name());
            productIds.add(p.getProduct_id());
        }

        int temp=currentYear;
        for(int i=0;i<10;i++){
            years.add(temp);
            temp--;
        }
    }

    private void setSpinner(){

        ArrayAdapter<String> productAdapter=new ArrayAdapter<String>(c,R.layout.spinner_product,products);
        productAdapter.setDropDownViewResource(R.layout.spinner_product);
        sp_product.setAdapter(productAdapter);
        sp_product.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tv_dashboard.setText("Sale Rate In "+products.get(position));
                product_id=productIds.get(position);
                fetchData();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        ArrayAdapter<Integer> yearAdapter=new ArrayAdapter<Integer>(c, R.layout.spinner_product,years);
        yearAdapter.setDropDownViewResource(R.layout.spinner_product);
        sp_year.setAdapter(yearAdapter);
        sp_year.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                year=years.get(i);
                updateDuration(year);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private void updateDuration(int year){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(year,0,1,0,0,1);

        start_time=calendar.getTimeInMillis();
        Log.e("startTime ", AppUtils.formatTime(start_time));

        calendar.set(year,11,32,0,0,0);
        final_time=calendar.getTimeInMillis();

        Log.e("endTime",AppUtils.formatTime(final_time));

        fetchData();
    }
}
