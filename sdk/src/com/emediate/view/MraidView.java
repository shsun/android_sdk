package com.emediate.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import android.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.webkit.JsResult;
import android.webkit.URLUtil;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.emediate.controller.MraidController.Dimensions;
import com.emediate.controller.MraidController.PlayerProperties;
import com.emediate.controller.MraidController.Properties;
import com.emediate.controller.MraidUtilityController;
import com.emediate.controller.util.MraidPlayer;
import com.emediate.controller.util.MraidPlayerListener;
import com.emediate.controller.util.MraidUtils;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

/**
 * This is the view to place into a layout to implement ormma functionality. It can be used either via xml or
 * programatically
 * 
 * It is a subclass of the standard WebView which brings with it all the standard functionality as well as the inherent
 * bugs on some os versions.
 * 
 * Webviews have a tendency to leak on orientation in older versions of the android OS this can be minimized by using an
 * application context, but this will break the launching of subwindows (such as alert calls from javascript)
 * 
 * It is important to not use any of the layout constants elsewhere in the same view as things will get confused.
 * Normally this is not an issue as generated layout constants will not interfere.
 */
public class MraidView extends WebView implements OnGlobalLayoutListener {

	private static final String LOG_TAG = "View";

	/**
	 * Included by David
	 * 
	 * @author leiwang
	 * 
	 */
	public enum ViewState {
		LOADING, DEFAULT, RESIZED, EXPANDED, HIDDEN, LEFT_BEHIND, OPENED;
	}

	// static for accessing xml attributes
	private static int[] attrs = { R.attr.maxWidth, R.attr.maxHeight };

	// Messaging constants
	private static final int MESSAGE_RESIZE = 1000;
	private static final int MESSAGE_CLOSE = 1001;
	private static final int MESSAGE_HIDE = 1002;
	private static final int MESSAGE_SHOW = 1003;
	private static final int MESSAGE_EXPAND = 1004;
	private static final int MESSAGE_SEND_EXPAND_CLOSE = 1005;
	private static final int MESSAGE_OPEN = 1006;
	private static final int MESSAGE_PLAY_VIDEO = 1007;
	private static final int MESSAGE_PLAY_AUDIO = 1008;
	private static final int MESSAGE_RAISE_ERROR = 1009;

	// Extra constants
	public static final String DIMENSIONS = "expand_dimensions";
	public static final String PLAYER_PROPERTIES = "player_properties";
	public static final String EXPAND_URL = "expand_url";
	public static final String ACTION_KEY = "action";
	private static final String EXPAND_PROPERTIES = "expand_properties";
	private static final String RESIZE_WIDTH = "resize_width";
	private static final String RESIZE_HEIGHT = "resize_height";
	private static final String CURRENT_FILE = "_mraid_current";
	private static final String AD_PATH = "AD_PATH";
	private static final String ERROR_MESSAGE = "message";
	private static final String ERROR_ACTION = "action";

	// Debug message constant
	private static final String TAG = "View";

	// layout constants
	protected static final int BACKGROUND_ID = 101;
	protected static final int PLACEHOLDER_ID = 100;
	public static final int MRAID_ID = 102;

	// private constants
	private TimeOut mTimeOut; // timeout for loading a url
	private static String mScriptPath/* = null */; // holds the path for the ormma.js
	private static String mBridgeScriptPath /* = null */; // holds the path for the
	// ormma_bridge.js
	private boolean bPageFinished /* = false */; // boolean flag holding the loading
	// state of a page
	private MraidUtilityController mUtilityController; // primary javascript
	// bridge
	private float mDensity; // screen pixel density
	private int mContentViewHeight; // height of the content
	private boolean bKeyboardOut; // state of the keyboard
	private int mDefaultHeight; // default height of the view
	private int mDefaultWidth; // default width of the view
	private int mInitLayoutHeight; // initial height of the view
	private int mInitLayoutWidth; // initial height of the view
	private int mIndex; // index of the view within its viewgroup
	private GestureDetector mGestureDetector; // gesture detector for capturing
	// unwanted gestures
	private ViewState mViewState = ViewState.DEFAULT; // holds current view
														// state
	// state
	private MraidViewListener mListener; // listener for communicated events
	private static MraidPlayer player;
	// (back to the parent)
	// public String mDataToInject = null; // javascript to inject into the view
	private String mLocalFilePath; // local path the the ad html

	// URL Protocols registered by the client.
	// if such a protocol is encountered then
	// shouldOverrideUrlLoading will forward the url to the listener
	// by calling handleRequest
	// Should this be a static variable?

	private final HashSet<String> registeredProtocols = new HashSet<String>();

	private int mDefaultPositionX = 0, mDefaultPositionY = 0;

	public int getDefaultPositionX() {
		return mDefaultPositionX;
	}

