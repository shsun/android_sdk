package com.emediate.view;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.AttributeSet;

import com.emediate.controller.MraidLocationController;
import com.emediate.controller.async.LoadHTMLAsynTask;
import com.emediate.controller.model.Param;
import com.emediate.controller.util.UDIDGenerator;

/**
 * Implementation of {@link MraidView} for use with Emediate ad campaigns.
 * <p>
 * In order to properly restore this view after a orientation change,
 * {@link #fetchCampaignNormal()} or {@link #fetchCampaignByFinalUrl(String)}
 * should be called after the hosting Activity's <code>onCreate</code> has
 * returned.
 * 
 * @author Fredrik Hyttnäs-Lenngren
 * 
 */
public class EmediateView extends MraidView {

    private MraidLocationController mLocationController;

    /** AdsBaseUrl */
    private String mAdsBaseUrl = "http://stage.emediate.eu/eas";
    /** AdsParams */
    private ArrayList<Param> mAdsParams = new ArrayList<Param>();
    /** Device_ID */
    private String mUDID;
    private static final String UDID_KEY = "eas_uid";
    /** Final Url */
    private String mFinalUrl = "";
    /** Preference */
    private static final String SHARE_PREF_NAME = "EmediateSDK";
    /** RefreshRate */
    private int mRefreshRate = 60; // 60 *1000 as default
    /** If the View is being restored */
    private boolean mIsRestoring = false;

    /** Timer Runner */
    private boolean run = false; // Set Default State of the Runnable Task as
				 // false
    private Handler handler = new Handler();
    private Runnable task = new Runnable() {

	public void run() {
	    if (run) {
		handler.postDelayed(this, mRefreshRate * 1000);
		fetchCampaign();
	    }
	}
    };

    /**
     * Set AdsRefresh Rate The Unit of Rate is seconds, if refreshRate is 10, it
     * is acturally mean 10*1000 milliseconds = 10 seconds ; Default RefreshRate
     * is 60*1000
     * 
     * @param refreshRate
     */
    public void setAdsRefreshRate(int adsRefreshRate) {
	mRefreshRate = adsRefreshRate;
    }

    public void startService() {
	stopService();
	run = true;
	handler.postDelayed(task, mRefreshRate * 1000);
    }

    public void stopService() {
	run = false;
    }

    public EmediateView(Context context) {
	super(context);
	hasUDID();

	mLocationController = new MraidLocationController(this, context);
	mLocationController.startLocationListener();
    }

    public EmediateView(Context context, AttributeSet set) {
	super(context, set);
	hasUDID();
	mLocationController = new MraidLocationController(this, context);
	mLocationController.startLocationListener();

    }

    /**
     * Set Ads Base Url
     * 
     * @param adsBaseUrl
     */
    public void setAdsBaseUrl(String adsBaseUrl) {
	mAdsBaseUrl = adsBaseUrl;
    }

    /**
     * Get Ads Base url
     * 
     * @return
     */
    public String getAdsBaseUrl() {
	return mAdsBaseUrl;
    }

    /**
     * Set Ads parameters
     * 
     * @param adsParams
     */
    public void setAdsParams(ArrayList<Param> adsParams) {
	mAdsParams = adsParams;
    }

    /**
     * Get All Ads Parameters
     * 
     * @return
     */
    public String getAllParams() {
	String allParams = "";
	for (Param param : mAdsParams) {
	    allParams += "&" + param.key + "=" + param.value;
	}
	return allParams;
    }

    /**
     * GetDevice UDID and Append after ADS url as parameter
     * 
     * @return
     */
    public String appendDeviceUDIDToURL() {
	return "&" + UDID_KEY + "=" + mUDID;
    }

    /**
     * Get Device CurrentLocation and Append after ADS url as parameter
     * 
     * @return
     */
    public String appendLocationToURL() {
	return mLocationController.getLocationParams();
    }

    /**
     * Check if the UDID has already been created
     */
    protected void hasUDID() {
	String UDID = getApplicationPreferences().getString(UDID_KEY, null);
	if (UDID == null) {
	    UDID = new UDIDGenerator().generateUDID();
	    getApplicationPreferences().edit().putString(UDID_KEY, UDID).commit();
	}
	mUDID = UDID;
    }

    /**
     * API: Fetch Campaign Developer only need to supply the full ads Url as
     * parameter eg. adsUrl = "http://stage.emediate.eu/eas?cu=512&kw1=expand";
     * 
     * @param adsUrl
     */
    public void fetchCampaignByFinalUrl(String adsUrl) {
	mFinalUrl = (adsUrl + appendDeviceUDIDToURL() + appendLocationToURL()).trim();
	fetchCampaign();
	startService();
    }

    /**
     * API: Fetch Campaign Normal Developer need to Fill in BaseUrl, Params
     * firstly And SDK will reassemble BaseUrl and Params and generate the final
     * adsURL.
     */
    public void fetchCampaignNormal() {
	mFinalUrl = (getAdsBaseUrl() + "?" + getAllParams() + appendDeviceUDIDToURL() + appendLocationToURL())
		.trim();
	fetchCampaign();
	startService();
    }

    /**
     * Fetch the current campaign based on the final url and if previous advert
     * should be reused.
     */
    private void fetchCampaign() {
	new LoadHTMLAsynTask(this.getContext(), mFinalUrl, this, mIsRestoring).execute();
	mIsRestoring = false; // Reset
    }

    @Override
    public Parcelable onSaveInstanceState() {
	final Bundle bundle = new Bundle();
	bundle.putParcelable("instanceState", super.onSaveInstanceState());
	bundle.putBoolean("restore", true);

	return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
	if (state instanceof Bundle) {
	    final Bundle bundle = (Bundle) state;
	    mIsRestoring = bundle.getBoolean("restore");
	    super.onRestoreInstanceState(bundle.getParcelable("instanceState"));
	} else {
	    super.onRestoreInstanceState(state);
	}
    }

    /**
     * Return true if the view is restoring the last displayed advert
     */
    public boolean isRestoringState() {
	return mIsRestoring;
    }

    /**
     * Get SharePreference
     * 
     * @return
     */
    private SharedPreferences getApplicationPreferences() {
	return this.getContext().getSharedPreferences(SHARE_PREF_NAME, Context.MODE_PRIVATE);
    }

    @Override
    protected void onDetachedFromWindow() {
	super.onDetachedFromWindow();

	if (this.getState().equals("default")) {
	    stopService();
	} else {
	    startService();
	}
    }

}
