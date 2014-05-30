
package com.cameraimara;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {
	
	// result for picture
	//private int TAKE_PICTURE;
	
	// Controls
	private Button takeBtn;
	@SuppressWarnings("unused")
	private ImageView result;
	//private Uri outputFileUri;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		result = (ImageView) findViewById(R.id.result);
		
		takeBtn = (Button) findViewById(R.id.takeBtn);
		takeBtn.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) {
				//Camera camera = Camera.open();
				//Camera.Parameters parameters = camera.getParameters();

				//camera.setParameters(parameters);
				
				Intent camerastart = new Intent(MainActivity.this, CameraActivity.class);
				startActivity(camerastart);

			}
		});
	}

}