	public int getDefaultPositionY() {
		return mDefaultPositionY;
	}
	
	@Override
	protected void onLayout (boolean changed, int left, int top, int right, int bottom){
		mDefaultPositionX = left;
		mDefaultPositionY = top;
		if(mDefaultPositionY == 0 || mDefaultPositionX == 0){
			int[] location = new int[2];
			getLocationOnScreen(location);
			mDefaultPositionX = location[0];
			mDefaultPositionY = location[1];
			
		}
	}

	public enum ACTION {
		PLAY_AUDIO, PLAY_VIDEO
	}

	private String mapAPIKey;

	/**
	 * InstMraidtes a new ormma view.
	 * 
	 * @param context
	 *            the context
	 * @param listener
	 *            the listener
	 */
	public MraidView(Context context, MraidViewListener listener) {
		super(context);
		setListener(listener);
		initialize();
	}

	public void setMapAPIKey(String key) {
		this.mapAPIKey = key;
	}

	/**
	 * Sets the listener.
	 * 
	 * @param listener
	 *            the new listener
	 */
	public void setListener(MraidViewListener listener) {
		mListener = listener;
	}

	/**
	 * Removes the listener.
	 */
	public void removeListener() {
		mListener = null;
	}

	/**
	 * Instantiates a new ormma view.
	 * 
	 * @param context
	 *            the context
	 */
	public MraidView(Context context) {
		super(context);
		initialize();
	}

	public MraidView(Context context, String mapAPIKey) {
		super(context);

		if (!(context instanceof MapActivity)) {
			throw new IllegalArgumentException("MapActivity context required");
		}

		this.mapAPIKey = mapAPIKey;

		initialize();
	}

	public MraidView(Context context, String mapAPIKey, MraidViewListener listener) {
		super(context);

		if (!(context instanceof MapActivity)) {
			throw new IllegalArgumentException("MapActivity context required");
		}

		this.mapAPIKey = mapAPIKey;

		setListener(listener);

		initialize();
	}

	/**
	 * Sets the max size.
	 * 
	 * @param w
	 *            the width
	 * @param h
	 *            the height
	 */
	public void setMaxSize(int w, int h) {
		mUtilityController.setMaxSize(w, h);
	}

	/**
	 * Register a protocol
	 * 
	 * @param protocol
	 *            the protocol to be registered
	 */

	public void registerProtocol(String protocol) {
		if (protocol != null)
			registeredProtocols.add(protocol.toLowerCase());
	}

	/**
	 * Deregister a protocol
	 * 
	 * @param protocol
	 *            the protocol to be de registered
	 */

	public void deregisterProtocol(String protocol) {
		if (protocol != null)
			registeredProtocols.remove(protocol.toLowerCase());
	}

	/**
	 * Is Protocol Registered
	 * 
	 * @param uri
	 *            The uri
	 * @return true , if the url's protocol is registered by the user, else false if scheme is null or not registered
	 */
	private boolean isRegisteredProtocol(Uri uri) {

		String scheme = uri.getScheme();
		if (scheme == null)
			return false;

		for (String protocol : registeredProtocols) {
			if (protocol.equalsIgnoreCase(scheme))
				return true;
		}
		return false;
	}

	/**
	 * The listener interface for receiving ormmaView events. The class that is interested in processing a ormmaView
	 * event implements this interface, and the object created with that class is registered with a component using the
	 * component's <code>addOrmmaViewListener<code> method. When
	 * the ormmaView event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OrmmaViewEvent
	 */
	public interface MraidViewListener {

		/**
		 * On ready.
		 * 
		 * @return true, if successful
		 */
		abstract boolean onReady();

		/**
		 * On resize.
		 * 
		 * @return true, if successful
		 */
		abstract boolean onResize();

		/**
		 * On expand.
		 * 
		 * @return true, if successful
		 */
		abstract boolean onExpand();

		/**
		 * On expand close.
		 * 
		 * @return true, if successful
		 */
		abstract boolean onExpandClose();

		/**
		 * On resize close.
		 * 
		 * @return true, if successful
		 */
		abstract boolean onResizeClose();

		/**
		 * On event fired.
		 * 
		 * @return true, if successful
		 */
		abstract boolean onEventFired();

		/**
		 * On Handling Requests
		 * 
		 * @param url
		 *            The url whose protocol has been registered.
		 */

		abstract void handleRequest(String url);
	}

	/**
	 * Inject java script into the view
	 * 
	 * @param str
	 *            the java script to inject
	 */
	public void injectJavaScript(String str) {
		if (str != null)
			super.loadUrl("javascript:" + str);
	}

	/**
	 * Load a url into the view
	 * 
	 * @param url
	 *            the url
	 * @param dataToInject
	 *            any additional javascript to inject
	 */
	public void loadUrl(String url, String dataToInject) {
		loadUrl(url, false, dataToInject);
	}

