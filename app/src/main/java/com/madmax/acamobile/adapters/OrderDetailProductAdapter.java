package com.madmax.acamobile.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.madmax.acamobile.DataUpdateActivity;
import com.madmax.acamobile.OrderDetailActivity;
import com.madmax.acamobile.R;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.app.Price;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.models.OrderModel;
import com.madmax.acamobile.models.Product;
import com.madmax.acamobile.models.ProductModel;
import com.madmax.acamobile.models.StockModel;
import java.util.ArrayList;
import java.util.concurrent.Executor;

public class OrderDetailProductAdapter extends RecyclerView.Adapter<OrderDetailProductAdapter.Holder> {

    boolean isSoldOut;
    private final Activity c;
    private StockModel stock;
    private final ArrayList<OrderModel> data;
    private final LayoutInflater mInflater;
    final SharedPreferences sharedPreferences;
    String voucherId;
    String userId,authToken;
    Executor postExecutor;



    public OrderDetailProductAdapter(Activity c, ArrayList<OrderModel> data, StockModel stock,boolean isSoldOut,String voucherId){
        this.data=data;
        this.c=c;
        this.voucherId=voucherId;
        this.mInflater= LayoutInflater.from(c);
        this.stock=stock;
        this.isSoldOut=isSoldOut;
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=sharedPreferences.getString("userId",null);
        authToken=sharedPreferences.getString("authToken",null);
        postExecutor= ContextCompat.getMainExecutor(c);

    }

    @NonNull
    @Override
    public OrderDetailProductAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=mInflater.inflate(R.layout.item_order_detail,parent,false);
        return new Holder(view);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull OrderDetailProductAdapter.Holder holder, int position) {

        OrderModel model=data.get(position);
        holder.tv_point.setText(model.getPoint()+"");
        holder.tv_price.setText(AppUtils.getTwoDecimalDouble((double)model.getPrice()));
        holder.tv_amount.setText(AppUtils.getTwoDecimalDouble(model.getAmount()));
        holder.tv_discount.setText(model.getDiscount()+"");
        holder.tv_quantity.setText(model.getQuantity()+"");
        holder.tv_foc.setText(model.getFoc()+"");

        ProductModel p=AppUtils.findProductById(model.getProduct_id());
        if(p!=null){
            holder.tv_product_name.setText(p.getProduct_name());
            holder.tv_retail.setText(AppUtils.getTwoDecimalDouble((double)p.getPrice(Price.retail_price)));
        }

        if(isSoldOut){
            holder.tv_left.setVisibility(View.GONE);
        }else{
            ArrayList<Product> products=stock.getProducts();
            for(int i=0;i<products.size();i++){
                Product product=products.get(i);
                int totalQty=model.getQuantity()+model.getFoc();
                if(product.getProduct_id()==model.getProduct_id()){
                    int left=product.getCount();
                    holder.tv_left.setText(left+"");
                    if(totalQty>left){
                        holder.mLayout.setBackgroundColor(c.getResources().getColor(R.color.warning_yellow));
                        OrderDetailActivity.isEnoughToSend=false;
                    }else{
                        holder.mLayout.setBackgroundColor(c.getResources().getColor(R.color.white));
                    }

                    break;
                }
            }
        }

    }

    public class Holder extends RecyclerView.ViewHolder{

        TextView tv_point,tv_price,tv_amount,tv_retail,tv_discount,tv_product_name,tv_quantity,
                tv_foc,tv_left;
        LinearLayout mLayout;

        public Holder(View view){
            super(view);
            tv_product_name=view.findViewById(R.id.tv_product_name);
            tv_point=view.findViewById(R.id.tv_point);
            tv_price=view.findViewById(R.id.tv_price);
            tv_amount=view.findViewById(R.id.tv_phone);
            tv_quantity=view.findViewById(R.id.tv_quantity);
            tv_foc=view.findViewById(R.id.tv_foc);
            tv_retail=view.findViewById(R.id.tv_retail);
            tv_discount=view.findViewById(R.id.tv_discount);
            tv_left=view.findViewById(R.id.tv_left);
            mLayout=view.findViewById(R.id.mLayout);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(OrderDetailActivity.priceEditable){

                        OrderModel model=data.get(getAdapterPosition());
                        ProductModel p=AppUtils.findProductById(model.getProduct_id());
                        Intent intent=new Intent(c, DataUpdateActivity.class);
                        intent.putExtra("hint","Enter price");
                        assert p != null;
                        intent.putExtra("message","Update price for "+p.getProduct_name());
                        intent.putExtra("key","price");
                        intent.putExtra("extra1",model.getProduct_id()+"");
                        intent.putExtra("contentId",voucherId);
                        intent.putExtra("url", Routing.UPDATE_DETAIL_PRICE);

                        c.startActivity(intent);


                    }else{
                        Toast.makeText(c,"Price cannot edit",Toast.LENGTH_SHORT).show();
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

    public void setStock(StockModel stock) {
        this.stock = stock;
    }
}
