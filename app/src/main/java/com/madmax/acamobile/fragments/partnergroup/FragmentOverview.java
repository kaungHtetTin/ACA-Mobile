package com.madmax.acamobile.fragments.partnergroup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import com.madmax.acamobile.R;
import com.madmax.acamobile.charts.TargetPlanAndOrderRate;

public class FragmentOverview extends Fragment {
    View v;

    LinearLayout mLayout;
    String userId;
    SharedPreferences sharedPreferences;

    String groupId;
    public FragmentOverview(String groupId){
        this.groupId=groupId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v= inflater.inflate(R.layout.fragment_partner_group_overview, container, false);

        sharedPreferences=getActivity().getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=sharedPreferences.getString("userId",null);

        setUpView();
        return v;

    }

    private void setUpView(){
        mLayout=v.findViewById(R.id.mLayout);

        TargetPlanAndOrderRate targetPlanAndOrderRate=new TargetPlanAndOrderRate(getActivity(),groupId,userId);
        mLayout.addView(targetPlanAndOrderRate.getView());
    }
}
