
package com.cameraimara;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {
	
	// result for picture
	private int TAKE_PICTURE;
	
	// Controls
	private Button takeBtn;
	private ImageView result;
	private Uri outputFileUri;
	
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
				// Create an output file.
				File file = new File(Environment.getExternalStorageDirectory(),	"test.jpg");
				outputFileUri = Uri.fromFile(file);
				
				// Generate the Intent.
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
				

				// Launch the camera application.
				startActivityForResult(intent, TAKE_PICTURE);
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		if (requestCode == TAKE_PICTURE) 
		{
			// Check if the result includes a thumbnail Bitmap
			if (data != null) 
			{
				if (data.hasExtra("data")) 
				{
					Bitmap thumbnail = data.getParcelableExtra("data");
					result.setImageBitmap(thumbnail);
				}
			} 
			else 
			{
				// If there is no thumbnail image data, the image
				// will have been stored in the target output URI.
				// Resize the full image to fit in out image view.
				int width = result.getWidth();
				int height = result.getHeight();
				BitmapFactory.Options factoryOptions = new BitmapFactory.Options();
				factoryOptions.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(outputFileUri.getPath(),factoryOptions);
				
				int imageWidth = factoryOptions.outWidth;
				int imageHeight = factoryOptions.outHeight;
				
				// Determine how much to scale down the image
				int scaleFactor = Math.min(imageWidth/width, imageHeight/height);
				
				// Decode the image file into a Bitmap sized to fill the View
				factoryOptions.inJustDecodeBounds = false;
				factoryOptions.inSampleSize = scaleFactor;
				factoryOptions.inPurgeable = true;
				Bitmap bitmap =	BitmapFactory.decodeFile(outputFileUri.getPath(), factoryOptions);
				result.setImageBitmap(bitmap);
			}
		}
	}

}
