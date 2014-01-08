package com.emediate.view;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.emediate.controller.MraidLocationController;
import com.emediate.controller.ad.AdBuffer;
import com.emediate.controller.ad.AsyncAdTask;
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
    /* Ads url exclusive UDID and Location*/
    private String mAdsUrl = "";
    /** Final Url */
    private String mFinalUrl = "";
    /** Preference */
    private static final String SHARE_PREF_NAME = "EmediateSDK";
    /** RefreshRate */
    private long mRefreshRate = 60000; // One minute
    /** If the View is being restored */
    private boolean mIsRestoring = false;
    /** Buffer which holds temporary ads */
    private AdBuffer mBuffer;
    /** Number of ads which should exist within the buffer */
    private int mNumAdsToBuffer = 1;

    /** Timer Runner */
    private boolean run = false; // Set Default State of the Runnable Task as
				 // false
    private Handler handler = new Handler();
    private Runnable task = new Runnable() {

	public void run() {
	    if (run) {
		handler.postDelayed(this, mRefreshRate);
		fetchCampaign();
	    }
	}
    };

    

    public EmediateView(Context context, AttributeSet set) {
	super(context, set);
	hasUDID();

	mLocationController = new MraidLocationController(this, context);
    }

    public EmediateView(Context context) {
	this(context, null);
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
     * Get SharePreference
     * 
     * @return
     */
    private SharedPreferences getApplicationPreferences() {
	return this.getContext().getSharedPreferences(SHARE_PREF_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Same as calling <code>fetchCampaignNormal(<em>false</em>)</code>
     * 
     * @see #fetchCampaignNormal(boolean)
     */
    public final void fetchCampaignNormal() {
	fetchCampaignNormal(false);
    }

    /**
     * Same as calling <code>fetchCampaignByFinalUrl(url, <em>false</em>)</code>
     * 
     * @see #fetchCampaignByFinalUrl(String, boolean)
     */
    public final void fetchCampaignByFinalUrl(String adsUrl) {
	fetchCampaignByFinalUrl(adsUrl, false);
    }
    
    /**
     * API: Fetch Campaign Normal Developer need to Fill in BaseUrl, Params
     * firstly And SDK will reassemble BaseUrl and Params and generate the final
     * adsURL.
     * 
     * @param clearBuffer
     *            true to clear the buffer before starting campaign
     * 
     */
    public final void fetchCampaignNormal(boolean clearBuffer) {
	fetchCampaignByFinalUrl(getAdsBaseUrl() + "?" + getAllParams(), clearBuffer);
    }



    /**
     * API: Fetch Campaign Developer only need to supply the full ads Url as
     * parameter eg. adsUrl = "http://stage.emediate.eu/eas?cu=512&kw1=expand";
     * 
     * @param adsUrl
     *            final ad-campaign url
     * @param clearBuffer
     *            true if underlaying ad buffer should be cleared before
     *            campaign is started
     */
    public void fetchCampaignByFinalUrl(String adsUrl, boolean clearBuffer) {
	mAdsUrl = adsUrl;
	mFinalUrl = (adsUrl + appendDeviceUDIDToURL() + appendLocationToURL()).trim();
	
	mBuffer = new AdBuffer(getContext(), adsUrl);
	
	if (clearBuffer) {
	    mBuffer.clear();
	}
	
	fetchCampaign();
	startService();
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
     * Fetch the current campaign based on the final url and if previous advert
     * should be reused.
     */
    private void fetchCampaign() {
	new AsyncAdTask(this, mBuffer, mFinalUrl, mIsRestoring).execute(mNumAdsToBuffer);
	mIsRestoring = false; // Reset
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

    /**
     * Manualy start the ad-service, only call this if you manualy called
     * {@link #stopService()}
     * 
     * @throws IllegalStateException
     *             if the ad-view has not yet been setup
     */
    public void startService() {
	if (TextUtils.isEmpty(mFinalUrl))
	    throw new IllegalStateException("There are no campaign values");

	stopService();
	run = true;

	mLocationController.startLocationListener();
	
	if (mRefreshRate > 0)
	    handler.postDelayed(task, mRefreshRate);
    }

    /**
     * Manualy stop the ad campaign service. No new ads will be delivered until
     * {@link #startService()}, {@link #fetchCampaignNormal()} or
     * {@link #fetchCampaignByFinalUrl(String)} is called.
     */
    public void stopService() {
	run = false;
	handler.removeCallbacksAndMessages(null);
	mLocationController.stopLocationListener();
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
     * Set the refresh rate of ads in {@link TimeUnit#SECONDS}. A refresh rate
     * lower than 0 means no refresh after inital fetch.
     * 
     * @param refreshRate
     *            the refresh rate
     */
    public void setAdsRefreshRate(int refreshRate) {
	mRefreshRate = TimeUnit.SECONDS.toMillis(refreshRate);
    }

    /**
     * Set the number of ads which should be keept buffered (inclusive).
     * 
     * @param buffer
     *            number of ads to buffer, or <= 1 to prevent buffering of ads
     */
    public void setAdsBufferLimit(int buffer) {
	mNumAdsToBuffer = Math.max(1, buffer);
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
     * Set Ads parameters
     * 
     * @param adsParams
     */
    public void setAdsParams(ArrayList<Param> adsParams) {
	mAdsParams = adsParams;
    }

    @Override
    public Parcelable onSaveInstanceState() {
	final Bundle bundle = new Bundle();
	bundle.putParcelable("instanceState", super.onSaveInstanceState());
	bundle.putBoolean("restore", true);
	bundle.putParcelableArrayList("params", mAdsParams);
	bundle.putString("url", mAdsUrl);
	bundle.putInt("buffer", mNumAdsToBuffer);
	bundle.putLong("refresh", mRefreshRate);
	
	return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
	if (state instanceof Bundle) {
	    final Bundle bundle = (Bundle) state;
	    mIsRestoring = bundle.getBoolean("restore");
	    mAdsParams = bundle.getParcelableArrayList("params");
	    mRefreshRate = bundle.getLong("refresh");
	    mNumAdsToBuffer = bundle.getInt("buffer");
	    mAdsUrl = bundle.getString("url");
    
	    super.onRestoreInstanceState(bundle.getParcelable("instanceState"));
	} else {
	    super.onRestoreInstanceState(state);
	}
	
	if(mIsRestoring) {
	    fetchCampaignByFinalUrl(mAdsUrl);
	}
	
    }

    /**
     * Return true if the view is restoring the last displayed advert
     */
    public boolean isRestoringState() {
	return mIsRestoring;
    }
}
