
package com.emediate.android;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.emediate.android.adapter.ParamsListAdapter;
import com.emediate.android.database.DBHandler;
import com.emediate.android.emediatesdktester.R;
import com.emediate.android.util.ParamListView;
import com.emediate.controller.model.Param;

public class AdsCreateActivity extends Activity implements View.OnClickListener {

	/** EditTextView */
	private EditText mAddNameTxt, mBaseUrlTxt, mCUTxt;

	/** RadioButon */
	private RadioGroup mAdsRadioGroup;
	private RadioButton mAdsRadioBtn;

	private static final String CU_KEY = "cu";
	/** Button */
	private Button mAddParamsBtn;

	/** ListView */
	private ParamListView mParamsListView;
	private ParamsListAdapter mParamsListAdapter;

	/** Params */
	private ArrayList<Param> mParams;

	private DBHandler mDBHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ads_create);

		mAdsRadioGroup = (RadioGroup) findViewById(R.id.radio_ads_type_group);

		mAddNameTxt = (EditText) findViewById(R.id.param_ads_name);
		mBaseUrlTxt = (EditText) findViewById(R.id.param_ads_base_url);
		mCUTxt = (EditText) findViewById(R.id.param_ads_cu);

		mAddParamsBtn = (Button) findViewById(R.id.add_param_btn);
		mAddParamsBtn.setOnClickListener(this);

		mParams = new ArrayList<Param>();
		mParamsListView = (ParamListView) findViewById(R.id.params_list);
		mParamsListAdapter = new ParamsListAdapter(this, mParams);
		mParamsListView.setAdapter(mParamsListAdapter);

		mDBHandler = new DBHandler(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_ads_create, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.menu_save:
				saveNewAds();
				return true;
			case R.id.menu_cancel:
				setResult(MainActivity.CREATE_CANCEL_ADS_STATUS, null);
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.add_param_btn:
				popAddParamDialog(AdsCreateActivity.this);
				break;
		}

	}

	/**
	 * Get Ads Type MRAID or ORMMA
	 * 
	 * @return
	 */
	private String getAdsTypeListener() {
		// get selected radio button from radioGroup
		int selectedId = mAdsRadioGroup.getCheckedRadioButtonId();
		// find the radiobutton by returned id
		mAdsRadioBtn = (RadioButton) findViewById(selectedId);
		return mAdsRadioBtn.getText().toString();
	}

	/**
	 * Get Final Ads URL
	 * 
	 * @return
	 */
	private String getFinalAdsURL() {
		String adsUrl = mBaseUrlTxt.getText().toString().trim() + "?" + CU_KEY + "="
				+ mCUTxt.getText().toString().trim();

		if (mParams.size() != 0) {
			for (Param aParam : mParams) {
				adsUrl += "&" + aParam.key + "=" + aParam.value;
			}
		}
		return adsUrl;
	}

	/**
	 * Save a new Ads
	 */
	private void saveNewAds() {
		String url = "", type = "MRAID";
		if (mAddNameTxt.getText().toString() == null || mAddNameTxt.getText().toString().trim().equals("")) {
			Toast.makeText(this, "The Ads name can't be empty", Toast.LENGTH_LONG).show();
		}
		else if (mBaseUrlTxt.getText().toString() == null || mBaseUrlTxt.getText().toString().trim().equals("")) {
			Toast.makeText(this, "The Ads Base URL can't be empty", Toast.LENGTH_LONG).show();
		}
		else if (mCUTxt.getText().toString() == null || mCUTxt.getText().toString().trim().equals("")) {
			Toast.makeText(this, "The Ads Content Unit ID can't be empty", Toast.LENGTH_LONG).show();
		}
		else if (mDBHandler.hasThisAd(mAddNameTxt.getText().toString())) {
			Toast.makeText(this, "Duplicated Ads Name", Toast.LENGTH_LONG).show();
		}
		else {
			url = getFinalAdsURL();
			type = getAdsTypeListener();

			mDBHandler.open();
			mDBHandler.insert(mAddNameTxt.getText().toString(), url, type);
			mDBHandler.close();

			setResult(MainActivity.CREATE_ADS_STATUS, null);
			finish();

			Toast.makeText(
					this,
					"The Ads name:   " + mAddNameTxt.getText().toString() + "\nThe Ads Type: " + type
							+ "\nFull URL:     " + url, Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Popup Add Parameters Dialog
	 * 
	 * @param mContext
	 */
	public void popAddParamDialog(Context mContext) {
		Builder builder = new AlertDialog.Builder(mContext);

		// Get Layout infalter
		LayoutInflater inflater = this.getLayoutInflater();
		final LinearLayout dialogView = (LinearLayout) inflater.inflate(R.layout.dialog_add_params, null);

		builder.setView(dialogView);
		builder.setTitle("Add optional Parameters");
		builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {
				addNewParam(((EditText) dialogView.findViewById(R.id.add_key)).getText().toString().trim(),
						((EditText) dialogView.findViewById(R.id.add_value)).getText().toString().trim());
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});
		builder.show();
	}

	/**
	 * Add a new Parameter
	 * 
	 * @param key
	 * @param value
	 */
	private void addNewParam(String key, String value) {
		if (key != null && !key.trim().equals("") && value != null && !value.trim().equals("")) {
			Param newParam = new Param();
			newParam.key = key;
			newParam.value = value;
			mParams.add(newParam);
			mParamsListAdapter.notifyDataSetChanged();
		}
		else
			Toast.makeText(this, "Please fill in both 'Key' and 'Value'!", Toast.LENGTH_LONG).show();

	}


}
