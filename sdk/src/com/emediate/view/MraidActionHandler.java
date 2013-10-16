package com.emediate.view;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

import com.emediate.controller.MraidController.Dimensions;
import com.emediate.controller.MraidController.PlayerProperties;
import com.emediate.controller.util.MraidPlayer;
import com.emediate.controller.util.MraidPlayerListener;
import com.emediate.controller.util.MraidUtils;
import com.emediate.view.MraidView.ACTION;

/**
 * Activity class to handle full screen audio/video
 * @author Roshan
 *
 */
public class MraidActionHandler extends Activity {

	private HashMap<ACTION, Object> actionData = new HashMap<ACTION, Object>();
	private RelativeLayout layout;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle data = getIntent().getExtras();
		
		layout = new RelativeLayout(this);
		layout.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		setContentView(layout);
		
		doAction(data);
		
	}

	/**
	 * Perform action - Play audio/video
	 * @param data - Action data
	 */
	private void doAction(Bundle data) {

		String actionData = data.getString(MraidView.ACTION_KEY);
				
		if(actionData == null)
			return;
		
		MraidView.ACTION actionType = MraidView.ACTION.valueOf(actionData); 
		
		switch (actionType) {
		case PLAY_AUDIO: {
			MraidPlayer player = initPlayer(data,actionType);			
			player.playAudio();
		}
			break;
		case PLAY_VIDEO: {
			MraidPlayer player = initPlayer(data,actionType);
			player.playVideo();
		}
			break;
		default:
			break;
		}
	}
	
	/**
	 * Create and initialize player
	 * @param playData - Play data
	 * @param actionType - type of action
	 * @return
	 */
	MraidPlayer initPlayer(Bundle playData,ACTION actionType){				

		PlayerProperties properties = (PlayerProperties) playData.getParcelable(MraidView.PLAYER_PROPERTIES);

		Dimensions playDimensions = (Dimensions)playData.getParcelable(MraidView.DIMENSIONS);		
				
		MraidPlayer player = new MraidPlayer(this);
		player.setPlayData(properties,MraidUtils.getData(MraidView.EXPAND_URL, playData));
		
		RelativeLayout.LayoutParams lp;
		if(playDimensions == null) {
			lp = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
			lp.addRule(RelativeLayout.CENTER_IN_PARENT);				
		}
		else {
			// Play video in dimensions given
			lp = new RelativeLayout.LayoutParams(playDimensions.width, playDimensions.height);
			lp.topMargin = playDimensions.y;
			lp.leftMargin = playDimensions.x;		

		}
		player.setLayoutParams(lp);
		layout.addView(player);
		
		this.actionData.put(actionType, player);
		setPlayerListener(player);
		
		return player;
	}
	
	/**
	 * Set listener
	 * @param player - player instance
	 */
	private void setPlayerListener(MraidPlayer player){
		player.setListener(new MraidPlayerListener() {
			
			@Override
			public void onPrepared() {
				
				
			}
			
			@Override
			public void onError() {				
				finish();
			}
			
			@Override
			public void onComplete() {
				finish();
			}
		});
	}

	@Override
	protected void onStop() {
		
		for(Map.Entry<ACTION, Object> entry: actionData.entrySet()){
			switch(entry.getKey()){
			case PLAY_AUDIO : 
			case PLAY_VIDEO : {
				MraidPlayer player = (MraidPlayer)entry.getValue();
				player.releasePlayer();
			}			
			break;
			default : break;
		}	
	}
		super.onStop();
	}	
	
}
