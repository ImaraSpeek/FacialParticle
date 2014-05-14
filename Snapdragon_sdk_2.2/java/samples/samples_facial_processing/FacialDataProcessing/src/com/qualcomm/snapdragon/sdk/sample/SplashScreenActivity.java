/* ======================================================================
 *  Copyright © 2014 Qualcomm Technologies, Inc. All Rights Reserved.
 *  QTI Proprietary and Confidential.
 *  =====================================================================
 *  
 * @file:   SplashScreenActivity.java
 *
 */


package com.qualcomm.snapdragon.sdk.sample;

import com.example.facialprocessing.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreenActivity extends Activity {

	  @Override
	     public void onCreate(Bundle savedInstanceState) {         

	        super.onCreate(savedInstanceState);    
	        setContentView(R.layout.splash);
	        //rest of the code

			
	    	new Handler().postDelayed(new Runnable(){
	            @Override
	            public void run() {
	                /* Create an Intent that will start the Menu-Activity. */
	                Intent mainIntent = new Intent(SplashScreenActivity.this,CameraPreviewActivity.class);
	                SplashScreenActivity.this.startActivity(mainIntent);
	                SplashScreenActivity.this.finish();
	            }
	        }, 1500);
	  }
	    	
}
