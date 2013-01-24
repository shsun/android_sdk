package com.emediate.controller.util;

/**
 * 
 * Interface class to receive call backs from Player
 *
 */
public interface MraidPlayerListener {	
	
	/**
	 * On completion
	 */
	public void onComplete();
	
	/**
	 * On loading complete
	 */
	public void onPrepared();
	
	/**
	 * On Error
	 */
	public void onError();
}

