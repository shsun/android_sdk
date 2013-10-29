package com.emediate.controller.async;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.webkit.URLUtil;

import com.emediate.view.EmediateView;

public class LoadHTMLAsynTask extends AsyncTask<Void, String, String> {

    private boolean isLoaded = false;
    private String finalUrl;
    private Context mContext;
    private EmediateView mEmediateView;
    private String mUrl;
    private static final String HTMLFileName = "EmediateAds.html";

    public LoadHTMLAsynTask(Context context, String url, View mView, boolean isLoaded) {
	mContext = context;
	mUrl = url;
	this.isLoaded = isLoaded;
	this.mEmediateView = (EmediateView) mView;
    }

    @Override
    protected String doInBackground(Void... params) {
	String url = mUrl;

	if (isLoaded) {
	    try {
		File writeFile = new File(mContext.getFilesDir(), HTMLFileName);
		return finalUrl = "file://" + writeFile.getAbsolutePath();
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	} else {

	    if (URLUtil.isValidUrl(url)) {
		InputStream is = null;
		try {
		    URL u = new URL(url);
		    HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		    setUserAgent(conn);
		    conn.setDoInput(true);
		    conn.connect();

		    is = conn.getInputStream();
		    try {
			File writeFile = new File(mContext.getFilesDir(), HTMLFileName);
			byte buff[] = new byte[1024];
			FileOutputStream out = new FileOutputStream(writeFile);

			do {
			    int numread = is.read(buff);
			    if (numread <= 0)
				break;
			    out.write(buff, 0, numread);

			} while (true);

			out.flush();
			out.close();

			finalUrl = "file://" + writeFile.getAbsolutePath();

		    } catch (Exception e) {
			e.printStackTrace();
		    }
		    is.close();
		    return finalUrl;

		} catch (MalformedURLException e) {
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	}

	return null;
    }

    @Override
    protected void onPostExecute(String result) {
	super.onPostExecute(result);
	if (result != null) {
	    mEmediateView.loadUrl(result);
	    mEmediateView.setAdsIsOrientationUpdated(false);
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
