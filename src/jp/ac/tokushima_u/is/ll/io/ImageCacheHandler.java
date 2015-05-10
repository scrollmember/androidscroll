package jp.ac.tokushima_u.is.ll.io;

import java.io.IOException;
import java.util.ArrayList;

import jp.ac.tokushima_u.is.ll.provider.LearningLogContract;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Choices;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Items;
import jp.ac.tokushima_u.is.ll.util.ApiConstants;
import jp.ac.tokushima_u.is.ll.util.BitmapUtil;
import jp.ac.tokushima_u.is.ll.util.Constants;
import jp.ac.tokushima_u.is.ll.util.JsonItemUtil.ItemsQuery;
import jp.ac.tokushima_u.is.ll.util.JsonQuizUtil.ChoicesQuery;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;

public class ImageCacheHandler extends JsonHandler {
	public ImageCacheHandler(Context context) {
		super(LearningLogContract.CONTENT_AUTHORITY);
		this.context = context;
	}

	private Context context;

	@Override
	public ArrayList<ContentProviderOperation> parse(ContentResolver resolver)
			throws IOException {
		Cursor cursor = resolver.query(Choices.CONTENT_URI, ChoicesQuery.PROJECTION, Choices.File_TYPE+"=?", new String[]{Constants.FileTypeImage}, Choices.DEFAULT_SORT);
		try{
			while(cursor.moveToNext()){
				String photoUrl = cursor.getString(ChoicesQuery.CHOICE_CONTENT);
				Bitmap bp = BitmapUtil.getBitmap(context, photoUrl, ApiConstants.MiddleSizePostfix);
				if(bp!=null)
					BitmapUtil.getBitmap(context, photoUrl, ApiConstants.SmallSizePostfix);
			}
		}finally{
			cursor.close();
		}
		
		Cursor item_cursor = resolver.query(Items.CONTENT_URI, ItemsQuery.PROJECTION, Items.FILE_TYPE+"=?", new String[]{Constants.FileTypeImage}, Items.DEFAULT_SORT);
		try{
			while(item_cursor.moveToNext()){
				String photoUrl = item_cursor.getString(ItemsQuery.PHOTO_URL);
				Bitmap bp = BitmapUtil.getBitmap(context, photoUrl, ApiConstants.SmallestSizePostfix);
				if(bp!=null)
					BitmapUtil.getBitmap(context, photoUrl, ApiConstants.MiddleSizePostfix);
			}
		}finally{
			item_cursor.close();
		}
		
		return null;
	}

}
