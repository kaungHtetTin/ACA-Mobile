package com.madmax.acamobile.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.madmax.acamobile.ProductTransferActivity;
import com.madmax.acamobile.R;
import com.madmax.acamobile.models.Product;
import java.util.ArrayList;
import java.util.concurrent.Executor;


public class ProductTransferAdapter extends RecyclerView.Adapter<ProductTransferAdapter.Holder> {

    private final Activity c;
    ArrayList<Product> initialProducts;
    ArrayList<Product> targetProducts;

    private final LayoutInflater mInflater;
    final SharedPreferences sharedPreferences;
    String userId,authToken;
    Executor postExecutor;
    boolean isPressed=false;

    public ProductTransferAdapter(Activity c,ArrayList<Product> initialProducts,ArrayList<Product> targetProducts) {
        this.c = c;
        this.initialProducts=initialProducts;
        this.targetProducts=targetProducts;
        this.mInflater= LayoutInflater.from(c);
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=sharedPreferences.getString("userId",null);
        authToken=sharedPreferences.getString("authToken",null);
        postExecutor= ContextCompat.getMainExecutor(c);
        Log.e("start adapter ",initialProducts.size()+"");
    }

    @NonNull
    @Override
    public ProductTransferAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=mInflater.inflate(R.layout.item_product_transfer,parent,false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductTransferAdapter.Holder holder, int position) {

        try{
            Product initialProduct=initialProducts.get(position);
            Product targetProduct=targetProducts.get(position);

            final int[] initialQty = {initialProduct.getCount()};
            final int[] targetQty = {targetProduct.getCount()};
            final int[] POSITION={position};

            holder.tv.setText(initialProduct.getProduct_name());

            holder.tv_initial.setText(initialQty[0] +"");
            holder.tv_target.setText(targetQty[0] +"");


            holder.tv_target.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_UP:
                            isPressed=false;
                            Log.e("Action ","Release");
                            break;
                        case MotionEvent.ACTION_DOWN:
                            isPressed=true;
                            Log.e("Action ","Press");
                            transfer(holder.tv_initial, holder.tv_target, initialQty,targetQty,POSITION);
                            break;


                    }
                    return true;
                }
            });

            holder.tv_initial.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_UP:
                            isPressed=false;
                            Log.e("Action ","Release");
                            break;
                        case MotionEvent.ACTION_DOWN:
                            isPressed=true;
                            Log.e("Action ","Press");
                            transfer2(holder.tv_target, holder.tv_initial,targetQty,initialQty,POSITION);
                            break;

                    }
                    return true;
                }
            });


        }catch (Exception e){
            Toast.makeText(c,e.toString(),Toast.LENGTH_SHORT).show();
        }

    }

    private void transfer(TextView tv1, TextView tv2, int[] initial, int[] target,int [] position){
        Thread t=new Thread(new Runnable() {
            @Override
            public void run() {
                while (isPressed && initial[0]!=0){
                    initial[0]--;
                    target[0]++;
                    tv1.setText(initial[0]+"");
                    tv2.setText(target[0]+"");

                    initialProducts.get(position[0]).setCount(initial[0]);
                    targetProducts.get(position[0]).setCount(target[0]);
                    initialProducts.get(position[0]).setQtyChange(true);
                    targetProducts.get(position[0]).setQtyChange(true);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        t.start();

    }

    private void transfer2(TextView tv1, TextView tv2, int[] initial, int[] target,int [] position){
        Thread t=new Thread(new Runnable() {
            @Override
            public void run() {
                while (isPressed && initial[0]!=0){
                    initial[0]--;
                    target[0]++;
                    tv1.setText(initial[0]+"");
                    tv2.setText(target[0]+"");

                    initialProducts.get(position[0]).setCount(target[0]);
                    targetProducts.get(position[0]).setCount(initial[0]);
                    initialProducts.get(position[0]).setQtyChange(true);
                    targetProducts.get(position[0]).setQtyChange(true);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        t.start();

    }

    public class Holder extends RecyclerView.ViewHolder{

        TextView tv,tv_initial,tv_target;


        public Holder(View view){
            super(view);
            tv=view.findViewById(R.id.tv_product_name);
            tv_initial=view.findViewById(R.id.tv_initial);
            tv_target=view.findViewById(R.id.tv_target);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return  initialProducts.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void setTargetProducts(ArrayList<Product> targetProducts) {
        this.targetProducts = targetProducts;
    }

    public void setInitialProducts(ArrayList<Product> initialProducts) {
        this.initialProducts = initialProducts;
    }

    public ArrayList<Product> getTargetProducts() {
        return targetProducts;
    }

    public ArrayList<Product> getInitialProducts() {
        return initialProducts;
    }
}
