package com.madmax.acamobile.adapters;

import static com.madmax.acamobile.CreateOrderActivity.price_index;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.madmax.acamobile.CreateOrderActivity;
import com.madmax.acamobile.R;
import com.madmax.acamobile.app.AppUtils;
import com.madmax.acamobile.app.Price;
import com.madmax.acamobile.models.PriceModel;
import com.madmax.acamobile.models.ProductModel;
import java.util.ArrayList;
import java.util.concurrent.Executor;


public class OrderCreateProductAdapter extends RecyclerView.Adapter<OrderCreateProductAdapter.Holder> {

    private final Activity c;
    private final ArrayList<ProductModel> data;
    private final LayoutInflater mInflater;
    final SharedPreferences sharedPreferences;
    String userId,authToken;
    Executor postExecutor;


    public OrderCreateProductAdapter(Activity c, ArrayList<ProductModel> data){
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
    public OrderCreateProductAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=mInflater.inflate(R.layout.item_order_create,parent,false);
        return new Holder(view);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull OrderCreateProductAdapter.Holder holder, int position) {

//        try{

            ProductModel model=data.get(position);
            PriceModel [] prices=model.getPrices();

            String product_name=model.getProduct_name();            //product name
            int quantity=model.getQuantity();// quantity
            float price=model.getSelectedPrice();


            // calculate amount

            int tempQty=quantity/model.getPack();
            int pack=quantity%model.getPack();
            float pack_price=pack*model.getPack_price();
            double amount=tempQty*price+pack_price;

            //calculate point
            float point=model.getPoint()*tempQty;



            int discount=(int)model.getDiscount();
            float discount_save=(float)(price/100.0)*discount;
            price=price-discount_save;// selected Price


            PriceModel retail=prices[price_index];  //OrderCreateActivity
            float saving=retail.getPrice()-price;

            //this this for profit
            int foc=model.getFoc();
            int focAndQty;
            if(price_index==0){
                focAndQty=foc+quantity;
            }else{
                focAndQty=quantity;
            }
            focAndQty=focAndQty/model.getPack();
            float tempPrice=focAndQty*retail.getPrice();
            float profit= (float) (tempPrice-amount);

            holder.tv_product_name.setText(product_name);
            holder.et_quantity.setText(quantity+"");
            holder.tv_price.setText(model.getSelectedPrice()+"");
            holder.tv_amount.setText(AppUtils.getTwoDecimalDouble(amount));
            holder.tv_point.setText(AppUtils.getTwoDecimalDouble((double)point));
            holder.tv_retail.setText(retail.getPrice()+"");
            holder.tv_saving.setText(""+saving);
            holder.tv_profit.setText(AppUtils.getTwoDecimalDouble((double)profit));
            holder.et_foc.setText(foc+"");

            if(amount==0){
                holder.tv_profit_percent.setText("0%");
            }else{
                double profit_percent= (profit*(100/amount));
                holder.tv_profit_percent.setText(AppUtils.getTwoDecimalDouble(profit_percent)+"%");
            }


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

                    dataUpdate(holder.getAdapterPosition(),quantity);

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
                String qty=charSequence+"";
                int foc;
                if(qty.equals("")){
                    foc=0;
                }else{
                    foc=Integer.parseInt(qty);
                }

                data.get(holder.getAdapterPosition()).setFoc(foc);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });



//        }catch (Exception e){
//            Toast.makeText(c,e.toString(),Toast.LENGTH_SHORT).show();
//        }

      //  calculateOrder();

    }

    public class Holder extends RecyclerView.ViewHolder{

        TextView tv_point,tv_price,tv_amount,tv_retail,tv_saving,tv_profit,tv_product_name,tv_profit_percent;
        EditText et_quantity,et_foc;


        public Holder(View view){
            super(view);
            tv_product_name=view.findViewById(R.id.tv_product_name);
            tv_point=view.findViewById(R.id.tv_point);
            tv_price=view.findViewById(R.id.tv_price);
            tv_amount=view.findViewById(R.id.tv_phone);
            et_quantity=view.findViewById(R.id.et_quantity);
            et_foc=view.findViewById(R.id.et_foc);
            tv_retail=view.findViewById(R.id.tv_retail);
            tv_profit=view.findViewById(R.id.tv_profit);
            tv_saving= view.findViewById(R.id.tv_saving);
            tv_profit_percent=view.findViewById(R.id.tv_profit_percent);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(c,data.get(getAdapterPosition()).getSelectedPrice()+"",Toast.LENGTH_SHORT).show();
                }
            });
        }
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

        if(CreateOrderActivity.SELECTED_PRICE_INDEX==13){
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
        }else{
            return prices[CreateOrderActivity.SELECTED_PRICE_INDEX];
        }

    }


    public ArrayList<ProductModel> getData() {
        return data;
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
