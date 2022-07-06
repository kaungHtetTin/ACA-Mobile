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

public class RetailAndAgentRate {

    TextView tv_from,tv_to;
    TextView tv_retail_amount,tv_retail_percent,tv_wholesale_amount,tv_wholesale_percent;
    LineChart lineChart;
    ProgressBar pb;

    long start_time,final_time;

    ArrayList<Entry> retails=new ArrayList<>();
    ArrayList<Entry> agents=new ArrayList<>();

    private final LayoutInflater mInflater;
    private final Activity c;
    View view;
    String userId;
    FragmentManager fragmentManager;
    Executor postExecutor;

    public RetailAndAgentRate(Activity c, String userId, FragmentManager fragmentManager) {
        this.c = c;
        this.userId = userId;
        this.fragmentManager = fragmentManager;

        this.mInflater= LayoutInflater.from(c);
        postExecutor= ContextCompat.getMainExecutor(c);
        initializeView();
    }

    private void initializeView(){
        view=mInflater.inflate(R.layout.chart_retial_and_agent_rate,null);
        tv_from=view.findViewById(R.id.tv_from);
        tv_to=view.findViewById(R.id.tv_to);
        lineChart=view.findViewById(R.id.lineChart);
        pb=view.findViewById(R.id.pb);

        tv_retail_amount=view.findViewById(R.id.tv_retail_amount1);
        tv_retail_percent=view.findViewById(R.id.tv_retail_percent);
        tv_wholesale_amount=view.findViewById(R.id.tv_wholesale_amount1);
        tv_wholesale_percent=view.findViewById(R.id.tv_wholesale_percent);

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
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            retails.clear();
                            agents.clear();
                            setUpChart(response);
                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    Log.e("ErrOrders ",msg);


                }
            }).url(Routing.CHART_RETAIL_AND_ORDER+"?user_id="+userId+"&start_date="+start_time+"&end_date="+final_time);

            myHttp.runTask();
        }).start();
    }

    private void setUpChart(String response) {
        try {

            JSONObject joMain = new JSONObject(response);
            JSONObject joRetail=null;
            JSONObject joAgent=null;
            if(joMain.has("agents")){
                joAgent=joMain.getJSONObject("agents");
            }

            if(joMain.has("retails")){
                joRetail=joMain.getJSONObject("retails");
            }


            int totalRetail=0;
            int totalWholesale=0;
            for (int i = 0; i < Initializer.products.size(); i++) {
                ProductModel model = Initializer.products.get(i);
                String productId = model.getProduct_id() + "";

                if(joRetail!=null){
                    if(joRetail.has(productId)){
                        int count=joRetail.getJSONObject(productId).getInt("count");
                        totalRetail+=count;
                        retails.add(new Entry(i,count));
                    }else{
                        retails.add(new Entry(i,0));
                    }
                }else{
                    retails.add(new Entry(i,0));
                }

                if(joAgent!=null){
                    if (joAgent.has(productId)) {
                        int count = joAgent.getJSONObject(productId).getInt("count");
                        totalWholesale+=count;
                        agents.add(new Entry(i, count));
                    } else {
                        agents.add(new Entry(i, 0));
                    }
                }else{
                    agents.add(new Entry(i, 0));
                }
            }

            int total=totalRetail+totalWholesale;

            Log.e("Whosale Rate ", "Retail - "+totalRetail+" sale - "+totalWholesale);
            tv_retail_amount.setText(totalRetail+"");
            tv_wholesale_amount.setText(totalWholesale+"");

            if(total!=0){
                int retailPercent=totalRetail*(100/total);
                int wholesalePercent=100-retailPercent;
                tv_retail_percent.setText(retailPercent+"%");
                tv_wholesale_percent.setText(wholesalePercent+"%");
            }else{
                tv_retail_percent.setText("0 %");
                tv_wholesale_percent.setText("0 %");
            }

            LineDataSet setComp1 = new LineDataSet(retails, "Retail");
            setComp1.setColor(Color.GREEN);
            setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);

            LineDataSet setComp2 = new LineDataSet(agents, "Wholesale");
            setComp2.setColor(Color.BLUE);
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
            lineChart.getDescription().setText("Retail Vs Wholesale");

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
}
