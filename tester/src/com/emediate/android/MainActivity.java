
package com.emediate.android;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.emediate.android.adapter.AdsListAdapter;
import com.emediate.android.database.DBHandler;
import com.emediate.android.emediatesdktester.R;
import com.emediate.android.model.Ads;

public class MainActivity extends Activity implements OnItemClickListener {

	/** Ads Mraid ListView */
	private ListView mAdsMraidListView;
	private AdsListAdapter mAdsMraidListAdapter;
	private ArrayList<Ads> mAdsMraid;
	

	/** Ads Ormma ListView */
	private ListView mAdsOrmmaListView;
	private AdsListAdapter mAdsOrmmaListAdapter;
	private ArrayList<Ads> mAdsOrmma;

	public static final int CREATE_ADS_STATUS = 1001;
	public static final int CREATE_CANCEL_ADS_STATUS = 1002;
	
	public static final String ADS_TYPE_MRAID = "MRAID", ADS_TYPE_ORMMA = "ORMMA";

	private DBHandler mDBHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mDBHandler = new DBHandler(this);
		
		// MRAID Init
		mAdsMraid = new ArrayList<Ads>();
		mAdsMraid = mDBHandler.getAllAdsByType(ADS_TYPE_MRAID);
		if (mAdsMraid == null)
			mAdsMraid = new ArrayList<Ads>();

		mAdsMraidListView = (ListView) findViewById(R.id.ads_mraid_list);
		mAdsMraidListAdapter = new AdsListAdapter(this, mAdsMraid);
		mAdsMraidListView.setAdapter(mAdsMraidListAdapter);
		mAdsMraidListView.setOnItemClickListener(this);

		// ORMMA Init
		mAdsOrmma = new ArrayList<Ads>();
		mAdsOrmma = mDBHandler.getAllAdsByType(ADS_TYPE_ORMMA);
		if (mAdsOrmma == null)
			mAdsOrmma = new ArrayList<Ads>();

		mAdsOrmmaListView = (ListView) findViewById(R.id.ads_ormma_list);
		mAdsOrmmaListAdapter = new AdsListAdapter(this, mAdsOrmma);
		mAdsOrmmaListView.setAdapter(mAdsOrmmaListAdapter);
		mAdsOrmmaListView.setOnItemClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.menu_create:
				openAdsCreateActivity();
				return true;
			case R.id.menu_delete:
				this.startActivity(new Intent(MainActivity.this, AdsSampleMapActivity.class));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Popup Choose Ads Type Dialog
	 * 
	 * @param mContext
	 */
	private void popChooseAdsTypeDialog(Context context, final String adsUrl, final String adsType) {
		Builder builder = new AlertDialog.Builder(context);
		builder.setCancelable(true);
		builder.setTitle(null);
		builder.setMessage("Choose your ads type:");
		builder.setPositiveButton("SimpleBanner", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, AdsSampleActivity.class);
				intent.putExtra(AdsSampleActivity.ADS_PLACE_TYPE, AdsSampleActivity.ADS_PLACE_TYPE_SIMPLE);
				intent.putExtra(AdsSampleActivity.ADS_URL, adsUrl);
				intent.putExtra(AdsSampleActivity.ADS_TYPE, adsType);
				startActivity(intent);
			}
		});
		builder.setNegativeButton("Part of ListView", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				popChoosePartsOfListViewTypeDialog(MainActivity.this, adsUrl, adsType);
			}
		});
		builder.show();
	}

	/**
	 * Popup Choose Part Of ListView Type Dialog
	 * 
	 * @param mContext
	 */
	private void popChoosePartsOfListViewTypeDialog(Context mContext, final String adsUrl, final String adsType) {
		Builder builder = new AlertDialog.Builder(mContext);
		// Get Layout infalter
		LayoutInflater inflater = this.getLayoutInflater();
		final LinearLayout dialogView = (LinearLayout) inflater.inflate(R.layout.dialog_part_of_listview, null);
		builder.setView(dialogView);
		builder.setCancelable(true);
		builder.setTitle(null);
		builder.setPositiveButton("Open Ads", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, AdsSampleActivity.class);
				intent.putExtra(AdsSampleActivity.ADS_PLACE_TYPE, AdsSampleActivity.ADS_PLACE_TYPE_LISTVIEW);
				intent.putExtra(AdsSampleActivity.ADS_URL, adsUrl);
				intent.putExtra(AdsSampleActivity.ADS_TYPE, adsType);
				intent.putExtra(AdsSampleActivity.ADS_POSITION, ((EditText) dialogView.findViewById(R.id.dialog_index_list)).getText().toString());
				startActivity(intent);
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});
		builder.show();
	}

	/**
	 * Open Ads Create Activity
	 */
	private void openAdsCreateActivity() {
		this.startActivityForResult(new Intent(this, AdsCreateActivity.class), CREATE_ADS_STATUS);
		Toast.makeText(this, "Open Ads Creater", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == CREATE_ADS_STATUS) {
			mAdsMraid = mDBHandler.getAllAdsByType(ADS_TYPE_MRAID);
			mAdsMraidListAdapter = new AdsListAdapter(this, mAdsMraid);
			mAdsMraidListView.setAdapter(mAdsMraidListAdapter);
			mAdsMraidListAdapter.notifyDataSetChanged();
			

			mAdsOrmma = mDBHandler.getAllAdsByType(ADS_TYPE_ORMMA);
			mAdsOrmmaListAdapter = new AdsListAdapter(this, mAdsOrmma);
			mAdsOrmmaListView.setAdapter(mAdsOrmmaListAdapter);
			mAdsOrmmaListAdapter.notifyDataSetChanged();

			Toast.makeText(this, "Your ads has been created.", Toast.LENGTH_SHORT).show();
		}
		else if (resultCode == CREATE_CANCEL_ADS_STATUS) {
			Toast.makeText(this, "You have canceled the ads creation.", Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long arg3) {
		if(parent.getId() == mAdsMraidListView.getId())
			popChooseAdsTypeDialog(MainActivity.this, mAdsMraid.get(position).url, mAdsMraid.get(position).type);
		else if(parent.getId() == mAdsOrmmaListView.getId())
			popChooseAdsTypeDialog(MainActivity.this, "https://dl.dropbox.com/u/22406053/ormma-test-ad-level-1.html", mAdsOrmma.get(position).type); // For Testing
	}

}
