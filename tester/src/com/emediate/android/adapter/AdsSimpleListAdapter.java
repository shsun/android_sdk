
package com.emediate.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.emediate.android.emediatesdktester.R;
import com.emediate.view.EmediateView;

public class AdsSimpleListAdapter extends BaseAdapter {

	private String[] mSampleText;
	private String mAdsUrl;
	private Context mContext;
	private int mAdsPosition;

	public AdsSimpleListAdapter(Context context, String adsUrl, String adsType, String[] sampleText, int adsPosition,
			int adsViewTop) {
		mContext = context;
		mSampleText = sampleText;
		mAdsPosition = adsPosition;
		mAdsUrl = adsUrl;
	}

	@Override
	public int getCount() {
		return mSampleText.length + 1;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		if (position > mAdsPosition)
			return position - 1;
		else
			return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = null;
		if (position == mAdsPosition) {

			if (convertView == null) {
				v = LayoutInflater.from(mContext).inflate(R.layout.ads_emediate_banner, parent, false);
			}
			else
				v = convertView;

			EmediateView mEmediateView = (EmediateView) v.findViewById(R.id.emediate_ads_view);
			mEmediateView.setAdsRefreshRate(60);
			mEmediateView.fetchCampaignByFinalUrl(mAdsUrl);
			
		}
		else {
			if (convertView == null) {
				v = LayoutInflater.from(mContext).inflate(R.layout.list_ads_sample, parent, false);
			}
			else
				v = convertView;

			TextView adsSampleText = (TextView) v.findViewById(R.id.ads_sample_txt);
			if (position > mAdsPosition)
				adsSampleText.setText(mSampleText[position - 1]);
			else
				adsSampleText.setText(mSampleText[position]);
		}

		return v;
	}
}
