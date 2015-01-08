package com.tiwence.rgbcolorchallenge;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RulesFragment extends Fragment {
	
	private View mRulesView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRulesView = inflater.inflate(R.layout.fragment_rules, container, false);

		RgbUtils.setFontFace(getActivity(), (ViewGroup)mRulesView);
		
		return mRulesView;
	}
}
