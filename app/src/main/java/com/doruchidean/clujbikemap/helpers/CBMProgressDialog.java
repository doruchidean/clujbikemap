package com.doruchidean.clujbikemap.helpers;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;

import com.doruchidean.clujbikemap.R;

/**
 * Created by Doru on 20/03/16.
 *
 */
public class CBMProgressDialog extends ProgressDialog {

	public CBMProgressDialog(Context context) {
		super(context, R.style.AppTheme_ProgressDialog);
	}

	public void setMaxTimeLimitSeconds(int seconds){
		Handler h = new Handler();
		Runnable r = new Runnable() {
			@Override
			public void run() {
				CBMProgressDialog.this.dismiss();
			}
		};

		h.postDelayed(r, seconds*1000);
	}
}
