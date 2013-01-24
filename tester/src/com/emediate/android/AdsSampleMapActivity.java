package com.emediate.android;

import android.os.Bundle;

import com.emediate.android.emediatesdktester.R;
import com.google.android.maps.MapActivity;


public class AdsSampleMapActivity extends MapActivity{

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_ads_sample_map);
	}
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

}
