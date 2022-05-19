package com.madmax.acamobile.adapters;


import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.madmax.acamobile.DataUpdateActivity;
import com.madmax.acamobile.R;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.models.ProductLeftModel;
import java.util.ArrayList;


public class StockProductUpdateAdapter extends RecyclerView.Adapter<StockProductUpdateAdapter.Holder> {

    private final Activity c;
    private final ArrayList<ProductLeftModel> data;
    private final LayoutInflater mInflater;

    public StockProductUpdateAdapter(Activity c, ArrayList<ProductLeftModel> data){
        this.data=data;
        this.c=c;
        this.mInflater= LayoutInflater.from(c);

    }


    @NonNull
    @Override
    public StockProductUpdateAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=mInflater.inflate(R.layout.item_stock_product_update,parent,false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StockProductUpdateAdapter.Holder holder, int position) {

        try{
            ProductLeftModel product=data.get(position);
            holder.tv_count.setText(product.getCount()+"");
            holder.tv_product_name.setText(product.getProduct_name());

        }catch (Exception e){
            Toast.makeText(c,e.toString()+"stock product adapter",Toast.LENGTH_SHORT).show();
        }

    }

    public class Holder extends RecyclerView.ViewHolder{

        TextView tv_product_name,tv_count;

        public Holder(View view){
            super(view);
            tv_product_name=view.findViewById(R.id.tv_product_name);
            tv_count=view.findViewById(R.id.tv_count);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ProductLeftModel model=data.get(getAdapterPosition());
                    Intent intent=new Intent(c, DataUpdateActivity.class);
                    intent.putExtra("hint","Enter items left");
                    intent.putExtra("message","Update items left for "+model.getProduct_name()+"("+model.getCount()+")");
                    intent.putExtra("key","count");
                    intent.putExtra("contentId",model.getProduct_id());
                    intent.putExtra("url", Routing.UPDATE_ITEM_LEFT);
                    c.startActivity(intent);
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
