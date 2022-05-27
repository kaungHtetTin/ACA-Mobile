package com.madmax.acamobile.fragments.mygroup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import com.madmax.acamobile.R;
import com.madmax.acamobile.app.Routing;
import com.madmax.acamobile.charts.SaleAndOrderRate;
import com.madmax.acamobile.charts.SaleRateInAProduct;

public class FragmentOverview extends Fragment {
    View v;


    LinearLayout mLayout;

    String userId;
    String groupId;
    public FragmentOverview(String userId,String groupId){
        this.userId=userId;
        this.groupId=groupId;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v= inflater.inflate(R.layout.fragment_my_group_overview, container, false);

        setUpView();

        return v;

    }

    private void setUpView(){
        mLayout=v.findViewById(R.id.mLayout);

        SaleAndOrderRate saleAndOrderRate=new SaleAndOrderRate(getActivity(),getActivity().getSupportFragmentManager(),userId,
                Routing.CHART_GROUP_SALE_AND_ORDER+"?group_id="+groupId+"&",true);
        saleAndOrderRate.invisibleTVSeeMore();
        mLayout.addView(saleAndOrderRate.getView());

        SaleRateInAProduct saleRateInAProduct=new SaleRateInAProduct(userId,getActivity(),Routing.CHART_GROUP_SALE_RATE_IN_A_PRODUCT+"?group_id="+groupId+"&",true);
        mLayout.addView(saleRateInAProduct.getView());


    }
}