	/*
	 * @see android.webkit.WebView#loadUrl(java.lang.String)
	 */
	@Override
	public void loadUrl(String url) {
		loadUrl(url, false, null);
	}

	/**
	 * Load view from html in a local file
	 * 
	 * @param f
	 *            the file
	 * @param dataToInject
	 *            any additional javascript to inject
	 */
	public void loadFile(File f, String dataToInject) {
		try {
			loadInputStream(new FileInputStream(f), dataToInject);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * The Class TimeOut. A timertask for terminating the load if it takes too long If it fires three times without
	 * making progress, it will cancel the load
	 */
	class TimeOut extends TimerTask {

		int mProgress = 0;
		int mCount = 0;

		@Override
		public void run() {
			Activity activity = (Activity) MraidView.this.getContext();
			activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {

					int progress = getProgress();
					if (progress == 100) {
						TimeOut.this.cancel();
					}
					else {
						if (mProgress == progress) {
							mCount++;
							if (mCount == 3) {
								try {
									stopLoading();
								}
								catch (Exception e) {
									Log.w(TAG, "error in stopLoading");
									e.printStackTrace();
								}
								TimeOut.this.cancel();
							}
						}
					}
					mProgress = progress;
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.webkit.WebView#clearView()
	 */
	@Override
	public void clearView() {
		reset();
		super.clearView();
	}

	/**
	 * Reset the view.
	 */
	private void reset() {
		if (mViewState == ViewState.EXPANDED) {
			closeExpanded();
		}
		else if (mViewState == ViewState.RESIZED) {
			closeResized();
		}
		invalidate();
		mUtilityController.deleteOldAds();
		mUtilityController.stopAllListeners();
		resetLayout();
	}

	/**
	 * Loads the view from an input stream. Does the real loading work
	 * 
	 * @param is
	 *            the input stream
	 * @param dataToInject
	 *            the data to inject
	 */
	private void loadInputStream(InputStream is, String dataToInject) {
		String url;
		reset();
		if (mTimeOut != null) {
			mTimeOut.cancel();
		}
		mTimeOut = new TimeOut();

		try {
			mLocalFilePath = mUtilityController.writeToDiskWrap(this, is, CURRENT_FILE, true, dataToInject,
					mBridgeScriptPath, mScriptPath);

			url = "file://" + mLocalFilePath + CURRENT_FILE;
			Timer timer = new Timer();
			timer.schedule(mTimeOut, 2000, 2000);
			if (dataToInject != null) {
				injectJavaScript(dataToInject);
			}

			super.loadUrl(url);
		}
		catch (IllegalStateException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (is != null) {
				try {
					is.close();
				}
				catch (Exception e) {
				}
			}
			is = null;
		}
	}

	/**
	 * Load url.
	 * 
	 * @param url
	 *            the url
	 * @param dontLoad
	 *            the dont load
	 * @param dataToInject
	 *            any additional javascript to inject
	 */
	public void loadUrl(String url, boolean dontLoad, String dataToInject) {
		if (URLUtil.isValidUrl(url)) {
			if (!dontLoad) {
				InputStream is = null;
				bPageFinished = false;
				try {
					URL u = new URL(url);
					String name = u.getFile();
					// if it is in the asset directory use the assetmanager
					if (url.startsWith("file:///android_asset/")) {
						name = url.replace("file:///android_asset/", "");
						AssetManager am = getContext().getAssets();
						is = am.open(name);
					}
					else {
						is = u.openStream();
					}
					loadInputStream(is, dataToInject);
					return;

				}
				catch (MalformedURLException e) {
				}
				catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
			super.loadUrl(url);
		}
	}

	/**
	 * Instantiates a new Mraid view.
	 * 
	 * @param context
	 *            the context
	 * @param set
	 *            the set
	 */
	public MraidView(Context context, AttributeSet set) {
		super(context, set);

		initialize();

		TypedArray a = getContext().obtainStyledAttributes(set, attrs);

		int w = a.getDimensionPixelSize(0, -1);
		int h = a.getDimensionPixelSize(1, -1);

		if (w > 0 && h > 0)
			mUtilityController.setMaxSize(w, h);

		a.recycle();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.webkit.WebView#onTouchEvent(android.view.MotionEvent)
	 * 
	 * used for trapping scroll events
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		boolean ret = mGestureDetector.onTouchEvent(ev);
		if (ret)
			ev.setAction(MotionEvent.ACTION_CANCEL);
		return super.onTouchEvent(ev);
	}

	/**
	 * The message handler. To keep things in the ui thread.
	 */
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			Bundle data = msg.getData();
			switch (msg.what) {
				case MESSAGE_SEND_EXPAND_CLOSE:
					Log.d("handler", "message_expand_close!");
					if (mListener != null) {
						mListener.onExpandClose();
					}
					break;
				case MESSAGE_RESIZE: {
					mViewState = ViewState.RESIZED;
					ViewGroup.LayoutParams lp = getLayoutParams();
					lp.height = data.getInt(RESIZE_HEIGHT, lp.height);
					lp.width = data.getInt(RESIZE_WIDTH, lp.width);
					String injection = "window.mraidview.fireChangeEvent({ state: \'resized\'," + " size: { width: "
							+ lp.width + ", " + "height: " + lp.height + "}});";
					injectJavaScript(injection);
					requestLayout();
					if (mListener != null)
						mListener.onResize();
					break;
				}
				case MESSAGE_CLOSE: {
					Log.d("handler", "message_close!");
					switch (mViewState) {
						case RESIZED:
							closeResized();
							break;
						case EXPANDED:
							closeExpanded();
							break;
					}

					break;
				}
				case MESSAGE_HIDE: {
					Log.d("handler", "message_hide!");
					setVisibility(View.INVISIBLE);
					String injection = "window.mraidview.fireChangeEvent({ state: \'hidden\' });";

					injectJavaScript(injection);
					break;
				}
				case MESSAGE_SHOW: {
					Log.d("handler", "message_show!");
					String injection = "window.mraidview.fireChangeEvent({ state: \'default\' });";
					injectJavaScript(injection);
					setVisibility(View.VISIBLE);
					break;
				}
				case MESSAGE_EXPAND: {
					Log.d("handler", "message_expand!");
					doExpand(data);
					break;
				}
				case MESSAGE_OPEN: {
					mViewState = ViewState.LEFT_BEHIND;
					break;
				}

				case MESSAGE_PLAY_AUDIO: {
					playAudioImpl(data);
					break;
				}

				case MESSAGE_PLAY_VIDEO: {
					playVideoImpl(data);
					break;
				}
				case MESSAGE_RAISE_ERROR:
					String strMsg = data.getString(ERROR_MESSAGE);
					String action = data.getString(ERROR_ACTION);
					String injection = "window.mraidview.fireErrorEvent(\"" + strMsg + "\", \"" + action + "\")";
					injectJavaScript(injection);
					break;
			}
			super.handleMessage(msg);
		}

	};

	/**
	 * Change ad display to new dimensions
	 * 
	 * @param d
	 *            - display dimensions
	 * 
	 */
	private FrameLayout changeContentArea(Dimensions d) {

		FrameLayout contentView = (FrameLayout) getRootView().findViewById(R.id.content);
		ViewGroup parent = (ViewGroup) getParent();
		FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams((int) (d.width), (int) (d.height));
		fl.topMargin = 0; 
		fl.leftMargin = 0; 

		int index = 0;
		int count = parent.getChildCount();
		for (index = 0; index < count; index++) {
			if (parent.getChildAt(index) == MraidView.this)
				break;
		}
		mIndex = index;
		FrameLayout placeHolder = new FrameLayout(getContext());
		placeHolder.setId(PLACEHOLDER_ID);
		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(getWidth(), getHeight());
		parent.addView(placeHolder, index, lp);
		parent.removeView(MraidView.this);

		FrameLayout backGround = new FrameLayout(getContext());

		backGround.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				Log.i("MRAID", "background touch called");
				return true;
			}
		});
		FrameLayout.LayoutParams bgfl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,
				FrameLayout.LayoutParams.FILL_PARENT);
		backGround.setId(BACKGROUND_ID);
		backGround.setPadding((int) (d.x), (int) (d.y), 0, 0);
		backGround.addView(MraidView.this, fl);
		contentView.addView(backGround, bgfl);

		return backGround;
	}

