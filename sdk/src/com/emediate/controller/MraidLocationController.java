package com.emediate.controller;

import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.emediate.controller.listeners.LocListener;
import com.emediate.view.MraidView;

/**
 * The Class OrmmaLocationController.  Ormma controller for interacting with lbs
 */
public class MraidLocationController extends MraidController {

	private static final String LOG_TAG = "MraidLocationController";
	
	private LocationManager mLocationManager;
	private boolean hasPermission = false;
	final int INTERVAL = 1000;
	private LocListener mGps;
	private LocListener mNetwork;
	private int mLocListenerCount;
	private boolean allowLocationServices = false;
	
	/**
	 * Instantiates a new ormma location controller.
	 *
	 * @param adView the ad view
	 * @param context the context
	 */
	public MraidLocationController(MraidView adView, Context context) {
		super(adView, context);
		try {
			mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			if (mLocationManager.getProvider(LocationManager.GPS_PROVIDER) != null)
				mGps = new LocListener(context, INTERVAL, this, LocationManager.GPS_PROVIDER);
			if (mLocationManager.getProvider(LocationManager.NETWORK_PROVIDER) != null)
				mNetwork = new LocListener(context, INTERVAL, this, LocationManager.NETWORK_PROVIDER);
			hasPermission = true;
		} catch (SecurityException e) {

		}
	}

	/**
	 * @param flag - Should the location services be enabled / not.
	 */
	public void allowLocationServices(boolean flag) {
		this.allowLocationServices = flag;
	}

	/**
	 * @return - allowLocationServices
	 */
	public boolean allowLocationServices() {
		return allowLocationServices;
	}	
	
	private static String formatLocation(Location loc)
	{
		return "{ lat: " + loc.getLatitude() + ", lon: " + loc.getLongitude() + ", acc: " + loc.getAccuracy() +"}";
	}
	
	/**
	 * Gets the location.
	 *
	 * @return the location
	 */
	public String getLocation() {
		Log.d(LOG_TAG, "getLocation: hasPermission: " + hasPermission);
		if (!hasPermission) {
			return null;
		}
		List<String> providers = mLocationManager.getProviders(true);
		Iterator<String> provider = providers.iterator();
		Location lastKnown = null;
		while (provider.hasNext()) {
			lastKnown = mLocationManager.getLastKnownLocation(provider.next());
			if (lastKnown != null) {
				break;
			}
		}
		Log.d(LOG_TAG, "getLocation: " + lastKnown);
		if (lastKnown != null) {
			return formatLocation(lastKnown);
		} else
			return null;
	}

	/**
	 * Gets the location parameters
	 * as lng= and lat=
	 *
	 * @return the location
	 */
	public String getLocationParams() {
		Log.d(LOG_TAG, "getLocation: hasPermission: " + hasPermission);
		if (!hasPermission) {
			return null;
		}
		List<String> providers = mLocationManager.getProviders(true);
		Iterator<String> provider = providers.iterator();
		Location lastKnown = null;
		while (provider.hasNext()) {
			lastKnown = mLocationManager.getLastKnownLocation(provider.next());
			if (lastKnown != null) {
				break;
			}
		}
		Log.d(LOG_TAG, "getLocation: " + lastKnown);
		if (lastKnown != null) {
			return "&lng="+lastKnown.getLongitude()+"&lat="+lastKnown.getLatitude();
		} else
			return "";
	}
	
	/**
	 * Start location listener.
	 */
	public void startLocationListener() {
		if (mLocListenerCount == 0) {

			if (mNetwork != null)
				mNetwork.start();
			if (mGps != null)
				mGps.start();
		}
		mLocListenerCount++;
	}

	/**
	 * Stop location listener.
	 */
	public void stopLocationListener() {
		mLocListenerCount--;
		if (mLocListenerCount == 0) {

			if (mNetwork != null)
				mNetwork.stop();
			if (mGps != null)
				mGps.stop();
		}
	}

	/**
	 * Success.
	 *
	 * @param loc the loc
	 */
	public void success(Location loc) {
		String script = "window.mraidview.fireChangeEvent({ location: "+ formatLocation(loc) + "})";
		Log.d(LOG_TAG, script);
		mMraidView.injectJavaScript(script);
	}

	/**
	 * Fail.
	 */
	public void fail() {
		Log.e(LOG_TAG, "Location can't be determined");
		mMraidView.injectJavaScript("window.mraidview.fireErrorEvent(\"Location cannot be identified\", \"MraidLocationController\")");
	}

	/* (non-Javadoc)
	 * @see com.ormma.controller.OrmmaController#stopAllListeners()
	 */
	@Override
	public void stopAllListeners() {
		mLocListenerCount = 0;
		try {
			mGps.stop();
		} catch (Exception e) {
		}
		try {
			mNetwork.stop();
		} catch (Exception e) {
		}
	}

}
