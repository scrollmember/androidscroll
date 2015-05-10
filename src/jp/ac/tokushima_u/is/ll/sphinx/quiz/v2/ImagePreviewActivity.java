
package jp.ac.tokushima_u.is.ll.sphinx.quiz.v2;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.util.ApiConstants;
import jp.ac.tokushima_u.is.ll.util.BitmapUtil;
import uk.co.senab.photoview.PhotoViewAttacher;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageView;

/**
 * 画像プレビュー用Activity
 * ImageViewひとつだけなのでxml作ってないです
 * 
 * 使い方：
 * Intentの付加情報(Extras)に"IMG_ID"という
 * キーをつけたStringのURLを指定する
 * (IMG_IDにはSCROLLのImageIDを指定する)
 * 
 * 例：
 * <>呼び出し元：
 * intent.putExtra("IMG_ID", imageURL);
 * startActivity(intent);
 */
public class ImagePreviewActivity extends Activity {
    
    private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
    private final int FP = ViewGroup.LayoutParams.MATCH_PARENT;
    
    private ImageView mImageView;
    private Bitmap mBitmap;
    private PhotoViewAttacher mAttacher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        mImageView = new ImageView(this);
        
        Intent intent = getIntent();
        if(intent != null) {
            String  imageurl = intent.getStringExtra("IMG_ID");
            mBitmap = BitmapUtil.getBitmap(this, imageurl, ApiConstants.LargeSizePostfix);
        }

        if(mBitmap == null){
            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.noimage);
        }
        
        mImageView.setImageBitmap(mBitmap);
        
        mAttacher = new PhotoViewAttacher(mImageView);
        
        setContentView(mImageView, new LayoutParams(FP,FP));
    }
}
