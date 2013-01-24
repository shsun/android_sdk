package com.emediate.android.util;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;

public class DialogCreator {
	
	/**
	 * Create not network warning dialog
	 */
	public void popupMsgDialog(Context mContext, String mTitle, String mMsg, String mBtn){
		Builder offlineDialog = new AlertDialog.Builder(mContext)
        .setTitle(mTitle)
        .setMessage(mMsg)
        .setPositiveButton(mBtn, new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int whichButton) {
        		
            }
        });
		offlineDialog.show();
	}
	
	
}
