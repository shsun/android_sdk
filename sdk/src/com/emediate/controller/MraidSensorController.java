package com.emediate.controller;

import android.content.Context;
import android.util.Log;

import com.emediate.controller.listeners.AccelListener;
import com.emediate.view.MraidView;

/**
 * The Class OrmmaSensorController.  OrmmaController for interacting with sensors
 */
public class MraidSensorController extends MraidController {
	private static final String LOG_TAG = "MraidSensorController";
	final int INTERVAL = 1000;
	private AccelListener mAccel;
	private float mLastX = 0;
	private float mLastY = 0;
	private float mLastZ = 0;

	/**
	 * Instantiates a new ormma sensor controller.
	 *
	 * @param adView the ad view
	 * @param context the context
	 */
	public MraidSensorController(MraidView adView, Context context) {
		super(adView, context);
		mAccel = new AccelListener(context, this);
	}

	/**
	 * Start tilt listener.
	 */
	public void startTiltListener() {
		mAccel.startTrackingTilt();
	}

	/**
	 * Start shake listener.
	 */
	public void startShakeListener() {
		mAccel.startTrackingShake();
	}

	/**
	 * Stop tilt listener.
	 */
	public void stopTiltListener() {
		mAccel.stopTrackingTilt();
	}

	/**
	 * Stop shake listener.
	 */
	public void stopShakeListener() {
		mAccel.stopTrackingShake();
	}

	/**
	 * Start heading listener.
	 */
	public void startHeadingListener() {
		mAccel.startTrackingHeading();
	}

	/**
	 * Stop heading listener.
	 */
	public void stopHeadingListener() {
		mAccel.stopTrackingHeading();
	}

	/**
	 * Stop.
	 */
	void stop() {
	}

	/**
	 * On shake.
	 */
	public void onShake() {
		mMraidView.injectJavaScript("Mraid.gotShake()");
	}

	/**
	 * On tilt.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void onTilt(float x, float y, float z) {
		mLastX = x;
		mLastY = y;
		mLastZ = z;
		
		String script = "window.mraidview.fireChangeEvent({ tilt: "+ getTilt() + "})";
		Log.d(LOG_TAG, script );
		mMraidView.injectJavaScript(script);
	}

	/**
	 * Gets the tilt.
	 *
	 * @return the tilt
	 */
	public String getTilt() {
		String tilt = "{ x : \"" + mLastX + "\", y : \"" + mLastY + "\", z : \"" + mLastZ + "\"}";
		Log.d(LOG_TAG, "getTilt: " + tilt);
		return tilt;
	}

	/**
	 * On heading change.
	 *
	 * @param f the f
	 */
	public void onHeadingChange(float f) {
		String script = "window.mraidview.fireChangeEvent({ heading: " + (int) (f * (180 / Math.PI)) + "});";
		Log.d(LOG_TAG, script );
		mMraidView.injectJavaScript(script);
	}

	/**
	 * Gets the heading.
	 *
	 * @return the heading
	 */
	public float getHeading() {
		Log.d(LOG_TAG, "getHeading: " + mAccel.getHeading());
		return mAccel.getHeading();
	}

	/* (non-Javadoc)
	 * @see com.ormma.controller.OrmmaController#stopAllListeners()
	 */
	@Override
	public void stopAllListeners() {
		mAccel.stopAllListeners();
	}
}
