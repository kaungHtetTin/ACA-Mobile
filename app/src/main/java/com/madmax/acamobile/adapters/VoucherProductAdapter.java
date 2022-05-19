package com.madmax.acamobile.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.madmax.acamobile.OrderDetailActivity;
import com.madmax.acamobile.R;
import com.madmax.acamobile.app.Price;
import com.madmax.acamobile.models.PriceModel;
import com.madmax.acamobile.models.Product;
import com.madmax.acamobile.models.ProductModel;
import com.madmax.acamobile.models.StockModel;

import java.util.ArrayList;
import java.util.concurrent.Executor;


public class VoucherProductAdapter extends RecyclerView.Adapter<VoucherProductAdapter.Holder> {

    private final Activity c;
    private final ArrayList<ProductModel> data;
    private final LayoutInflater mInflater;
    private StockModel stock;
    final SharedPreferences sharedPreferences;
    String userId,authToken;
    Executor postExecutor;

    public VoucherProductAdapter(Activity c, ArrayList<ProductModel> data,StockModel stock){
        this.data=data;
        this.c=c;
        this.stock=stock;
        this.mInflater= LayoutInflater.from(c);
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=sharedPreferences.getString("userId",null);
        authToken=sharedPreferences.getString("authToken",null);
        postExecutor= ContextCompat.getMainExecutor(c);

    }

    @NonNull
    @Override
    public VoucherProductAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=mInflater.inflate(R.layout.item_voucher,parent,false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherProductAdapter.Holder holder, int position) {

        ProductModel model=data.get(position);
        holder.tv_product.setText(model.getProduct_name());


        ArrayList<Product> products=stock.getProducts();
        Product p = null;
        int qty=model.getQuantity();
        int foc=model.getFoc();

        int totalQty=qty+foc;

        for(int i=0;i<products.size();i++){
            Product product=products.get(i);

            if(product.getProduct_id()==model.getProduct_id()){
                p=product;
                int left=p.getCount();
                holder.tv_left.setText(left+"");
                if(totalQty>left){
                    holder.mLayout.setBackgroundColor(c.getResources().getColor(R.color.light_yellow));
                    OrderDetailActivity.isEnoughToSend=false;
                }else{
                    holder.mLayout.setBackgroundColor(c.getResources().getColor(R.color.white));

                }

                break;
            }
        }


        String qtyStr=holder.et_quantity.getText().toString();
        if(qtyStr.equals("")){
            model.setQuantity(0);
        }else{
            model.setQuantity(Integer.parseInt(qtyStr));
        }

        String focStr=holder.et_foc.getText().toString();
        if(focStr.equals("")){
            model.setFoc(0);
        }else{
            model.setFoc(Integer.parseInt(focStr));
        }

        Product finalP = p;
        holder.et_quantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String qty=charSequence+"";
                int quantity;
                if(qty.equals("")){
                    quantity=0;

                }else{
                    quantity=Integer.parseInt(qty);

                }

                int left= finalP.getCount();
                holder.tv_left.setText(left+"");
                if(quantity>left){
                    holder.mLayout.setBackgroundColor(c.getResources().getColor(R.color.light_yellow));
                    OrderDetailActivity.isEnoughToSend=false;
                }else{
                    holder.mLayout.setBackgroundColor(c.getResources().getColor(R.color.white));
                    OrderDetailActivity.isEnoughToSend=true;
                }

                dataUpdate(holder.getAdapterPosition(), quantity);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        holder.et_foc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String tempFoc=charSequence+"";
                int foc;
                if(tempFoc.equals("")){
                    foc=0;

                }else{
                    foc=Integer.parseInt(tempFoc);

                }

                int left= finalP.getCount();
                holder.tv_left.setText(left+"");
                int totalQty=foc+model.getQuantity();
                if(totalQty>left){
                    holder.mLayout.setBackgroundColor(c.getResources().getColor(R.color.light_yellow));
                    OrderDetailActivity.isEnoughToSend=false;
                }else{
                    holder.mLayout.setBackgroundColor(c.getResources().getColor(R.color.white));
                }

                data.get(holder.getAdapterPosition()).setFoc(foc);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


    }

    private void dataUpdate(int position,int quantity){

        //quantity update
        data.get(position).setQuantity(quantity);

        //price Update
        for(int i=0;i<data.size();i++){
            ProductModel m=data.get(i);
            PriceModel price=selectPrice(m.getPrices(),totalQty());
            float PRICE=price.getPrice();
            data.get(i).setSelectedPrice(PRICE);
        }
    }

    private int totalQty(){
        int totalQty=0;
        for(int i=0;i<data.size();i++){
            ProductModel model=data.get(i);
            int temp=model.getQuantity()/model.getPack();
            totalQty+=temp;

        }
        return totalQty;
    }

    private PriceModel selectPrice(PriceModel [] prices,int quantity){

        for (int i = 0, pricesLength = prices.length; i < pricesLength; i++) {
            PriceModel price = prices[i];

            if (quantity <= price.getCount()-1) {

                if(i==0){
                    return prices[i];
                }else{
                    return prices[i-1];
                }
            }

            if(quantity>=5000)return prices[Price.five_thousand_price];

        }
        return prices[0];
    }



    public class Holder extends RecyclerView.ViewHolder{

        TextView tv_product,tv_left;
        EditText et_quantity,et_foc;
        RelativeLayout mLayout;
        public Holder(View view){
            super(view);
            tv_product=view.findViewById(R.id.tv_product_name);
            tv_left=view.findViewById(R.id.tv_left);
            et_quantity=view.findViewById(R.id.et_quantity);
            mLayout=view.findViewById(R.id.mLayout);
            et_foc=view.findViewById(R.id.et_foc);

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

    public void setStock(StockModel stock) {
        this.stock = stock;
    }

    public ArrayList<ProductModel>  getSelectedProducts(){
        ArrayList<ProductModel> temp=new ArrayList<>();
        for(int i=0;i<data.size();i++){
            ProductModel model=data.get(i);
            if((model.getQuantity()+model.getFoc())>0 ){
                temp.add(model);
            }
        }
        return temp;
    }
}
