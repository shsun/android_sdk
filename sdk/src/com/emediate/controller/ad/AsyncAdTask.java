/*
 * Created by Fredrik Hyttnäs-Lenngren on 7 nov 2013
 * Copyright (c) 2013 Emediate. All rights reserved.
 */
package com.emediate.controller.ad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

import android.os.AsyncTask;
import android.os.Build;

import com.emediate.view.EmediateView;

/**
 * {@link AsyncTask} for fetching and serving ads to an {@link EmediateView}
 * 
 * @author Fredrik Hyttnäs-Lenngren
 * 
 */
public class AsyncAdTask extends AsyncTask<Integer, Void, String> {

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
    protected String doInBackground(Integer... count) {
	if (mReuseOld) {
	    final File previousAd = mAdCache.peep();
	    if (previousAd.exists()) {
		return toString(previousAd);
	    }
	}

	// There was nothing to reuse, or not reusing
	try {
	    if (!mAdCache.isEmpty()) {
		return toString(mAdCache.pop());
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
	    return toString(mAdCache.pop());
	} catch (IOException e) {
	    e.printStackTrace();
	    return toString(mAdCache.peep()); // Returned the last servered if possible
	}
    }

    /**
     * Converts the contents of the given File to a human-readable string
     * 
     * @param file
     *            the file to convert
     * @return the contents of the file, or null if unable to convert the file
     */
    protected String toString(File file) {

	BufferedReader reader = null;
	try {
	    final StringBuilder builder = new StringBuilder();
	    reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

	    String line;
	    while ((line = reader.readLine()) != null) {
		builder.append(line);
	    }

	    return builder.toString();
	} catch (IOException e) {
	    return null;
	} finally {
	    try {
		if (reader != null)
		    reader.close();
	    } catch (IOException e) {
		// Ignore
	    }
	}
    }

    @Override
    protected void onPostExecute(String result) {
	if (result != null) {
	    mView.loadDataWithBaseURL(null, result, "text/html", "utf-8", null);
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
	userAgentBuilder.append(Locale.getDefault() + "/");

	conn.addRequestProperty("User-Agent", userAgentBuilder.toString());
    }

}
