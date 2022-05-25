package com.madmax.acamobile.fragments.mygroup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.madmax.acamobile.R;

public class FragmentOverview extends Fragment {
    View v;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v= inflater.inflate(R.layout.fragment_my_group_overview, container, false);

        return v;

    }
}
