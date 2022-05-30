package com.madmax.acamobile.adapters;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.madmax.acamobile.BrandListActivity;
import com.madmax.acamobile.BusinessActivity;
import com.madmax.acamobile.CreateOrderActivity;
import com.madmax.acamobile.MainActivity;
import com.madmax.acamobile.MyGroupActivity;
import com.madmax.acamobile.PartnerGroupDetailActivity;
import com.madmax.acamobile.PartnerGroupListActivity;
import com.madmax.acamobile.ProductLeftActivity;
import com.madmax.acamobile.ProductListActivity;
import com.madmax.acamobile.R;
import com.madmax.acamobile.StockListActivity;
import com.madmax.acamobile.VoucherActivity;
import com.madmax.acamobile.WebViewActivity;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.models.PanelModel;
import java.util.ArrayList;
import java.util.concurrent.Executor;


public class MainPanelAdapter extends RecyclerView.Adapter<MainPanelAdapter.Holder> {

    private final Activity c;
    private final ArrayList<PanelModel> data;
    private final LayoutInflater mInflater;
    final SharedPreferences sharedPreferences;
    String userId,authToken;
    Executor postExecutor;
    boolean verified;
    long valid_date;

    public MainPanelAdapter(Activity c, ArrayList<PanelModel> data){
        this.data=data;
        this.c=c;
        this.mInflater= LayoutInflater.from(c);
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        verified=sharedPreferences.getBoolean("verified",false);
        userId=sharedPreferences.getString("userId",null);
        authToken=sharedPreferences.getString("authToken",null);
        postExecutor= ContextCompat.getMainExecutor(c);
        valid_date=sharedPreferences.getLong("valid_date",0);
        valid_date=valid_date*1000;

    }


    @NonNull
    @Override
    public MainPanelAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=mInflater.inflate(R.layout.item_main_panel,parent,false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MainPanelAdapter.Holder holder, int position) {

        try{
            PanelModel model=data.get(position);
            holder.tv.setText(model.getTitle());

            AppUtils.setPhotoFromRealUrl(holder.iv,model.getSrc());

        }catch (Exception e){
            Toast.makeText(c,e.toString(),Toast.LENGTH_SHORT).show();
        }

    }

    public class Holder extends RecyclerView.ViewHolder{

        ImageView iv;
        TextView tv;


        public Holder(View view){
            super(view);
            iv=view.findViewById(R.id.iv_panel);
            tv=view.findViewById(R.id.tv_panel);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(valid_date<System.currentTimeMillis()){
                        Intent intent=new Intent(c, WebViewActivity.class);
                        intent.putExtra("link", Routing.DOMAIN+"acamobile/register.php");
                        c.startActivity(intent);
                        return;
                    }


                    PanelModel model=data.get(getAdapterPosition());
                    switch (model.getId()){
                        case 1:
                            if(verified){
                                Intent intent1=new Intent(c, BrandListActivity.class);
                                intent1.putExtra("key","create_order");
                                c.startActivity(intent1);
                            }else{
                                Toast.makeText(c,"Your account is not verified by your leader",Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case 2:
                            if(verified){
                                Intent intent2=new Intent(c, BrandListActivity.class);
                                intent2.putExtra("key","voucher");
                                c.startActivity(intent2);
                            }else{
                                Toast.makeText(c,"Your account is not verified by your leader",Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case 3:
                            Intent intent3=new Intent(c, ProductLeftActivity.class);
                            c.startActivity(intent3);
                            break;

                        case 6:
                            Intent intent6=new Intent(c, MyGroupActivity.class);
                            c.startActivity(intent6);
                            break;
                        case 7:
                            Intent intent7=new Intent(c, PartnerGroupListActivity.class);
                            c.startActivity(intent7);
                            break;
                        case 8:
                            Intent intent8=new Intent(c, BusinessActivity.class);
                            c.startActivity(intent8);
                            break;
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void calculate(){


    }
}
