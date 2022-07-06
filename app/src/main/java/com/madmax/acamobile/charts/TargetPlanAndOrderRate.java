package com.madmax.acamobile.charts;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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

public class TargetPlanAndOrderRate {
    LineChart lineChart;
    ProgressBar pb;
    TextView tv_to,tv_from,tv_target_point,tv_rewarded_point;

    String groupId,memberId;
    Executor postExecutor;

    ArrayList<Entry> targetProducts=new ArrayList<>();
    ArrayList<Entry> orderProducts=new ArrayList<>();

    Activity c;
    private final LayoutInflater mInflater;
    View view;

    public TargetPlanAndOrderRate(Activity c,String groupId,String memberId){
        this.groupId=groupId;
        this.memberId=memberId;
        this.c=c;

        this.mInflater= LayoutInflater.from(c);
        postExecutor= ContextCompat.getMainExecutor(c);

        initializeView();

    }

    private void initializeView(){
        view=mInflater.inflate(R.layout.chart_targetplan_and_order,null);
        tv_from=view.findViewById(R.id.tv_from);
        tv_to=view.findViewById(R.id.tv_to);
        tv_target_point=view.findViewById(R.id.tv_total_target_point);
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

                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            targetProducts.clear();
                            orderProducts.clear();
                            setUpChart(response);
                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    Log.e("ErrOrders ",msg);


                }
            }).url(Routing.COMPARE_TARGET_PLAN_AND_ORDER_RATE+"?group_id="+groupId+"&member_id="+memberId);

            myHttp.runTask();
        }).start();
    }

    private void setUpChart(String response) {
        try {

            JSONObject joMain = new JSONObject(response);
            JSONObject joTarget = joMain.getJSONObject("target_plan_detail");
            JSONObject joOrder=null;
            if(joMain.has("sale_detail")){
                joOrder=joMain.getJSONObject("sale_detail");
            }

            JSONObject joPlan=joMain.getJSONObject("target_plan");
            setUpPlanRange(joPlan);

            float total_target_point=0;
            float total_rewarded_point=0;

            for (int i = 0; i < Initializer.products.size(); i++) {
                ProductModel model = Initializer.products.get(i);
                String productId = model.getProduct_id() + "";

                if (joTarget.has(productId)) {
                    int count = joTarget.getJSONObject(productId).getInt("count");
                    float point=model.getPoint()*count;
                    total_target_point+=point;
                    targetProducts.add(new Entry(i, count));

                } else {
                    targetProducts.add(new Entry(i, 0));
                }

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
            tv_target_point.setText(total_target_point+"");

            LineDataSet setComp1 = new LineDataSet(targetProducts, "Target Plan");
            setComp1.setColor(Color.RED);
            setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);

            LineDataSet setComp2 = new LineDataSet(orderProducts, "Sale Rate");
            setComp2.setColor(Color.GREEN);
            setComp2.setAxisDependency(YAxis.AxisDependency.LEFT);

            // use the interface ILineDataSet
            List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(setComp1);
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
            lineChart.getDescription().setText("");

        } catch (Exception e) {
            Log.e("TargetCharErr ", e.toString());
            pb.setVisibility(View.GONE);
            lineChart.setVisibility(View.VISIBLE);

        }
    }

    private void setUpPlanRange (JSONObject jo) throws JSONException {
        long startDate=jo.getLong("start_date")*1000;
        long endDate=jo.getLong("end_date")*1000;

        Calendar startCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        startCalendar.setTimeInMillis(startDate);
        int startDay=startCalendar.get(Calendar.DAY_OF_MONTH);
        int startMonth=startCalendar.get(Calendar.MONTH);
        int startYear=startCalendar.get(Calendar.YEAR);

        Calendar endCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        endCalendar.setTimeInMillis(endDate);
        int endDay=endCalendar.get(Calendar.DAY_OF_MONTH);
        int endMonth=endCalendar.get(Calendar.MONTH);
        int endYear=endCalendar.get(Calendar.YEAR);

        tv_from.setText(AppUtils.month[startMonth] +" "+startDay+", "+startYear);
        tv_to.setText(AppUtils.month[endMonth]+" "+endDay+", "+endYear);

    }

    public View getView() {
        return view;
    }
}
