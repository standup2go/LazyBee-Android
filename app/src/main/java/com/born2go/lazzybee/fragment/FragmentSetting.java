package com.born2go.lazzybee.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.born2go.lazzybee.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentSetting extends Fragment {


    public static final String TAG = "FragmentSetting";

    public FragmentSetting() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }


}
