package com.emediate.controller.util;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.emediate.controller.MraidController.PlayerProperties;

/**
 * 
 * Player class to play audio and video
 *
 */

public class MraidPlayer extends VideoView implements OnCompletionListener, OnErrorListener, OnPreparedListener {

	private PlayerProperties playProperties;
	private AudioManager aManager;
	private MraidPlayerListener listener;
	private int mutedVolume;
	private String contentURL;
	private RelativeLayout transientLayout;
	
	private static String transientText = "Loading. Please Wait..";
	private static String LOG_TAG = "Mraid Player";
	private boolean isReleased;
			
	/**
	 * Constructor
	 * @param context - Current context	 * 
	 */
	public MraidPlayer(Context context){
		super(context);
		aManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);		
	}
	
	/**
	 * 
	 * @param properties - player properties
	 * @param url - url to play
	 */
	public void setPlayData(PlayerProperties properties,String url){
		isReleased = false;
		playProperties = properties;
		contentURL = url;
	}

	/**
	 * Play audio
	 * @param url - audio url
	 */
	public void playAudio() {
		loadContent();
	}

	/**
	 * Show player control
	 */
	void displayControl() {
		
		if (playProperties.showControl()) {
			MediaController ctrl = new MediaController(getContext());
			setMediaController(ctrl);
			ctrl.setAnchorView(this);
		}
			
	}
	
	/**
	 * Load audio/video content
	 * @param url - audio/video url
	 */
	void loadContent(){
		
		
		contentURL = contentURL.trim();
		
		contentURL = MraidUtils.convert(contentURL);
		if(contentURL == null && listener != null){
			removeView();
			listener.onError();
			return;
		}
		
		setVideoURI(Uri.parse(contentURL));
		displayControl();
		startContent();
	}

	/**
	 * Play start
	 */
	void startContent() {
		
		setOnCompletionListener(this);
		setOnErrorListener(this);
		setOnPreparedListener(this);		
		
		if(!playProperties.inline)
			addTransientMessage();
		
		if (playProperties.isAutoPlay()) {
			start();
		}		
	}

	/**
	 * Play video
	 * @param url - video url
	 */
	public void playVideo() {	
		
		if (playProperties.doMute()) {
			mutedVolume = aManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			aManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_PLAY_SOUND);
			
		}
		loadContent();
	}

	/**
	 * Unmute audio
	 */
	void unMute() {
		aManager.setStreamVolume(AudioManager.STREAM_MUSIC, mutedVolume, AudioManager.FLAG_PLAY_SOUND);
	}

	/**
	 * Set callback listener
	 * @param listener - callback listener
	 */
	public void setListener(MraidPlayerListener listener) {
		this.listener = listener;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (playProperties.doLoop())
			start();
		else if (playProperties.exitOnComplete() || playProperties.inline) {	
			// for inline audio on completion, release player
			releasePlayer();
		}
	}
	
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.i(LOG_TAG, "Player error : " + what);
		clearTransientMessage();
		removeView();
		if(listener != null)
			listener.onError();
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		clearTransientMessage();
		if(listener != null)
			listener.onPrepared();
		}		

	/**
	 * Remove player from parent
	 */
	void removeView(){
		ViewGroup parent = (ViewGroup) getParent();
		if(parent != null)
			parent.removeView(this);
	}	
	
	/**
	 * Release player session, notify the listener
	 */
	public void releasePlayer(){
		
		if(isReleased)
			return;
		
		isReleased = true;
		
		stopPlayback();
		removeView();
		if (playProperties != null && playProperties.doMute())
			unMute();
		if (listener != null)
			listener.onComplete();
	}
	
	/**
	 * Add transient message
	 */
	void addTransientMessage(){
		
		if(playProperties.inline)
			return;
		
		transientLayout = new RelativeLayout(getContext());
		transientLayout.setLayoutParams(getLayoutParams());
		
		//create a transient text view
		TextView transientView = new TextView(getContext());
		transientView.setText(transientText);
		transientView.setTextColor(Color.WHITE);
		
		RelativeLayout.LayoutParams msgparams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		msgparams.addRule(RelativeLayout.CENTER_IN_PARENT);
		
		transientLayout.addView(transientView,msgparams);	
		ViewGroup parent = (ViewGroup) getParent();
		parent.addView(transientLayout);
	}
	
	/**
	 * Clear transient message
	 */
	void clearTransientMessage(){
		if(transientLayout != null) {
			ViewGroup parent = (ViewGroup) getParent();
			parent.removeView(transientLayout);
		}		
	}	
	
	
	
}
