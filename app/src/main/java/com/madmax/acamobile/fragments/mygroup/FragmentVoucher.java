package com.madmax.acamobile.fragments.mygroup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.madmax.acamobile.R;

public class FragmentVoucher extends Fragment {
    View v;

    String groupId;

    public FragmentVoucher(String groupId){
        this.groupId=groupId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v= inflater.inflate(R.layout.fragment_my_group_voucher, container, false);
        return v;
    }
}
