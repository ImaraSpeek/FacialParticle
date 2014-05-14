
package com.cameraimara;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	
	// result for picture
	private int TAKE_PICTURE;
	
	// Controls
	private Button takeBtn;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		takeBtn = (Button) findViewById(R.id.takeBtn);
		takeBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Create an output file.
				File file = new File(Environment.getExternalStorageDirectory(),	"test.jpg");
				Uri outputFileUri = Uri.fromFile(file);
				
				// Generate the Intent.
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
				

				// Launch the camera application.
				startActivityForResult(intent, TAKE_PICTURE);

			}
		});
	}

}