	/**
	 * Do the real work of an expand
	 */
	private void doExpand(Bundle data) {

		Dimensions d = (Dimensions) data.getParcelable(DIMENSIONS);
		String url = data.getString(EXPAND_URL);
		Properties p = data.getParcelable(EXPAND_PROPERTIES);
		if (URLUtil.isValidUrl(url)) {
			loadUrl(url);
		}

		FrameLayout backGround = changeContentArea(d);

		if (p.useBackground) {
			int color = p.backgroundColor | ((int) (p.backgroundOpacity * 0xFF) * 0x10000000);
			backGround.setBackgroundColor(color);
		}

		String injection = "window.mraidview.fireChangeEvent({ state: \'expanded\'," + " size: " + "{ width: "
				+ (int) (d.width / mDensity) + ", " + "height: " + (int) (d.height / mDensity) + "}" + " });";
		Log.d(LOG_TAG, "doExpand: injection: " + injection);
		injectJavaScript(injection);
		if (mListener != null)
			mListener.onExpand();
		mViewState = ViewState.EXPANDED;
	}

	/**
	 * Close resized.
	 */
	private void closeResized() {
		if (mListener != null)
			mListener.onResizeClose();
		String injection = "window.mraidview.fireChangeEvent({ state: \'default\'," + " size: " + "{ width: "
				+ mDefaultWidth + ", " + "height: " + mDefaultHeight + "}" + "});";
		Log.d(LOG_TAG, "closeResized: injection: " + injection);
		injectJavaScript(injection);
		resetLayout();
	}
	
