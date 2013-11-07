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
import android.webkit.URLUtil;

import com.emediate.view.EmediateView;

public class LoadHTMLAsyncTask extends AsyncTask<Void, String, String> {

    private boolean mReuse = false;

    private Context mContext;
    private EmediateView mEmediateView;
    private String mUrl;
    private static final String HTMLFileName = "EmediateAds.html";

    public LoadHTMLAsyncTask(Context context, String url, EmediateView mView, boolean shouldReuse) {
	mContext = context;
	mUrl = url;
	this.mReuse = shouldReuse;
	this.mEmediateView = mView;
    }

    @Override
    protected String doInBackground(Void... params) {
	if (mReuse) {
	    try {
		return getHTMLFilePath();
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	} else {
	    if (URLUtil.isValidUrl(mUrl)) {
		try {
		    final HttpURLConnection conn = (HttpURLConnection) new URL(mUrl).openConnection();
		    setUserAgent(conn);
		    conn.setDoInput(true);
		    conn.connect();

		    final InputStream input = conn.getInputStream();
		    final File writeFile = getHTMLFile();
		    final byte buff[] = new byte[1024];
		    final FileOutputStream out = new FileOutputStream(writeFile);

		    do {
			int numread = input.read(buff);
			if (numread <= 0)
			    break;
			out.write(buff, 0, numread);

		    } while (true);

		    out.flush();
		    out.close();
		    input.close();

		    return getHTMLFilePath();
		} catch (MalformedURLException e) {
		    e.printStackTrace();
		    return null;
		} catch (IOException e) {
		    e.printStackTrace();
		    return null;
		}
	    }
	}

	return null;
    }

    /**
     * Return a file which represents the last fetched ad.
     */
    protected File getHTMLFile() {
	return new File(mContext.getFilesDir(), HTMLFileName);
    }

    /**
     * Return the path the the file which represents the last fetched ad.
     */
    protected String getHTMLFilePath() {
	return "file://" + getHTMLFile();
    }

    @Override
    protected void onPostExecute(String result) {
	super.onPostExecute(result);
	if (result != null) {
	    mEmediateView.loadUrl(result);
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
