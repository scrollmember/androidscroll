
package jp.ac.tokushima_u.is.ll.ui.quiz;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.util.ApiConstants;
import jp.ac.tokushima_u.is.ll.util.BitmapUtil;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

public class ImagePreviewActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imagepreview);
        
        Intent intent = getIntent();
        if(intent != null) {
            String  imageurl = intent.getStringExtra("IMG_ID");
            ImageView imageView = (ImageView) findViewById(R.id.img_view);
            Bitmap bitmap = BitmapUtil
					.getBitmap(this, imageurl, ApiConstants.MiddleSizePostfix);
            imageView.setImageBitmap(bitmap);
            
        }
        
    }
}
