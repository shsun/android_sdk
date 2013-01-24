package com.emediate.controller.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.emediate.controller.MraidDisplayController;

/**
 * The Class OrmmaConfigurationBroadcastReceiver.
 */
public class MraidConfigurationBroadcastReceiver extends BroadcastReceiver {

	private MraidDisplayController mMraidDisplayController;
	
	/**
	 * The m last orientation.
	 */
	private int mLastOrientation;

	/**
	 * Instantiates a new ormma configuration broadcast receiver.
	 *
	 * @param ormmaDisplayController the ormma display controller
	 */
	public MraidConfigurationBroadcastReceiver(MraidDisplayController mraidDisplayController) {
		mMraidDisplayController = mraidDisplayController;
		mLastOrientation = mMraidDisplayController.getOrientation();
	}

	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_CONFIGURATION_CHANGED)) {
			int orientation = mMraidDisplayController.getOrientation();
			if (orientation != mLastOrientation) {
				mLastOrientation = orientation;
				mMraidDisplayController.onOrientationChanged(mLastOrientation);
			}
		}
	}

}
