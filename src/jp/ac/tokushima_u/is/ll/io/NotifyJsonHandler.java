package jp.ac.tokushima_u.is.ll.io;

import java.util.ArrayList;
import java.util.List;

import jp.ac.tokushima_u.is.ll.provider.LearningLogContract;
import jp.ac.tokushima_u.is.ll.util.JsonNotifyUtil;
import jp.ac.tokushima_u.is.ll.util.JsonQuizUtil;
import jp.ac.tokushima_u.is.ll.util.Lists;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

public class NotifyJsonHandler extends JsonHandler {
	private Context context;

	public NotifyJsonHandler(Context context) {
		super(LearningLogContract.CONTENT_AUTHORITY);
		this.context = context;
	}

	public ArrayList<ContentProviderOperation> parse(ContentResolver resolver) {
		final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
		
		List<ContentValues> cvs = JsonNotifyUtil.searchToUpdateNotifys(resolver);
		for(ContentValues cv:cvs){
			batch.addAll(JsonNotifyUtil.update(context, cv));
		}
		return batch;
	}

}
