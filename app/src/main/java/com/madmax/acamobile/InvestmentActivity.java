package com.madmax.acamobile;

import static com.madmax.acamobile.ProductLeftActivity.stocks;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.madmax.acamobile.adapters.InvestmentAdapter;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.app.Initializer;
import com.madmax.acamobile.app.MyHttp;
import com.madmax.acamobile.app.Price;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.models.InvestmentModel;
import com.madmax.acamobile.models.ProductModel;
import com.madmax.acamobile.models.StockModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executor;

public class InvestmentActivity extends AppCompatActivity {

    Spinner sp_prices;
    LinearLayout investmentLayout;
    TextView tv_total_amount;
    RecyclerView recyclerView;
    ProgressBar pb;

    InvestmentAdapter adapter;
    ArrayList<InvestmentModel> investments=new ArrayList<>();
    Executor postExecutor;

    SharedPreferences sharedPreferences;
    String userId;
    int priceIndex=0;
    String investmentJSON=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_investment);
        postExecutor= ContextCompat.getMainExecutor(this);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=sharedPreferences.getString("userId",null);
        setUpView();
    }


    private void setUpView(){
        pb=findViewById(R.id.pb);
        sp_prices=findViewById(R.id.sp_prices);
        investmentLayout=findViewById(R.id.investment_layout);
        tv_total_amount=findViewById(R.id.tv_total_amount);
        recyclerView=findViewById(R.id.recyclerView);

        LinearLayoutManager lm=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lm);
        adapter=new InvestmentAdapter(this,investments);
        recyclerView.setAdapter(adapter);

        investmentLayout.setVisibility(View.GONE);
        setPriceSpinner();
        fetchInvestment();
    }


    private void fetchInvestment(){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            pb.setVisibility(View.GONE);
                            investmentLayout.setVisibility(View.VISIBLE);
                            Log.e("Investment",response);

                            setResult(response,priceIndex);

                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    Log.e("ErrOrders ",msg);
                    pb.setVisibility(View.GONE);

                }
            }).url(Routing.GET_INVESTMENT+"?owner_id="+userId);

            myHttp.runTask();
        }).start();
    }

    private void setResult(String response,int priceIndex){
        investments.clear();
        int totalAmount=0;
        try {
            JSONArray ja=new JSONArray(response);
            for(int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                int product_id=jo.getInt("product_id");
                int quantity=jo.getInt("total");
                ProductModel p=AppUtils.findProductById(product_id);
                int amount=(int)(quantity*p.getPrice(priceIndex));
                totalAmount+=amount;
                investments.add(new InvestmentModel(p.getProduct_name(),product_id,quantity,amount));
            }
            adapter.notifyDataSetChanged();

            tv_total_amount.setText(AppUtils.getTwoDecimalDouble((double)totalAmount));
        }catch (Exception e){

        }
    }



    private void setPriceSpinner(){
        ArrayAdapter<String> stockAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, Price.priceTypes);
        sp_prices.setAdapter(stockAdapter);
        sp_prices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                priceIndex=position;

                fetchInvestment();

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }


}