	public boolean embeddedBrowserOpened = false;
	public void setEmebeddedBrowserOpen(boolean embeddedBrowserOpened){
		this.embeddedBrowserOpened = embeddedBrowserOpened;
	}
	public boolean getEmbeddedBrowserOpened(){
		return this.embeddedBrowserOpened;
	}
	

	/**
	 * The webview client used for trapping certain events
	 */
	WebViewClient mWebViewClient = new WebViewClient() {

		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			Log.d("MraidView-WebViewClient", "error:" + description);
			super.onReceivedError(view, errorCode, description, failingUrl);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			mDefaultHeight = (int) (getHeight() / mDensity);
			mDefaultWidth = (int) (getWidth() / mDensity);

			mUtilityController.init(mDensity);

		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Uri uri = Uri.parse(url);
			try {
				// If the protocol is registered then forward it to listener.
				if (mListener != null && isRegisteredProtocol(uri)) {
					mListener.handleRequest(url);
					return true;
				}

				if (url.startsWith("tel:")) {
					Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					getContext().startActivity(intent);
					return true;
				}

				if (url.startsWith("mailto:")) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					getContext().startActivity(intent);
					return true;
				}

				if (url.startsWith("mraid://")) {
					return true;
				}

				if(!getEmbeddedBrowserOpened()){
					 Intent intent = new Intent();
					 intent.setAction(android.content.Intent.ACTION_VIEW);
					 intent.setData(uri);
					 intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					 getContext().startActivity(intent);
				}else{
					setEmebeddedBrowserOpen(false);
				}
				return true;

			}
			catch (Exception e) {
				try {
					Intent intent = new Intent();
					intent.setAction(android.content.Intent.ACTION_VIEW);
					intent.setData(uri);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					getContext().startActivity(intent);
					return true;
				}
				catch (Exception e2) {
					return false;
				}
			}

		}

		public void onLoadResource(WebView view, String url) {
			Log.d(TAG, "lr:" + url);
		};

	};

	/**
	 * The m web chrome client.
	 */
	WebChromeClient mWebChromeClient = new WebChromeClient() {

		@Override
		public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
			Log.d("MraidView-WebChromeClient", message);
			return false;
		}
	};

	/**
	 * The b got layout params.
	 */
	private boolean bGotLayoutParams;

	/**
	 * Initialize the view
	 */
	@SuppressLint("SetJavaScriptEnabled")
	private void initialize() {

		setScrollContainer(false);
		setVerticalScrollBarEnabled(false);
		setHorizontalScrollBarEnabled(false);
		mGestureDetector = new GestureDetector(new ScrollEater());

		setBackgroundColor(0);
		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);

		wm.getDefaultDisplay().getMetrics(metrics);
		mDensity = metrics.density;

		bPageFinished = false;

		mUtilityController = new MraidUtilityController(this, this.getContext());

		addJavascriptInterface(mUtilityController, "MRAIDUtilityControllerBridge");

		setWebViewClient(mWebViewClient);
		setWebChromeClient(mWebChromeClient);
		setScriptPath();

		mContentViewHeight = getContentViewHeight();
		getViewTreeObserver().addOnGlobalLayoutListener(this);
		getSettings().setJavaScriptEnabled(true);
	}

	public void addJavascriptObject(Object obj, String name) {
		addJavascriptInterface(obj, name);
	}

	/**
	 * Gets the content view height.
	 * 
	 * @return the content view height
	 */
	private int getContentViewHeight() {
		View contentView = getRootView().findViewById(R.id.content);
		if (contentView != null) {
			return contentView.getHeight();
		}
		else
			return -1;
	}

	/**
	 * Sets the script path.
	 */
	private synchronized void setScriptPath() {
		if (mScriptPath == null) {
			mScriptPath = mUtilityController.copyTextFromJarIntoAssetDir("/mraid.js", "/mraid.js");
		}
		if (mBridgeScriptPath == null) {
			mBridgeScriptPath = mUtilityController.copyTextFromJarIntoAssetDir("/mraid_bridge.js", "/mraid_bridge.js");
		}

	}

	/**
	 * Close an expanded view.
	 */
	protected synchronized void closeExpanded() {

		resetContents();

		String injection = "window.mraidview.fireChangeEvent({ state: \'default\'," + " size: " + "{ width: "
				+ mDefaultWidth + ", " + "height: " + mDefaultHeight + "}" + "});";
		Log.d(LOG_TAG, "closeExpanded: injection: " + injection);
		injectJavaScript(injection);

		mViewState = ViewState.DEFAULT;

		mHandler.sendEmptyMessage(MESSAGE_SEND_EXPAND_CLOSE);
		setVisibility(VISIBLE);
	}

	/**
	 * Close an opened view.
	 * 
	 * @param openedFrame
	 *            the opened frame
	 */
	protected void closeOpened(View openedFrame) {
		((ViewGroup) ((Activity) getContext()).getWindow().getDecorView()).removeView(openedFrame);
		requestLayout();
	}

	/**
	 * Gets the state.
	 * 
	 * @return the state
	 */
	public String getState() {
		return mViewState.toString().toLowerCase();
	}

	/**
	 * Resize the view
	 * 
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	public void resize(int width, int height) {
		Message msg = mHandler.obtainMessage(MESSAGE_RESIZE);

		Bundle data = new Bundle();
		data.putInt(RESIZE_WIDTH, width);
		data.putInt(RESIZE_HEIGHT, height);
		msg.setData(data);

		mHandler.sendMessage(msg);
	}

	/**
	 * Close the view
	 */
	public void close() {
		mHandler.sendEmptyMessage(MESSAGE_CLOSE);
	}

	/**
	 * Hide the view
	 */
	public void hide() {
		mHandler.sendEmptyMessage(MESSAGE_HIDE);
	}

	/**
	 * Show the view
	 */
	public void show() {
		mHandler.sendEmptyMessage(MESSAGE_SHOW);
	}

	/**
	 * Gets the connectivity manager.
	 * 
	 * @return the connectivity manager
	 */
	public ConnectivityManager getConnectivityManager() {
		return (ConnectivityManager) this.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	/**
	 * Dump.
	 */
	public void dump() {
	}

	/**
	 * creates an expand message and throws it to the handler for the real work
	 * 
	 * @param dimensions
	 *            the dimensions
	 * @param URL
	 *            the uRL
	 * @param properties
	 *            the properties
	 */
	public void expand(Dimensions dimensions, String URL, Properties properties) {
		Message msg = mHandler.obtainMessage(MESSAGE_EXPAND);

		Bundle data = new Bundle();
		data.putParcelable(DIMENSIONS, dimensions);
		data.putString(EXPAND_URL, URL);
		data.putParcelable(EXPAND_PROPERTIES, properties);
		msg.setData(data);

		mHandler.sendMessage(msg);
	}

	/**
	 * Open.
	 * 
	 * @param url
	 *            the url
	 * @param back
	 *            show the back button
	 * @param forward
	 *            show the forward button
	 * @param refresh
	 *            show the refresh button
	 */
	public void open(String url, boolean back, boolean forward, boolean refresh) {

		Intent i = new Intent(getContext(), Browser.class);
		Log.d(TAG, "open:" + url);
		i.putExtra(Browser.URL_EXTRA, url);
		i.putExtra(Browser.SHOW_BACK_EXTRA, back);
		i.putExtra(Browser.SHOW_FORWARD_EXTRA, forward);
		i.putExtra(Browser.SHOW_REFRESH_EXTRA, refresh);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getContext().startActivity(i);

	}

	/**
	 * Open Map
	 * 
	 * @param url
	 *            - map url
	 * @param fullscreen
	 *            - should map be shown in full screen
	 */
	public void openMap(String POI, boolean fullscreen) {

		Log.d(TAG, "Opening Map Url " + POI);

		POI = POI.trim();
		POI = MraidUtils.convert(POI);

		if (fullscreen) {
			try {
				// start google maps
				Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(POI));
				mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				getContext().startActivity(mapIntent);

			}
			catch (ActivityNotFoundException e) {
				e.printStackTrace();
			}
		}
		else {
			// if not fullscreen, display map in current OrmmaView space
			if (mapAPIKey != null) {

				try {
					// java.lang.RuntimeException: stub
					MapView mapView = new MapView(getContext(), mapAPIKey);
					mapView.setBuiltInZoomControls(true);

				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				Toast.makeText(getContext(), "Error: no Google Maps API Key provided for embedded map",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	public void playAudioImpl(Bundle data) {

		PlayerProperties properties = (PlayerProperties) data.getParcelable(PLAYER_PROPERTIES);

		String url = data.getString(EXPAND_URL);

		MraidPlayer audioPlayer = getPlayer();
		audioPlayer.setPlayData(properties, url);
		audioPlayer.setLayoutParams(new ViewGroup.LayoutParams(1, 1));
		((ViewGroup) getParent()).addView(audioPlayer);

		audioPlayer.playAudio();

	}

	/**
	 * Play audio
	 * 
	 * @param url
	 *            - audio URL
	 * @param autoPlay
	 *            - should audio play immediately
	 * @param controls
	 *            - should native controls be visible
	 * @param loop
	 *            - should audio start over again after finishing
	 * @param position
	 *            - should audio be included with ad content
	 * @param startStyle
	 *            - normal/fullscreen; full screen if audio should play in full screen
	 * @param stopStyle
	 *            - normal/exit; exit if audio should exit after audio stops
	 */
	public void playAudio(String url, boolean autoPlay, boolean controls, boolean loop, boolean position,
			String startStyle, String stopStyle) {

		PlayerProperties properties = new PlayerProperties();

		properties.setProperties(false, autoPlay, controls, position, loop, startStyle, stopStyle);

		Bundle data = new Bundle();

		data.putString(ACTION_KEY, ACTION.PLAY_AUDIO.toString());
		data.putString(EXPAND_URL, url);
		data.putParcelable(PLAYER_PROPERTIES, properties);

		if (properties.isFullScreen()) {
			try {
				Intent intent = new Intent(getContext(), MraidActionHandler.class);
				intent.putExtras(data);
				getContext().startActivity(intent);
			}
			catch (ActivityNotFoundException e) {
				e.printStackTrace();
			}
		}
		else {
			Message msg = mHandler.obtainMessage(MESSAGE_PLAY_AUDIO);
			msg.setData(data);
			mHandler.sendMessage(msg);
		}
	}

	public void playVideoImpl(Bundle data) {

		PlayerProperties properties = (PlayerProperties) data.getParcelable(PLAYER_PROPERTIES);
		Dimensions d = (Dimensions) data.getParcelable(DIMENSIONS);
		String url = data.getString(EXPAND_URL);

		MraidPlayer videoPlayer = getPlayer();
		videoPlayer.setPlayData(properties, url);

		FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams((int) (d.width), (int) (d.height));
		fl.topMargin = (int) (d.x);
		fl.leftMargin = (int) (d.y);
		videoPlayer.setLayoutParams(fl);

		FrameLayout backGround = new FrameLayout(getContext());
		backGround.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				Log.i("MRAID", "background touch called");
				return true;
			}
		});
		backGround.setId(BACKGROUND_ID);
		backGround.setPadding((int) (d.x), (int) (d.y), 0, 0);

		FrameLayout contentView = (FrameLayout) getRootView().findViewById(R.id.content);
		contentView.addView(backGround, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,
				FrameLayout.LayoutParams.FILL_PARENT));

		backGround.addView(videoPlayer);
		setVisibility(View.INVISIBLE);

		videoPlayer.setListener(new MraidPlayerListener() {

			@Override
			public void onPrepared() {
			}

			@Override
			public void onError() {
				onComplete();
			}

			@Override
			public void onComplete() {
				FrameLayout background = (FrameLayout) getRootView().findViewById(BACKGROUND_ID);
				((ViewGroup) background.getParent()).removeView(background);
				setVisibility(View.VISIBLE);
			}
		});

		videoPlayer.playVideo();

	}

	/**
	 * Play video
	 * 
	 * @param url
	 *            - video URL
	 * @param audioMuted
	 *            - should audio be muted
	 * @param autoPlay
	 *            - should video play immediately
	 * @param controls
	 *            - should native player controls be visible
	 * @param loop
	 *            - should video start over again after finishing
	 * @param d
	 *            - inline area dimensions
	 * @param startStyle
	 *            - normal/fullscreen; full screen if video should play in full screen
	 * @param stopStyle
	 *            - normal/exit; exit if video should exit after video stops
	 */
	public void playVideo(String url, boolean audioMuted, boolean autoPlay, boolean controls, boolean loop,
			Dimensions d, String startStyle, String stopStyle) {

		Message msg = mHandler.obtainMessage(MESSAGE_PLAY_VIDEO);

		PlayerProperties properties = new PlayerProperties();

		properties.setProperties(audioMuted, autoPlay, controls, false, loop, startStyle, stopStyle);

		Bundle data = new Bundle();
		data.putString(EXPAND_URL, url);
		data.putString(ACTION_KEY, ACTION.PLAY_VIDEO.toString());

		data.putParcelable(PLAYER_PROPERTIES, properties);

		if (d != null)
			data.putParcelable(DIMENSIONS, d);

		if (properties.isFullScreen()) {
			try {
				Intent intent = new Intent(getContext(), MraidActionHandler.class);
				intent.putExtras(data);
				getContext().startActivity(intent);
			}
			catch (ActivityNotFoundException e) {
				e.printStackTrace();
			}
		}
		else if (d != null) {
			msg.setData(data);
			mHandler.sendMessage(msg);
		}
	}

	/**
	 * Revert to earlier ad state
	 */
	public void resetContents() {

		FrameLayout contentView = (FrameLayout) getRootView().findViewById(R.id.content);

		FrameLayout placeHolder = (FrameLayout) getRootView().findViewById(PLACEHOLDER_ID);
		FrameLayout background = (FrameLayout) getRootView().findViewById(BACKGROUND_ID);
		ViewGroup parent = (ViewGroup) placeHolder.getParent();
		background.removeView(this);
		contentView.removeView(background);
		resetLayout();
		parent.addView(this, mIndex);
		parent.removeView(placeHolder);
		parent.invalidate();
	}

	/**
	 * The Class NewLocationReciever.
	 */
	public static abstract class NewLocationReciever {

		/**
		 * On new location.
		 * 
		 * @param v
		 *            the v
		 */
		public abstract void OnNewLocation(ViewState v);
	}

	/**
	 * Reset layout.
	 */
	private void resetLayout() {
		ViewGroup.LayoutParams lp = getLayoutParams();
		if (bGotLayoutParams) {
			lp.height = mInitLayoutHeight;
			lp.width = mInitLayoutWidth;
		}
		setVisibility(VISIBLE);
		requestLayout();
	}

	/**
	 * Checks if is page finished.
	 * 
	 * @return true, if is page finished
	 */
	public boolean isPageFinished() {
		return bPageFinished;
	}

	// trap keyboard state and view height/width
	@Override
	public void onGlobalLayout() {
		boolean state = bKeyboardOut;
		if (!bKeyboardOut && mContentViewHeight >= 0 && getContentViewHeight() >= 0
				&& (mContentViewHeight != getContentViewHeight())) {

			state = true;
			String injection = "window.mraidview.fireChangeEvent({ keyboardState: true});";
			injectJavaScript(injection);

		}
		if (bKeyboardOut && mContentViewHeight >= 0 && getContentViewHeight() >= 0
				&& (mContentViewHeight == getContentViewHeight())) {

			state = false;
			String injection = "window.mraidview.fireChangeEvent({ keyboardState: false});";
			injectJavaScript(injection);
		}
		if (mContentViewHeight < 0) {
			mContentViewHeight = getContentViewHeight();
		}

		bKeyboardOut = state;
	}

	/**
	 * Gets the size.
	 * 
	 * @return the size
	 */
	public String getSize() {
		return "{ width: " + (int) (getWidth() / mDensity) + ", " + "height: " + (int) (getHeight() / mDensity) + "}";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.webkit.WebView#onAttachedToWindow()
	 * 
	 * Gather some initial information about the view.
	 */
	@Override
	protected void onAttachedToWindow() {
		if (!bGotLayoutParams) {
			ViewGroup.LayoutParams lp = getLayoutParams();
			mInitLayoutHeight = lp.height;
			mInitLayoutWidth = lp.width;
			bGotLayoutParams = true;
		}
		super.onAttachedToWindow();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.webkit.WebView#saveState(android.os.Bundle)
	 */
	@Override
	public WebBackForwardList saveState(Bundle outState) {
		outState.putString(AD_PATH, mLocalFilePath);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.webkit.WebView#restoreState(android.os.Bundle)
	 */
	@Override
	public WebBackForwardList restoreState(Bundle savedInstanceState) {

		mLocalFilePath = savedInstanceState.getString(AD_PATH);

		String url = "file://" + mLocalFilePath + java.io.File.separator + CURRENT_FILE;
		super.loadUrl(url);

		return null;
	}

	/**
	 * The Class ScrollEater.
	 */
	class ScrollEater extends SimpleOnGestureListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.view.GestureDetector.SimpleOnGestureListener#onScroll(android .view.MotionEvent,
		 * android.view.MotionEvent, float, float)
		 * 
		 * Gesture detector for eating scroll events
		 */
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			return true;
		}
	}

	public void raiseError(String strMsg, String action) {

		Message msg = mHandler.obtainMessage(MESSAGE_RAISE_ERROR);

		Bundle data = new Bundle();
		data.putString(ERROR_MESSAGE, strMsg);
		data.putString(ERROR_ACTION, action);
		msg.setData(data);
		mHandler.sendMessage(msg);
	}

	public float defaultWidth = 0, defaultHeight = 0;
	public void setAdsDefaultWidth(float defaultWidth){
		this.defaultWidth = defaultWidth;
	}
	public float getAdsDefaultWidth(){
		return this.defaultWidth;
	}
	public void setAdsDefaultHeight(float defaultHeight){
		this.defaultHeight = defaultHeight;
	}
	public float getAdsDefaultHeight(){
		return this.defaultHeight;
	}
	
	/**
	 * Checks if is expanded.
	 * 
	 * @return true, if is expanded
	 */
	public boolean isExpanded() {
		return mViewState == ViewState.EXPANDED;
	}

	protected void onDetachedFromWindow() {
		mUtilityController.stopAllListeners();
		super.onDetachedFromWindow();
	};

	MraidPlayer getPlayer() {

		if (player != null)
			player.releasePlayer();
		player = new MraidPlayer(getContext());
		return player;
	}
}
