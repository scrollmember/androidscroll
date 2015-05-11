package jp.ac.tokushima_u.is.ll.io;

import java.io.IOException;
import java.util.ArrayList;

import jp.ac.tokushima_u.is.ll.provider.LearningLogContract;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Itemtitles;
import jp.ac.tokushima_u.is.ll.util.AudioUtil;
import jp.ac.tokushima_u.is.ll.util.JsonItemUtil.ItemtitlesQuery;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
//test test test
public class AudioCacheHandler extends JsonHandler {
	public AudioCacheHandler(Context context) {
		super(LearningLogContract.CONTENT_AUTHORITY);
		this.context = context;
	}

	private Context context;

	@Override
	public ArrayList<ContentProviderOperation> parse(ContentResolver resolver)
			throws IOException {
		Cursor cursor = resolver.query(Itemtitles.CONTENT_URI, ItemtitlesQuery.PROJECTION, null, null, Itemtitles.DEFAULT_SORT);
		try{
			while(cursor.moveToNext()){
				String code = cursor.getString(ItemtitlesQuery.CODE);
				String content = cursor.getString(ItemtitlesQuery.CONTENT);
				AudioUtil.getPronounceAudio(context, content, code);
			}
		}finally{
			cursor.close();
		}
		return new ArrayList<ContentProviderOperation>();
	}

}
