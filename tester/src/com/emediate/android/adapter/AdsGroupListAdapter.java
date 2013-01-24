package com.emediate.android.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;


public class AdsGroupListAdapter extends BaseExpandableListAdapter{
	
	private static final String[] GROUP_TYPES = {"MRAID", "ORMMA"};

	@Override
	public Object getChild(int arg0, int arg1) {
		return null;
	}

	@Override
	public long getChildId(int arg0, int arg1) {
		return 0;
	}

	@Override
	public View getChildView(int arg0, int arg1, boolean arg2, View arg3, ViewGroup arg4) {
		return null;
	}

	@Override
	public int getChildrenCount(int arg0) {
		return 0;
	}

	@Override
	public Object getGroup(int position) {
		return null;
	}

	@Override
	public int getGroupCount() {
		return GROUP_TYPES.length;	// Mraid and Ormma
	}

	@Override
	public long getGroupId(int arg0) {
		return 0;
	}

	@Override
	public View getGroupView(int arg0, boolean arg1, View arg2, ViewGroup arg3) {
		return null;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int arg0, int arg1) {
		return false;
	}

}
