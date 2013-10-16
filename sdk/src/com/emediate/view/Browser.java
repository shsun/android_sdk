package com.emediate.view;

import java.io.InputStream;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.emediate.controller.MraidAssetController;

/**
 * Activity for implementing Ormma open calls. Configurable via the following
 * extras: URL_EXTRA String : url to load SHOW_BACK_EXTRA boolean (default
 * false) : show the back button SHOW_FORWARD_EXTRA boolean (default false) :
 * show the forward button SHOW_REFRESH_EXTRA boolean (default false) : show the
 * prefresh button
 * 
 * layout is constructed programatically
 */
public class Browser extends Activity {

	/** Extra Constants **/
	public static final String URL_EXTRA = "extra_url";
	public static final String SHOW_BACK_EXTRA = "open_show_back";
	public static final String SHOW_FORWARD_EXTRA = "open_show_forward";
	public static final String SHOW_REFRESH_EXTRA = "open_show_refresh";

	/** Layout Id constants. */
	private static final int ButtonId = 100;
	private static final int WebViewId = 101;
	private static final int ForwardId = 102;
	private static final int BackwardId = 103;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@SuppressLint({ "ResourceAsColor", "ResourceAsColor", "ResourceAsColor", "ResourceAsColor", "SetJavaScriptEnabled" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Build the layout
		RelativeLayout rl = new RelativeLayout(this);
		WebView webview = new WebView(this);

		this.getWindow().requestFeature(Window.FEATURE_PROGRESS);
		getWindow().setFeatureInt(Window.FEATURE_PROGRESS,
				Window.PROGRESS_VISIBILITY_ON);

		Intent i = getIntent();

		// Build the button bar
		LinearLayout bll = new LinearLayout(this);
		bll.setOrientation(LinearLayout.HORIZONTAL);
		bll.setId(ButtonId);
		bll.setWeightSum(100);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT,
				RelativeLayout.LayoutParams.FILL_PARENT);
		lp.addRule(RelativeLayout.ABOVE, ButtonId);
		BitmapDrawable bkgDrawable = new BitmapDrawable(
				bitmapFromJar("bkgrnd.png"));
		bll.setBackgroundDrawable(bkgDrawable);
		rl.addView(webview, lp);

		lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		rl.addView(bll, lp);

		LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.FILL_PARENT);
		lp2.weight = 25;
		lp2.gravity = Gravity.CENTER_VERTICAL;

		ImageButton backButton = new ImageButton(this);
		backButton.setBackgroundColor(android.R.color.transparent);
		backButton.setId(BackwardId);

		bll.addView(backButton, lp2);
		if (!i.getBooleanExtra(SHOW_BACK_EXTRA, true))

			backButton.setVisibility(ViewGroup.GONE);
		backButton.setImageBitmap(bitmapFromJar("leftarrow.png"));

		backButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(android.view.View arg0) {
				WebView wv = (WebView) findViewById(WebViewId);
				if (wv.canGoBack())
					wv.goBack();
				else
					Browser.this.finish();
			}
		});

		ImageButton forwardButton = new ImageButton(this);
		forwardButton.setBackgroundColor(android.R.color.transparent);
		forwardButton.setId(ForwardId);
		lp2 = new LinearLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.FILL_PARENT);
		lp2.weight = 25;
		lp2.gravity = Gravity.CENTER_VERTICAL;

		bll.addView(forwardButton, lp2);
		if (!i.getBooleanExtra(SHOW_FORWARD_EXTRA, true))
			forwardButton.setVisibility(ViewGroup.GONE);
		forwardButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(android.view.View arg0) {
				WebView wv = (WebView) findViewById(WebViewId);
				wv.goForward();
			}
		});

		ImageButton refreshButton = new ImageButton(this);
		refreshButton.setImageBitmap(bitmapFromJar("refresh.png"));
		refreshButton.setBackgroundColor(android.R.color.transparent);
		lp2 = new LinearLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp2.weight = 25;
		lp2.gravity = Gravity.CENTER_VERTICAL;

		bll.addView(refreshButton, lp2);
		if (!i.getBooleanExtra(SHOW_REFRESH_EXTRA, true))

			refreshButton.setVisibility(ViewGroup.GONE);
		refreshButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(android.view.View arg0) {
				WebView wv = (WebView) findViewById(WebViewId);
				wv.reload();
			}
		});

		ImageButton closeButton = new ImageButton(this);
		closeButton.setImageBitmap(bitmapFromJar("close.png"));
		closeButton.setBackgroundColor(android.R.color.transparent);
		lp2 = new LinearLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp2.weight = 25;
		lp2.gravity = Gravity.CENTER_VERTICAL;

		bll.addView(closeButton, lp2);
		closeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(android.view.View arg0) {
				Browser.this.finish();
			}
		});

		// Show progress bar
		getWindow().requestFeature(Window.FEATURE_PROGRESS);

		// Enable cookies
		CookieSyncManager.createInstance(this);
		CookieSyncManager.getInstance().startSync();
		webview.getSettings().setJavaScriptEnabled(true);
		webview.loadUrl(i.getStringExtra(URL_EXTRA));
		webview.setId(WebViewId);

		webview.setWebViewClient(new WebViewClient() {
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				Activity a = (Activity) view.getContext();
				Toast.makeText(a, "Mraid Error:" + description,
						Toast.LENGTH_SHORT).show();
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				ImageButton forwardButton = (ImageButton) findViewById(ForwardId);
				forwardButton
						.setImageBitmap(bitmapFromJar("unrightarrow.png"));
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				ImageButton forwardButton = (ImageButton) findViewById(ForwardId);

				// grey out buttons when appropriate
				if (view.canGoForward()) {
					forwardButton
							.setImageBitmap(bitmapFromJar("rightarrow.png"));
				} else {
					forwardButton
							.setImageBitmap(bitmapFromJar("unrightarrow.png"));
					
				}

			}
		});
		setContentView(rl);

		webview.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				// show progress bar while loading, url when loaded
				Activity a = (Activity) view.getContext();
				a.setTitle("Loading...");
				a.setProgress(progress * 100);
				if (progress == 100)
					a.setTitle(view.getUrl());
			}
		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		CookieSyncManager.getInstance().stopSync();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		CookieSyncManager.getInstance().startSync();
	}

	/**
	 * Accessing a jar in the normal way leaves a referece to the entire jar so
	 * this function is used to minimize memory usage
	 * 
	 * @param the
	 *            file to pull from the jar
	 * @return the bitmap
	 */
	public Bitmap bitmapFromJar(String source) {
		InputStream in = null;
		try {
			URL url = MraidAssetController.class.getClassLoader().getResource(
					source);
			String file = url.getFile();
			if (file.startsWith("file:")) {
				file = file.substring(5);
			}
			int pos = file.indexOf("!");
			if (pos > 0)
				file = file.substring(0, pos);
			JarFile jf = new JarFile(file);
			JarEntry entry = jf.getJarEntry(source);
			in = jf.getInputStream(entry);
			Bitmap bmp = BitmapFactory.decodeStream(in);
			return bmp;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
				in = null;
			}
		}
		return null;
	}
}
