/*
 * Created by Fredrik Hyttnäs-Lenngren on 7 nov 2013
 * Copyright (c) 2013 Emediate. All rights reserved.
 */
package com.emediate.controller.ad;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import com.emediate.view.EmediateView;

import android.os.AsyncTask;
import android.os.Build;

/**
 * {@link AsyncTask} for fetching and serving ads to an {@link EmediateView}
 * 
 * @author Fredrik Hyttnäs-Lenngren
 * 
 */
public class AsyncAdTask extends AsyncTask<Integer, Void, File> {

    private final EmediateView mView;
    private final AdBuffer mAdCache;
    private final String mAdUrl;
    private final boolean mReuseOld;

    /**
     * Create a new {@link AsyncTask}
     * 
     * @param view
     *            {@link EmediateView} where ad should be placed
     * @param buffer
     *            {@link AdBuffer} where ads should be placed/fetched
     * @param adUrl
     *            the url from where the ad should be retrived
     * @param reuse
     *            true to return the last returned ad
     */
    public AsyncAdTask(EmediateView view, AdBuffer buffer, String adUrl, boolean reuse) {
	mView = view;
	mAdCache = buffer;
	mAdUrl = adUrl;
	mReuseOld = reuse;
    }

    @Override
    protected File doInBackground(Integer... count) {
	if (mReuseOld) {
	    final File previousAd = mAdCache.peep();
	    if (previousAd.exists()) {
		return previousAd;
	    }
	}

	// There was nothing to reuse, or not reusing
	try {
	    if (!mAdCache.isEmpty()) {
		return mAdCache.pop();
	    }
	    // The cache was empty, fetch more
	    for (int i = 0; i < count[0]; i++) {
		final HttpURLConnection conn = (HttpURLConnection) new URL(mAdUrl).openConnection();
		setUserAgent(conn);
		conn.setDoInput(true);
		conn.connect();

		final InputStream input = conn.getInputStream();
		mAdCache.put(input);
		input.close();
	    }
	    return mAdCache.pop();
	} catch (IOException e) {
	    e.printStackTrace();
	    return mAdCache.peep(); // Returned the last servered if possible
	}
    }

    @Override
    protected void onPostExecute(File result) {
	if (result != null) {
	    mView.loadUrl("file://" + result);
	}
    }

    /**
     * Sets the <code>User-Agent</code> request property for identifying this
     * device.
     * 
     * @param conn
     *            connection on which user-agent should be set
     */
    public void setUserAgent(URLConnection conn) {
	final StringBuilder userAgentBuilder = new StringBuilder();
	userAgentBuilder.append("Emediate SDK 1.0/");
	userAgentBuilder.append("Android " + Build.VERSION.RELEASE + ";");
	userAgentBuilder.append(Build.MODEL + " Build/");

	conn.addRequestProperty("User-Agent", userAgentBuilder.toString());
    }

}
