package com.madmax.acamobile.fragments.partnergroup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.madmax.acamobile.R;

public class FragmentOrder extends Fragment {
    View v;

    String groupId;
    int isSoldOut;

    public FragmentOrder(String groupId,int isSoldOut){
        this.groupId=groupId;
        this.isSoldOut=isSoldOut;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v= inflater.inflate(R.layout.fragment_partner_group_order, container, false);

        return v;

    }
}
