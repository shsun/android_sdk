package com.emediate.android.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.emediate.android.database.DBHandler;
import com.emediate.android.emediatesdktester.R;
import com.emediate.android.model.Ads;


public class AdsListAdapter extends BaseAdapter{

	private Context mContext;
	private ArrayList<Ads> mAds;
	private ImageView mDeleteBtn;

	private DBHandler mDBHandler;
	
	public AdsListAdapter(Context context, ArrayList<Ads> ads){
		mContext = context;
		mAds = ads;
		mDBHandler = new DBHandler(context);
	}
	
	@Override
	public int getCount() {
		if(mAds != null)
			return mAds.size();
		else 
			return 0;
	}

	@Override
	public Object getItem(int position) {
		return mAds.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View v;
		if (convertView == null) {
			v = LayoutInflater.from(mContext).inflate(R.layout.list_ads, parent, false);
		} 
		else
			v = convertView;
		
		TextView adsText = (TextView) v.findViewById(R.id.ads);
		adsText.setText(mAds.get(position).name);
		
		mDeleteBtn = (ImageView) v.findViewById(R.id.ads_delete_btn);
		mDeleteBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mDBHandler.delete(mAds.get(position).name);
				mAds.remove(position);
				notifyDataSetChanged();

				Toast.makeText(mContext, "One ads has been deleted.", Toast.LENGTH_SHORT).show();
			}
		});
		
		return v;
	}

}
