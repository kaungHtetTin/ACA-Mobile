package com.madmax.acamobile.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.madmax.acamobile.ProductLeftActivity;
import com.madmax.acamobile.R;
import com.madmax.acamobile.StockProductUpdateActivity;
import com.madmax.acamobile.models.Product;
import com.madmax.acamobile.models.StockModel;
import java.util.ArrayList;


public class StockProductAdapter extends RecyclerView.Adapter<StockProductAdapter.Holder> {

    private final Activity c;
    private final ArrayList<StockModel> data;
    private final LayoutInflater mInflater;

    public StockProductAdapter(Activity c, ArrayList<StockModel> data){
        this.data=data;
        this.c=c;
        this.mInflater= LayoutInflater.from(c);

    }


    @NonNull
    @Override
    public StockProductAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=mInflater.inflate(R.layout.item_stock_product_left_container,parent,false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StockProductAdapter.Holder holder, int position) {

        try{

            StockModel stock;
            if(position==0){
                stock=data.get(position+1);
                holder.tv.setBackgroundResource(R.drawable.bg_noti_blue_table_stoke);
                holder.tv.setText("Stocks");
                holder.tv.setGravity(Gravity.CENTER);
                holder.tv.setTextColor(c.getResources().getColor(R.color.my_text_color));
            }
            else{
                stock=data.get(position);
                holder.tv.setText(stock.getName());
                if (position!=data.size()-1){
                    holder.mLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent=new Intent(c,StockProductUpdateActivity.class);
                            intent.putExtra("stock_id",stock.getStock_id()+"");
                            intent.putExtra("stock_name",stock.getName());
                            c.startActivity(intent);

                        }
                    });
                }else{
                    holder.tv.setBackgroundResource(R.drawable.bg_stroke_green);
                    holder.tv.setTextColor(Color.BLACK);
                }
            }

            holder.mLayout.removeAllViews();

            ArrayList<Product> products=stock.getProducts();
            for(int i=0;i<products.size();i++){
                Product product=products.get(i);

                String product_name=product.getProduct_name();
                int count=product.getCount();
                TextView textView = new TextView(c);
                if(position==0){
                    textView.setText(product_name);
                    textView.setTypeface(null,Typeface.BOLD);
                    textView.setBackgroundResource(R.drawable.bg_noti_blue_table_stoke);
                }
                else {
                    textView.setText(count+"");
                    textView.setBackgroundResource(R.drawable.bg_price_stroke);
                }
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        300,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );

                textView.setLayoutParams(params);
                textView.setGravity(Gravity.CENTER);
                textView.setTextColor(c.getResources().getColor(R.color.my_text_color));

                if(position==data.size()-1){
                    textView.setBackgroundResource(R.drawable.bg_stroke_green);
                    textView.setTextColor(Color.BLACK);
                }
                holder.mLayout.addView(textView);

            }


        }catch (Exception e){
            Toast.makeText(c,e.toString()+"stock product adapter",Toast.LENGTH_SHORT).show();
        }

    }

    public class Holder extends RecyclerView.ViewHolder{

        TextView tv;
        LinearLayout mLayout;

        public Holder(View view){
            super(view);
            tv=view.findViewById(R.id.tv_stock_title);
            mLayout=view.findViewById(R.id.stockLayout);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

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
}
