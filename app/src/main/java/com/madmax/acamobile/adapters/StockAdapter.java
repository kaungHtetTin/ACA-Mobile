package com.madmax.acamobile.adapters;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.madmax.acamobile.R;
import com.madmax.acamobile.models.StockModel;
import java.util.ArrayList;
import java.util.concurrent.Executor;


public class StockAdapter extends RecyclerView.Adapter<StockAdapter.Holder> {

    private final Activity c;
    private final ArrayList<StockModel> data;
    private final LayoutInflater mInflater;
    final SharedPreferences sharedPreferences;
    String userId,authToken;
    Executor postExecutor;

    public StockAdapter(Activity c, ArrayList<StockModel> data){
        this.data=data;
        this.c=c;
        this.mInflater= LayoutInflater.from(c);
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=sharedPreferences.getString("userId",null);
        authToken=sharedPreferences.getString("authToken",null);
        postExecutor= ContextCompat.getMainExecutor(c);

    }


    @NonNull
    @Override
    public StockAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=mInflater.inflate(R.layout.item_stock,parent,false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StockAdapter.Holder holder, int position) {

        try{
           StockModel model=data.get(position);
           holder.tv.setText(model.getName());

        }catch (Exception e){
            Toast.makeText(c,e.toString(),Toast.LENGTH_SHORT).show();
        }

    }

    public class Holder extends RecyclerView.ViewHolder{


        TextView tv;


        public Holder(View view){
            super(view);

            tv=view.findViewById(R.id.tv_stock_name);

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
