package com.emediate.controller.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.emediate.controller.MraidNetworkController;

/**
 * The Class OrmmaNetworkBroadcastReceiver.
 */
public class MraidNetworkBroadcastReceiver extends BroadcastReceiver {

	private MraidNetworkController mMraidNetworkController;

	/**
	 * Instantiates a new ormma network broadcast receiver.
	 *
	 * @param ormmaNetworkController the ormma network controller
	 */
	public MraidNetworkBroadcastReceiver(MraidNetworkController mraidNetworkController) {
		mMraidNetworkController = mraidNetworkController;
	}

	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			mMraidNetworkController.onConnectionChanged();
		}
	}

}
