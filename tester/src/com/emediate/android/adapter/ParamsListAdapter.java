package com.emediate.android.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.emediate.android.emediatesdktester.R;
import com.emediate.controller.model.Param;


public class ParamsListAdapter extends BaseAdapter{

	private ArrayList<Param> mParams;
	private Context mContext;
	private ImageButton mDeleteBtn;
	
	public ParamsListAdapter(Context context, ArrayList<Param> params){
		mContext = context;
		mParams = params;
		
	}
	
	@Override
	public int getCount() {
		return mParams.size();
	}

	@Override
	public Object getItem(int position) {
		return mParams.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		View v;
		if (convertView == null) {
			v = LayoutInflater.from(mContext).inflate(R.layout.list_params, parent, false);
		} 
		else
			v = convertView;
		
		TextView keyText = (TextView) v.findViewById(R.id.param_key);
		TextView valueText = (TextView) v.findViewById(R.id.param_value);
		
		keyText.setText(mParams.get(position).key);
		valueText.setText(mParams.get(position).value);
		
		mDeleteBtn = (ImageButton) v.findViewById(R.id.param_delete_btn);
		mDeleteBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mParams.remove(position);
				notifyDataSetChanged();
			}
		});
		
		return v;
	}

}
