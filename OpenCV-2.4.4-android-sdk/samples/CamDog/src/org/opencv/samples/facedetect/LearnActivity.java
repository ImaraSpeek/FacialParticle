package org.opencv.samples.facedetect;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class LearnActivity extends Activity {

    private static final String    TAG                 = "OCVSample::Activity";
   
    private MenuItem               mItemFace50;
    private MenuItem               mItemFace40;
    private MenuItem               mItemFace30;
    private MenuItem               mItemFace20;

    
    public static int		 method				= 1;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.select_view);
        
        // Define all the buttons
        Button Detect = (Button)findViewById(R.id.Detect);
        Detect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent DetectIntent = new Intent(getApplicationContext(), FdActivity.class);
                startActivity(DetectIntent);
            }
        });

    }
      
    public LearnActivity() {
       Log.i(TAG, "Instantiated new " + this.getClass());
    }


    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemFace50 = menu.add("Face size 50%");
        mItemFace40 = menu.add("Face size 40%");
        mItemFace30 = menu.add("Face size 30%");
        mItemFace20 = menu.add("Face size 20%");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemFace50)
            ;
        else if (item == mItemFace40)
            ;
        else if (item == mItemFace30)
            ;
        else if (item == mItemFace20)
            ;
        return true;
    }

}
