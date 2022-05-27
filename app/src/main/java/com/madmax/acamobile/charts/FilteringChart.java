package com.madmax.acamobile.charts;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import com.madmax.acamobile.R;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.app.Initializer;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.models.ProductModel;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Executor;

public class FilteringChart {

    LineChart lineChart;
    ProgressBar pb;
    TextView tv_to,tv_from,tv_rewarded_point;

    String groupId,memberId;
    Executor postExecutor;

    ArrayList<Entry> orderProducts=new ArrayList<>();

    long start_date,end_date;
    Activity c;
    private final LayoutInflater mInflater;
    View view;

    public FilteringChart(String groupId, String memberId, long start_date, long end_date, Activity c) {
        this.groupId = groupId;
        this.memberId = memberId;
        this.start_date = start_date;
        this.end_date = end_date;
        this.c = c;

        this.mInflater= LayoutInflater.from(c);
        postExecutor= ContextCompat.getMainExecutor(c);

        initializeView();
    }

    private void initializeView(){
        view=mInflater.inflate(R.layout.chart_filtering,null);
        tv_from=view.findViewById(R.id.tv_from);
        tv_to=view.findViewById(R.id.tv_to);
        tv_rewarded_point=view.findViewById(R.id.tv_total_rewarded_point);

        lineChart=view.findViewById(R.id.lineChart);
        pb=view.findViewById(R.id.pb);

        fetchPlanAndOrderRate();
    }

    private void fetchPlanAndOrderRate(){
        lineChart.setVisibility(View.INVISIBLE);
        pb.setVisibility(View.VISIBLE);
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {

                    Log.e("filtering ",response);
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            orderProducts.clear();
                            setUpPlanRange();
                            setUpChart(response);
                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    Log.e("ErrOrders ",msg);


                }
            }).url(Routing.CHART_GROUP_FILTER_ORDER_RATE+"?group_id="+groupId+"&member_id="+memberId+"&start_date="+start_date+"&end_date="+end_date);

            myHttp.runTask();
        }).start();
    }

    private void setUpChart(String response) {
        try {

            JSONObject joMain = new JSONObject(response);
            JSONObject joOrder=null;
            if(joMain.has("orders")){
                joOrder=joMain.getJSONObject("orders");
            }

            float total_rewarded_point=0;

            for (int i = 0; i < Initializer.products.size(); i++) {
                ProductModel model = Initializer.products.get(i);
                String productId = model.getProduct_id() + "";

                if(joOrder!=null){
                    if (joOrder.has(productId)) {
                        int count = joOrder.getJSONObject(productId).getInt("count");
                        float point=model.getPoint()*count;
                        total_rewarded_point+=point;
                        orderProducts.add(new Entry(i, count));
                    } else {
                        orderProducts.add(new Entry(i, 0));
                    }
                }else{
                    orderProducts.add(new Entry(i, 0));
                }
            }


            tv_rewarded_point.setText(total_rewarded_point+"");

            LineDataSet setComp2 = new LineDataSet(orderProducts, "Order Rate");
            setComp2.setColor(Color.GREEN);
            setComp2.setAxisDependency(YAxis.AxisDependency.LEFT);

            // use the interface ILineDataSet
            List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(setComp2);

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

        } catch (Exception e) {
            Log.e("TargetCharErr ", e.toString());
            pb.setVisibility(View.GONE);
            lineChart.setVisibility(View.VISIBLE);

        }
    }
    public View getView() {
        return view;
    }

    private void setUpPlanRange () {


        Calendar startCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        if(start_date==0){
            tv_from.setText("At beginning");
        }else{
            startCalendar.setTimeInMillis(start_date);
            int startDay=startCalendar.get(Calendar.DAY_OF_MONTH);
            int startMonth=startCalendar.get(Calendar.MONTH);
            int startYear=startCalendar.get(Calendar.YEAR);
            tv_from.setText(AppUtils.month[startMonth] +" "+startDay+", "+startYear);
        }

        Calendar endCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        endCalendar.setTimeInMillis(end_date);
        int endDay=endCalendar.get(Calendar.DAY_OF_MONTH);
        int endMonth=endCalendar.get(Calendar.MONTH);
        int endYear=endCalendar.get(Calendar.YEAR);


        tv_to.setText(AppUtils.month[endMonth]+" "+endDay+", "+endYear);

    }
}
