package jp.ac.tokushima_u.is.ll.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Items;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Itemtags;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Itemtitles;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Languages;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Settings;
import jp.ac.tokushima_u.is.ll.provider.LearningLogContract.Users;
import jp.ac.tokushima_u.is.ll.ui.media.AudioPlayer;
import jp.ac.tokushima_u.is.ll.ui.media.AudioRecorder;
import jp.ac.tokushima_u.is.ll.ui.media.ShowPhoto;
import jp.ac.tokushima_u.is.ll.ui.media.VideoPlayer;
import jp.ac.tokushima_u.is.ll.util.ApiConstants;
import jp.ac.tokushima_u.is.ll.util.Constants;
import jp.ac.tokushima_u.is.ll.util.ContextUtil;
import jp.ac.tokushima_u.is.ll.util.HttpClientFactory;
import jp.ac.tokushima_u.is.ll.util.Lists;
import jp.ac.tokushima_u.is.ll.util.NotifyingAsyncQueryHandler;
import jp.ac.tokushima_u.is.ll.util.NotifyingAsyncQueryHandler.AsyncQueryListener;
import jp.ac.tokushima_u.is.ll.util.StringUtils;
import jp.ac.tokushima_u.is.ll.util.UIUtils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.text.style.AbsoluteSizeSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * @author:dameng <a> This class is used to add/edit a new learning log object.
 *                </a>
 */
public class QestionActivity extends Activity implements AsyncQueryListener,Runnable {
	private static final String TAG = "NewLogActivity";

	private NotifyingAsyncQueryHandler mHandler;
	
	private static final int TRANSLATE_MESSAGE_SUCCESS = 0;
	private Handler translateHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case TRANSLATE_MESSAGE_SUCCESS:
				Bundle bundle = msg.getData();
				String code = bundle.getString("code");
				String result = bundle.getString("result");
				if(StringUtils.isBlank(code) || StringUtils.isBlank(result)){
					return;
				}
				EditText text = getTitleEditText(code);
				text.setText(result);
				break;
			default:
				super.handleMessage(msg);	
			}
		}
	};

	// category values & tag values
	private String[] cate_names = new String[] {};
	private String[] cate_values = new String[] {};
	private List<String> tags = new ArrayList<String>();
	private String selectedCat;
	private String itemId = null;
	private ImageView logAttach;
	private boolean isPublic = true;
	private boolean isLocationBased = true;
	private boolean isTextChoice = true;
	private boolean isFileChoice = true;
	private boolean isYesNoQuiz = true;

	private LocationManager locationmanager;
	
	// Dialog IDs
	private static final int DialogCategoryList = 1;
	private static final int DialogSettingList = 2;
	private static final int DialogTagList = 3;
	private static final int DialogAttachList = 4;
	private static final int DialogAttachAction = 5;
	
	// Activity return values
	private static final int VOICE_RECOGNITION_REQUEST_CODE = R.string.dialog_attach_option_btn_back;
	private static final int Gallery_REQUEST_CODE = R.string.dialog_attach_option_btn_delete;
	private static final int Camera_REQUEST_CODE = R.string.dialog_attach_option_btn_view;
	private static final int VIDEO_REQUEST_CODE = R.string.dialog_attach_option_title;
	private static final int AUDIO_REQUEST_CODE = R.string.description_home;

	private ProgressDialog pd;
	private ProgressDialog pd_upload;

	private boolean mItemCursor = false;
	private boolean mItemtitleCursor = false;
	private boolean mItemtagCursor = false;
	private boolean mSettingCursor = false;
	private boolean mSettingLanguageCursor = false;

	private EditText logNote;
	private LinearLayout titleLayout;

	private int screenWidth;
	private int screenHeight;
	// Image shrink rate
	private static final double attachImageWidthScale = 0.3;
	private static final double attachImageHeightScale = 0.4;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log_edit);
		logNote = (EditText) this.findViewById(R.id.log_title_note);
		titleLayout = (LinearLayout) this.findViewById(R.id.log_title_block);
		logAttach = (ImageView) this.findViewById(R.id.log_attach);

		DisplayMetrics dm = new DisplayMetrics();
		dm = getApplicationContext().getResources().getDisplayMetrics();
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;

		mHandler = new NotifyingAsyncQueryHandler(this.getContentResolver(),
				this);
		String userId = ContextUtil.getUserId(this);
		selectedCat = ContextUtil.getDefaultCat(this);

		pd = ProgressDialog.show(this, "Wait..", "Loading...", true, false);

		Uri uri = Users.buildUsersSettingUri(userId);
		mHandler.startQuery(SettingQuery._TOKEN, null, uri,
				SettingQuery.PROJECTION, Settings.FIELD + "=?",
				new String[] { Settings.SETTING_CATEGORY_FIELD_ID.toString() },
				Settings.DEFAULT_SORT);

		Uri languageuri = Users.buildUsersSettingLanguageUri(userId);

		mHandler.startQuery(SettingLanguageQuery._TOKEN, languageuri,
				SettingLanguageQuery.PROJECTION, Settings.DEFAULT_SORT);

		Intent intent = this.getIntent();
		if (intent != null && Intent.ACTION_EDIT.equals(intent.getAction())) {
			Uri itemuri = intent.getData();
			this.itemId = Items.getItemId(itemuri);
			Uri taguri = Items.buildItemtagsUri(itemId);
			Uri titleuri = Items.buildItemtitlesUri(itemId);
			mHandler.startQuery(ItemsQuery._TOKEN, itemuri,
					ItemsQuery.PROJECTION);
			mHandler.startQuery(ItemtitlesQuery._TOKEN, titleuri,
					ItemtitlesQuery.PROJECTION);
			mHandler.startQuery(ItemtagsQuery._TOKEN, taguri,
					ItemtagsQuery.PROJECTION);
		}else if(intent!=null&&intent.getType()!=null&&(intent.getType().contains("image/")||intent.getType().equals("video/"))){
			this.setAttachFile(intent);
		}
		
		locationmanager = (LocationManager) getSystemService(LOCATION_SERVICE);

		// mHandler.startQuery(SettingLanguageQuery._TOKEN, null, languageuri,
		// SettingLanguageQuery.PROJECTION, Settings.FIELD + "=? or "
		// + Settings.FIELD + "=?", new String[] {
		// Settings.SETTING_MYLAN_FIELD_ID.toString(),
		// Settings.SETTING_STUDYLAN_FIELD_ID.toString() },
		// Settings.DEFAULT_SORT);
	}

	public void onHomeClick(View view) {
		UIUtils.goHome(this);
	}

	public void onSaveClick(View view) {
		Map<String, String> titles = this.getTitleValues();
		if(titles==null||titles.size()<=0){
			Toast.makeText(QestionActivity.this,
					R.string.info_titles_empty, Toast.LENGTH_LONG).show();
			return;
		}
		this.pd_upload = ProgressDialog.show(this, "Wait..", "Uploading...", true, false);
		new Thread(this).start();
	}

	public void onDiscardClick(View view) {
		UIUtils.goHome(this);
	}

	public void onCategoryClick(View view) {
		this.showDialog(DialogCategoryList);
	}

	public void onCatTagClick(View view) {
		this.showDialog(DialogTagList);
	}

	public void onSettingClick(View view) {
		this.showDialog(DialogSettingList);
	}

	public void onAttachClick(View view) {
		this.showDialog(DialogAttachList);
	}
	

	@Override
	public void run() {
		try {
			DefaultHttpClient client = HttpClientFactory.getInstance(this);
			MultipartEntity entity = new MultipartEntity();

			Map<String, String> titles = this.getTitleValues();
			for(String key:titles.keySet()){
				String value = titles.get(key);
				entity.addPart("titleMap['"+key+"']", new StringBody(value));
			}
			if(this.isPublic)
				entity.addPart("shareLevel", new StringBody(this.getResources().getString(R.string.item_share_public)));
			else
				entity.addPart("shareLevel", new StringBody(this.getResources().getString(R.string.item_share_private)));
			entity.addPart("categoryId", new StringBody(this.selectedCat));
			entity.addPart("locationBased", new StringBody(String.valueOf(this.isLocationBased)));
			
			if(this.isFileChoice)
				entity.addPart("questionTypeIds", new StringBody(QuizActivity.QuizTypeImageMutiChoice.toString()));
			if(this.isTextChoice)
				entity.addPart("questionTypeIds", new StringBody(QuizActivity.QuizTypeTextMutiChoice.toString()));
			if(this.isYesNoQuiz)
				entity.addPart("questionTypeIds", new StringBody(QuizActivity.QuizTypeTextMutiChoice.toString()));
			
			if (this.logNote.getText() != null && this.logNote.getText().length() > 0)
				entity.addPart("note", new StringBody(this.logNote.getText().toString()));
			
			try{
				if(this.locationmanager==null)
					this.locationmanager = (LocationManager) getSystemService(LOCATION_SERVICE);
				Location loc = this.locationmanager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if (loc != null) {
					entity.addPart("itemLat", new StringBody(String.valueOf(loc.getLatitude())));
					entity.addPart("itemLng", new StringBody(String.valueOf(loc.getLongitude())));
					entity.addPart("speed", new StringBody(String.valueOf(loc.getSpeed())));
				}
			}catch(Exception e){
				
			}
			
			if(this.tags!=null){
				String t = "";
				for(int i=0;i<tags.size();i++){
					t = t+tags.get(i);
					if(i!=(tags.size()-1))
						t = t+",";
				}
				if(t!="")
					entity.addPart("tag", new StringBody(t));
			}
			
			try{
				if (this.logAttach.getVisibility() == View.VISIBLE) {
					Bundle args = (Bundle)this.logAttach.getTag();
					String path = args.getString(MultiFileActionListener.FilePathKey);
					File file = new File(path);
					entity.addPart("image", new FileBody(file));
				}
			}catch(Exception e){
				
			}

			HttpResponse response = null;
			if(this.itemId==null){
				HttpPost httpPost = new HttpPost(ApiConstants.ITEM_Add_URI);
				httpPost.setEntity(entity);
				response = client.execute(httpPost);
			}else{
//				HttpPut httpPut = new HttpPut(ApiConstants.ITEM_Add_URI+"/"+this.itemId);
//				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
//				for(String key:titles.keySet()){
//					String value = titles.get(key);
////					entity.addPart("titleMap['"+key+"']", new StringBody(value));
//					pairs.add(new BasicNameValuePair("titleMap['"+key+"']", value));
//				}
//				
//				httpPut.setEntity(new UrlEncodedFormEntity(pairs));
////				httpPut.setEntity(entity);
//				response = client.execute(httpPut);
				
				HttpPost httpPost = new HttpPost(ApiConstants.ITEM_Add_URI+"/"+this.itemId);
				entity.addPart("_method", new StringBody("put"));
				httpPost.setEntity(entity);
				response = client.execute(httpPost);
			}
			Log.d(Constants.LOG_TAG, ""
					+ response.getStatusLine().getStatusCode());
			Integer result = response.getStatusLine().getStatusCode() == 200 ? 1
					: 2;
			if (Constants.Item_SUCCESS.equals(result)) {
				resultHandler.sendEmptyMessage(1);
//				UIUtils.goSync(this);
			} else {
				resultHandler.sendEmptyMessage(2);
			}
			Log.d("LEARNINGLOG", String.valueOf(result));
		} catch (Exception e) {
			resultHandler.sendEmptyMessage(2);
			Log.e("LEARNINGLOG", "Error", e);
		}
	}
	
	private Handler resultHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if(pd_upload!=null&&pd_upload.isShowing())
				pd_upload.dismiss();
			switch(msg.what){
			case 1:
				Toast.makeText(QestionActivity.this, R.string.info_upload_success, Toast.LENGTH_SHORT).show();
				UIUtils.goHome(QestionActivity.this);break;
			case 2:
				Toast.makeText(QestionActivity.this, R.string.info_upload_failure, Toast.LENGTH_SHORT).show();break;
			};
		}
	};	


	// public void onCameraClick(View view) {
	// if (!Environment.getExternalStorageState().equals(
	// Environment.MEDIA_MOUNTED)) {
	// Toast.makeText(NewLogActivity.this, R.string.info_txt_no_sd_card,
	// Toast.LENGTH_LONG).show();
	// return;
	// }
	//
	// try {
	// Intent intent = new Intent();
	// intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
	// File tempfile = File.createTempFile("learninglog", ".jpg",
	// new File("/sdcard/DCIM/Camera/"));
	// intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempfile));
	// startActivityForResult(intent, Camera_REQUEST_CODE);
	// } catch (IOException e) {
	// Log.e(TAG, "can not open camera", e);
	// }
	// }
	//
	// public void onMicClick(View view){
	// if (!Environment.getExternalStorageState().equals(
	// Environment.MEDIA_MOUNTED)) {
	// Toast.makeText(NewLogActivity.this,
	// R.string.info_txt_no_sd_card, Toast.LENGTH_LONG).show();
	// return;
	// }
	// Intent intent = new Intent();
	// intent.setClass(NewLogActivity.this, AudioRecorder.class);
	// startActivityForResult(intent, AUDIO_REQUEST_CODE);
	// }


	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
		switch (id) {
		case DialogAttachAction: {
			ImageView iv = (ImageView) dialog
					.findViewById(R.id.attach_option_image);
			iv.setTag(args);
			Map<String, Object> map = this.getMultiFileParams(args);
			Bitmap bp = (Bitmap) map.get("bitmap");
			String filename = (String) map.get("filename");
			Long filesize = (Long) map.get("filesize");
			if (bp != null && filename != null && filesize != null) {
				iv.setImageBitmap(bp);
			}
		}
			break;
		default:
			super.onPrepareDialog(id, dialog, args);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		switch (id) {
		case DialogAttachAction: {
			LayoutInflater layoutInflater = LayoutInflater.from(this);
			View v = layoutInflater.inflate(
					R.layout.alert_dialog_attach_option, null);
			ImageView iv = (ImageView) v.findViewById(R.id.attach_option_image);
			iv.setTag(args);
			// ImageView iv = (ImageView)v.findViewById(R.id.attach_item_img);
			Map<String, Object> map = this.getMultiFileParams(args);
			Bitmap bp = (Bitmap) map.get("bitmap");
			String filename = (String) map.get("filename");
			Long filesize = (Long) map.get("filesize");
			if (bp != null && filename != null && filesize != null) {
				iv.setImageBitmap(bp);
				return new AlertDialog.Builder(this)
						.setTitle(R.string.dialog_attach_option_title)
						.setView(v)
						.setNeutralButton(
								R.string.dialog_attach_option_btn_view,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Dialog d = (Dialog) dialog;
										ImageView iv = (ImageView) d
												.findViewById(R.id.attach_option_image);
										Bundle args = (Bundle) iv.getTag();
										if (args == null)
											return;
										String path = args
												.getString(MultiFileActionListener.FilePathKey);
										String type = args
												.getString(MultiFileActionListener.FileTypeKey);
										if (Constants.FileTypeAudio
												.equals(type)) {
											Intent intent = new Intent(
													QestionActivity.this,
													AudioPlayer.class);
											intent.setData(Uri
													.fromFile(new File(path)));
											startActivity(intent);
										} else if (Constants.FileTypeImage
												.equals(type)) {
											Intent intent = new Intent(
													QestionActivity.this,
													ShowPhoto.class);
											intent.setData(Uri
													.fromFile(new File(path)));
											startActivity(intent);
										} else if (Constants.FileTypeVideo
												.equals(type)) {
											Intent intent = new Intent(
													QestionActivity.this,
													VideoPlayer.class);
											intent.setData(Uri
													.fromFile(new File(path)));
											startActivity(intent);
										}
									}
								})
						.setPositiveButton(
								R.string.dialog_attach_option_btn_delete,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										logAttach.setVisibility(View.GONE);
									}
								})
						.setNegativeButton(
								R.string.dialog_attach_option_btn_back,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {

									}
								}).create();
			}
		}
		default:
			return super.onCreateDialog(id, args);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DialogCategoryList: {
			int sel = 0;
			for (int j = 0; j < this.cate_values.length; j++) {
				if (this.selectedCat.equals(this.cate_values[j])) {
					sel = j;
					break;
				}
			}
			return new AlertDialog.Builder(this)
					.setTitle(R.string.item_category_label)
					.setSingleChoiceItems(this.cate_names, sel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									selectedCat = cate_values[whichButton];
								}
							})
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).create();
		}
		case DialogSettingList: {
			return new AlertDialog.Builder(QestionActivity.this)
					.setIcon(R.drawable.ic_title_setting)
					.setTitle(R.string.description_setting)
					.setMultiChoiceItems(
							R.array.item_options,
							new boolean[] { this.isPublic,
									this.isLocationBased, this.isTextChoice,
									this.isFileChoice, this.isYesNoQuiz },
							new DialogInterface.OnMultiChoiceClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton, boolean isChecked) {
									switch (whichButton) {
									case 0:
										isPublic = isChecked;
										break;
									case 1:
										isLocationBased = isChecked;
										break;
									case 2:
										isTextChoice = isChecked;
										break;
									case 3:
										isFileChoice = isChecked;
										break;
									case 4:
										isYesNoQuiz = isChecked;
										break;
									}
								}
							})
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).create();
		}
		case DialogTagList: {
			LayoutInflater factory = LayoutInflater.from(this);
			final View settingView = factory.inflate(
					R.layout.alert_dialog_log_setting, null);
			Spinner spanCategory = (Spinner) settingView
					.findViewById(R.id.spn_category);
			ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(
					this, android.R.layout.simple_spinner_item, this.cate_names);
			categoryAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spanCategory.setAdapter(categoryAdapter);
			EditText tagEdit = (EditText) settingView
					.findViewById(R.id.log_tag_edit);
			tagEdit.setText(Lists.arrayToString(this.tags));

			return new AlertDialog.Builder(QestionActivity.this)
					.setIcon(R.drawable.ic_title_category_default)
					.setTitle(R.string.description_category_tag)
					.setView(settingView)
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									Spinner spanCategory = (Spinner) settingView
											.findViewById(R.id.spn_category);
									EditText tagEdit = (EditText) settingView
											.findViewById(R.id.log_tag_edit);
									tags = Lists.stringToArray(tagEdit
											.getText().toString());
									selectedCat = cate_values[spanCategory
											.getSelectedItemPosition()];
								}
							}).create();
		}
		case DialogAttachList: {
			ListAdapter adapter = new SimpleAdapter(this, this.getAttachData(),
					R.layout.alert_dialog_log_attach_item, new String[] {
							"img", "txt" }, new int[] { R.id.attach_item_img,
							R.id.attach_item_text });
			DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					switch (which) {
					case 0: {
						try {
							Intent intent = new Intent();
							intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
							File tempfile = File.createTempFile("learninglog",
									".jpg", new File("/sdcard/DCIM/Camera/"));
							intent.putExtra(MediaStore.EXTRA_OUTPUT,
									Uri.fromFile(tempfile));
							startActivityForResult(intent, Camera_REQUEST_CODE);
						} catch (IOException e) {
							Log.e(TAG, "can not open camera", e);
						}
					}
						break;
					case 1: {
						if (!Environment.getExternalStorageState().equals(
								Environment.MEDIA_MOUNTED)) {
							Toast.makeText(QestionActivity.this,
									R.string.info_txt_no_sd_card,
									Toast.LENGTH_LONG).show();
							return;
						}
						Intent intent = new Intent();
						intent.setClass(QestionActivity.this,
								AudioRecorder.class);
						startActivityForResult(intent, AUDIO_REQUEST_CODE);
					}
						break;
					case 2: {
						Intent intent = new Intent();
						intent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
						intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
						startActivityForResult(intent, VIDEO_REQUEST_CODE);
					}
						break;
					case 3: {
						Intent intent = new Intent();
						intent.setType("*/*");
						intent.setAction(Intent.ACTION_GET_CONTENT);
						startActivityForResult(
								Intent.createChooser(intent, "Select Picture"),
								Gallery_REQUEST_CODE);
					}
						break;
					}
				};
			};
			return new AlertDialog.Builder(QestionActivity.this).setAdapter(
					adapter, clickListener).create();
		}
		default:
			return null;
		}
	}

	private List<Map<String, Object>> getAttachData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("img", R.drawable.camera);
		map.put("txt", "Photoes");
		list.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.mic);
		map.put("txt", "Audios");
		list.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.videorecord);
		map.put("txt", "Videos");
		list.add(map);

		map = new HashMap<String, Object>();
		map.put("img", R.drawable.folder);
		map.put("txt", "Files");
		list.add(map);

		return list;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == RESULT_OK) {
			if (requestCode == Gallery_REQUEST_CODE) {
				try {
					final ContentResolver cr = this.getContentResolver();
					Uri uri = data.getData();
					Cursor c = cr.query(uri, new String[] {
							MediaStore.MediaColumns.DATA,
							MediaStore.MediaColumns.MIME_TYPE }, null, null,
							null);
					String filetype = "";
					String path = "";
					Bitmap bp = null;
					try {
						if (c.moveToFirst()) {
							path = c.getString(0);
							String mimetype = c.getString(1);
							if (mimetype.contains("video")) {
								filetype = Constants.FileTypeVideo;
								bp = ThumbnailUtils.createVideoThumbnail(path,
										MediaStore.Video.Thumbnails.MICRO_KIND);
							} else if (mimetype.contains("audio")) {
								filetype = Constants.FileTypeAudio;
								bp = BitmapFactory.decodeResource(
										this.getResources(),
										R.drawable.audio_file_middle);
							} else if (mimetype.contains("image")) {
								filetype = Constants.FileTypeImage;
								Bitmap bitmap = BitmapFactory.decodeFile(path);
								bp = ThumbnailUtils.extractThumbnail(bitmap,
										new Double(screenWidth
												* attachImageWidthScale)
												.intValue(),
										new Double(screenWidth
												* attachImageHeightScale)
												.intValue());
							}
						}
					} finally {
						c.close();
					}

					if (filetype != null && filetype.length() > 0
							&& path != null && path.length() > 0 && bp != null) {
						this.logAttach.setVisibility(View.VISIBLE);
						Bundle args = new Bundle();
						args.putString(MultiFileActionListener.FileTypeKey,
								filetype);
						args.putString(MultiFileActionListener.FilePathKey,
								path);
						logAttach.setTag(args);
						logAttach
								.setOnClickListener(new MultiFileActionListener());
						logAttach.setVisibility(View.VISIBLE);
						this.logAttach.setImageBitmap(bp);
					}

				} catch (Exception e) {
					Log.e(TAG, "Cannot obtain photo", e);
				}
			} else if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
				ArrayList<String> matches = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				String str = "";
				if (matches != null && matches.size() > 0) {
					for (String v : matches) {
						str = str + v;
					}
				}
			} else if (requestCode == Camera_REQUEST_CODE) {
				final ContentResolver cr = getContentResolver();
				final String[] imageproject = new String[] {
						MediaStore.Images.ImageColumns._ID,
						MediaStore.Images.ImageColumns.DATE_TAKEN,
						MediaStore.Images.ImageColumns.DATA };
				Cursor imagecursor = cr.query(
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
						imageproject, null, null, imageproject[1] + " DESC");
				try {
					if (imagecursor.moveToFirst()) {
						String uristringpic = "content://media/external/images/media/"
								+ imagecursor.getInt(0);
						Uri newuri = Uri.parse(uristringpic);
						ImageView icon = (ImageView) findViewById(R.id.log_attach);
						Bitmap bitmap = BitmapFactory.decodeStream(cr
								.openInputStream(newuri));
						icon.setImageBitmap(ThumbnailUtils.extractThumbnail(
								bitmap,
								new Double(screenWidth * attachImageWidthScale)
										.intValue(),
								new Double(screenWidth * attachImageHeightScale)
										.intValue()));
						icon.setVisibility(View.VISIBLE);
						Bundle args = new Bundle();
						args.putString(MultiFileActionListener.FileTypeKey,
								Constants.FileTypeImage);
						args.putString(MultiFileActionListener.FilePathKey,
								imagecursor.getString(2));
						icon.setTag(args);

						icon.setOnClickListener(new MultiFileActionListener());
					}
				} catch (Exception e) {
					Log.e(TAG, "file not found", e);
				} finally {
					imagecursor.close();
				}
			} else if (requestCode == VIDEO_REQUEST_CODE) {
				final ContentResolver cr = getContentResolver();
				final String[] videoproject = new String[] {
						MediaStore.Video.VideoColumns._ID,
						MediaStore.Video.VideoColumns.DATE_TAKEN,
						MediaStore.Video.VideoColumns.DATA };
				Cursor vcursor = cr.query(
						MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
						videoproject, null, null, videoproject[1] + " DESC");
				try {
					if (vcursor.moveToFirst()) {
						String loc = vcursor.getString(2);
						Bitmap b = ThumbnailUtils.createVideoThumbnail(loc,
								MediaStore.Video.Thumbnails.MICRO_KIND);
						ImageView icon = (ImageView) findViewById(R.id.log_attach);
						icon.setImageBitmap(b);
						Bundle args = new Bundle();
						args.putString(MultiFileActionListener.FileTypeKey,
								Constants.FileTypeVideo);
						args.putString(MultiFileActionListener.FilePathKey,
								vcursor.getString(2));
						icon.setTag(args);

						icon.setOnClickListener(new MultiFileActionListener());
						icon.setVisibility(View.VISIBLE);
					}
				} catch (Exception e) {
					Log.e(TAG, "file not found", e);
				} finally {
					vcursor.close();
				}

			} else if (requestCode == AUDIO_REQUEST_CODE) {
				String filepath = data.getStringExtra("filepath");
				if (filepath != null) {
					this.logAttach.setVisibility(View.VISIBLE);
					Bundle args = new Bundle();
					args.putString(MultiFileActionListener.FileTypeKey,
							Constants.FileTypeAudio);
					args.putString(MultiFileActionListener.FilePathKey,
							filepath);
					logAttach.setTag(args);
					logAttach.setOnClickListener(new MultiFileActionListener());
					logAttach.setVisibility(View.VISIBLE);
					this.logAttach.setImageBitmap(BitmapFactory.decodeResource(
							this.getResources(), R.drawable.audio_file_middle));
				}
			}
		}
	}
	
	private void setAttachFile(Intent data){
		try {
			final ContentResolver cr = this.getContentResolver();
			Uri uri = data.getData();
			if(uri==null && data.getExtras()!=null){
				String str_uri = data.getExtras().getString("android.intent.extra.STREAM");
				if(str_uri!=null)
					uri = Uri.parse(str_uri);
			}
			if(uri==null)
				return;
			Cursor c = cr.query(uri, new String[] {
					MediaStore.MediaColumns.DATA,
					MediaStore.MediaColumns.MIME_TYPE }, null, null,
					null);
			String filetype = "";
			String path = "";
			Bitmap bp = null;
			try {
				if (c.moveToFirst()) {
					path = c.getString(0);
					String mimetype = c.getString(1);
					if (mimetype.contains("video")) {
						filetype = Constants.FileTypeVideo;
						bp = ThumbnailUtils.createVideoThumbnail(path,
								MediaStore.Video.Thumbnails.MICRO_KIND);
					} else if (mimetype.contains("audio")) {
						filetype = Constants.FileTypeAudio;
						bp = BitmapFactory.decodeResource(
								this.getResources(),
								R.drawable.audio_file_middle);
					} else if (mimetype.contains("image")) {
						filetype = Constants.FileTypeImage;
						Bitmap bitmap = BitmapFactory.decodeFile(path);
						bp = ThumbnailUtils.extractThumbnail(bitmap,
								new Double(screenWidth
										* attachImageWidthScale)
										.intValue(),
								new Double(screenWidth
										* attachImageHeightScale)
										.intValue());
					}
				}
			} finally {
				c.close();
			}

			if (filetype != null && filetype.length() > 0
					&& path != null && path.length() > 0 && bp != null) {
				this.logAttach.setVisibility(View.VISIBLE);
				Bundle args = new Bundle();
				args.putString(MultiFileActionListener.FileTypeKey,
						filetype);
				args.putString(MultiFileActionListener.FilePathKey,
						path);
				logAttach.setTag(args);
				logAttach
						.setOnClickListener(new MultiFileActionListener());
				logAttach.setVisibility(View.VISIBLE);
				this.logAttach.setImageBitmap(bp);
			}

		} catch (Exception e) {
			Log.e(TAG, "Cannot obtain photo", e);
		}
	}

	@Override
	public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		if (token == SettingLanguageQuery._TOKEN) {
			this.onTitleQueryComplete(cursor);
		} else if (token == SettingQuery._TOKEN) {
			this.onCategoryQueryComplete(cursor);
		} else if (token == ItemsQuery._TOKEN) {
			this.onItemQueryComplete(cursor);
		} else if (token == ItemtitlesQuery._TOKEN) {
			this.onItemtitleQueryComplete(cursor);
		} else if (token == ItemtagsQuery._TOKEN) {
			this.onItemtagQueryComplete(cursor);
		} else if (cursor != null)
			cursor.close();

		if (this.pd != null && this.mSettingLanguageCursor
				&& this.mSettingCursor) {
			if (this.itemId != null) {
				if (this.mItemCursor && this.mItemtagCursor
						&& this.mItemtitleCursor)
					pd.dismiss();
			} else if (pd != null && pd.isShowing()) {
				pd.dismiss();
			}
		}
	}

	private void onTitleQueryComplete(Cursor cursor) {
		try {
			this.mSettingCursor = true;
			if (!cursor.moveToFirst())
				return;
			do {
//				LinearLayout titleLayout = (LinearLayout) this
//						.findViewById(R.id.log_title_block);
				String content = cursor.getString(SettingLanguageQuery.NAME);
				String code = cursor.getString(SettingLanguageQuery.CODE);
				this.addTitleField(code, content);
//				LinearLayout titleBlock = (LinearLayout) this
//						.getLayoutInflater().inflate(R.layout.log_edit_title,
//								null);
//				TextView name = (TextView) titleBlock
//						.findViewById(R.id.txt_title_name);
//				EditText title = (EditText) titleBlock
//						.findViewById(R.id.txt_title_content);
//				name.setText(content);
//				title.setTag(code);
//				titleLayout.addView(titleBlock);
			} while (cursor.moveToNext());
		} finally {
			cursor.close();
		}
	}
	
	private EditText addTitleField(final String code, String content) {
		LinearLayout titleLayout = (LinearLayout) this
				.findViewById(R.id.log_title_block);
	
		LinearLayout titleBlock = (LinearLayout) this.getLayoutInflater()
				.inflate(R.layout.log_edit_title, null);
		Button name = (Button) titleBlock.findViewById(R.id.txt_title_name);
		name.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onTranslateClick(code);
			}
		});
		
		EditText title = (EditText) titleBlock
				.findViewById(R.id.txt_title_content);
		
		name.setText(content);
		title.setTag(code);
		titleLayout.addView(titleBlock);
		return title;
	}

	protected void onTranslateClick(String code) {
		EditText text = this.getTitleEditText(code);
		if(text==null || !StringUtils.isBlank(text.getText().toString())){
			return;
		}
		String testText = "";
		for(String t:getTitleValues().values()){
			if(!StringUtils.isBlank(t)){
				testText = t;
			}
		}
		if(StringUtils.isBlank(testText)){
			return;
		}
		
		Toast.makeText(QestionActivity.this, R.string.item_title_intranslate, Toast.LENGTH_SHORT).show();
		
		String uid = ContextUtil.getUserId(this);
		JSONObject jsonObject = new JSONObject(this.getTitleValues());
		String titles = jsonObject.toString();
		
		Thread thread = new TranslateThread(uid, code, titles);
		thread.start();
	}

	private void onCategoryQueryComplete(Cursor cursor) {
		try {
			this.mSettingLanguageCursor = true;
			if (!cursor.moveToFirst())
				return;
			this.cate_names = new String[cursor.getCount()];
			this.cate_values = new String[cursor.getCount()];
			int i = 0;
			do {
				this.cate_values[i] = cursor.getString(SettingQuery.CONTENT);
				this.cate_names[i] = cursor.getString(SettingQuery.NAME);
				i++;
			} while (cursor.moveToNext());

		} finally {
			cursor.close();
		}

	}

	public void onItemQueryComplete(Cursor cursor) {
		try {
			this.mItemCursor = true;
			if (!cursor.moveToFirst())
				return;

			// String photoUrl = cursor.getString(ItemsQuery.PHOTO_URL);
			// if(photoUrl!=null){
			// Bitmap bitmap =
			// BitmapUtil.getURLBitmap(ApiConstants.Image_Server_Url+photoUrl+ApiConstants.MiddleSizePostfix);
			// // Uri uri =
			// Uri.parse(ApiConstants.Image_Server_Url+photoUrl+ApiConstants.MiddleSizePostfix);
			// if (bitmap != null){
			// LinearLayout fileBlock =
			// (LinearLayout)this.findViewById(R.id.log_file_block);
			// fileBlock.setVisibility(View.VISIBLE);
			// ImageView imageView = new ImageView(this);
			// imageView.setImageBitmap(bitmap);
			// fileBlock.addView(imageView);
			// }
			// }

			String note = cursor.getString(ItemsQuery.NOTE);
			if (note != null) {
				this.logNote.setText(note);
			}
			String category = cursor.getString(ItemsQuery.Category);
			if (category != null)
				this.selectedCat = category;
		} finally {
			cursor.close();
		}
	}

	public void onItemtitleQueryComplete(Cursor cursor) {
		try {
			this.mItemtitleCursor = true;
			if (!cursor.moveToFirst())
				return;
			do {
				String code = cursor.getString(ItemtitlesQuery.CODE);
				EditText title = (EditText) this.getTitleEditText(code);
				String content = cursor.getString(ItemtitlesQuery.CONTENT);
				if(title==null)
					title = this.addTitleField(code, cursor.getString(ItemtitlesQuery.NAME));
				title.setText(content);
			} while (cursor.moveToNext());
		} finally {
			cursor.close();
		}
	}

	public void onItemtagQueryComplete(Cursor cursor) {
		try {
			this.mItemtagCursor = true;
			if (!cursor.moveToFirst())
				return;
			do {
				String tag = cursor.getString(ItemtagsQuery.TAG);
				this.tags.add(tag);
			} while (cursor.moveToNext());

		} finally {
			cursor.close();
		}
	}

	public Map<String, String> getTitleValues() {
		int count = titleLayout.getChildCount();
		Map<String, String> titles = new HashMap<String, String>();
		for (int i = 0; i < count; i++) {
			View view = titleLayout.getChildAt(i);
			if (view instanceof LinearLayout) {
				LinearLayout child = (LinearLayout) view;
				int ac = child.getChildCount();
				for (int j = 0; j < ac; j++) {
					View cv = child.getChildAt(j);
					if (cv instanceof EditText) {
						EditText et = (EditText) cv;
						if (et.getText() != null && et.getText().length() > 0
								&& et.getTag() != null) {
							titles.put(et.getTag().toString(), et.getText()
									.toString());
						}
					}
				}
			}
		}

		return titles;
	}

	public EditText getTitleEditText(String code) {
		int count = titleLayout.getChildCount();
		for (int i = 0; i < count; i++) {
			View view = titleLayout.getChildAt(i);
			if (view instanceof LinearLayout) {
				LinearLayout child = (LinearLayout) view;
				int ac = child.getChildCount();
				for (int j = 0; j < ac; j++) {
					View cv = child.getChildAt(j);
					if (cv instanceof EditText) {
						EditText et = (EditText) cv;
						if (code.equals(et.getTag()))
							return et;
					}
				}
			}
		}
		return null;
	}

	public class MultiFileActionListener implements OnClickListener {
		public static final String FileTypeKey = "filetype";
		public static final String FilePathKey = "filepath";
//		public static final String BitmapKey = "bitmap";

		@Override
		public void onClick(View v) {
			showDialog(DialogAttachAction, (Bundle) v.getTag());
		}
	}

	public Map<String, Object> getMultiFileParams(Bundle args) {
		Map<String, Object> map = new HashMap<String, Object>();
		String filetype = args.getString("filetype");
		String filepath = args.getString("filepath");
		Bitmap bp = null;
		try {
			if (Constants.FileTypeVideo.equals(filetype)) {
				bp = ThumbnailUtils.createVideoThumbnail(filepath,
						MediaStore.Video.Thumbnails.MICRO_KIND);
			} else if (Constants.FileTypeAudio.equals(filetype)) {
				bp = BitmapFactory.decodeResource(this.getResources(),
						R.drawable.audio_file_large);
			} else if (Constants.FileTypeImage.equals(filetype)) {
				bp = ThumbnailUtils.extractThumbnail(
						BitmapFactory.decodeFile(filepath),
						(int) (this.screenWidth * attachImageWidthScale),
						(int) (this.screenWidth * attachImageHeightScale));
			}
			File file = new File(filepath);
			if (bp != null)
				map.put("bitmap", bp);
			map.put("filename", file.getCanonicalPath());
			map.put("filesize", file.length());
		} catch (Exception e) {

		}
		return map;
	}

	private interface SettingQuery {
		public static String[] PROJECTION = { Settings.AUTHOR_ID,
				Settings.CONTENT, Settings.NAME, Settings.FIELD, Settings.NUM };
		public static int _TOKEN = 1;

		int CONTENT = 1;
		int NAME = 2;
	}

	private interface SettingLanguageQuery {
		public static String[] PROJECTION = { Settings.AUTHOR_ID,
				Settings.CONTENT, Settings.NAME, Languages.CODE,
				Settings.FIELD, Settings.NUM };
		public static int _TOKEN = 2;

		int NAME = 2;
		int CODE = 3;
	}

	private interface ItemsQuery {
		int _TOKEN = 3;
		String[] PROJECTION = { BaseColumns._ID, Items.ITEM_ID,
				Items.NICK_NAME, Items.PHOTO_URL, Items.FILE_TYPE, Items.TAG,
				Items.NOTE,
				// Items.PLACE,
				Items.LNGITUTE, Items.LATITUTE, Items.UPDATE_TIME,
				Items.CATEGORY };
		int NOTE = 6;
		int Category = 10;
	}

	private interface ItemtitlesQuery {
		int _TOKEN = 4;
		String[] PROJECTION = { Itemtitles._ID, Itemtitles.ITEMTITLE_ID,
				Itemtitles.ITEM_ID, Itemtitles.LANGUAGE_ID, Itemtitles.CONTENT,
				Languages.NAME, Languages.CODE };
		int CONTENT = 4;
		int NAME = 5;
		int CODE = 6;
	}

	private interface ItemtagsQuery {
		int _TOKEN = 5;
		String[] PROJECTION = { Itemtags._ID, Itemtags.ITEMTAG_ID,
				Itemtags.ITEM_ID, Itemtags.TAG };

		int TAG = 3;
	}
	
	class TranslateThread extends Thread{
		private String uid;
		private String target;
		private String titles;
		
		public TranslateThread(String uid, String target, String titles){
			super();
			this.uid = uid;
			this.target = target;
			this.titles = titles;
		}
		
		@Override
		public void run() {
			DefaultHttpClient client = HttpClientFactory.getInstance(QestionActivity.this);
			
			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
			qparams.add(new BasicNameValuePair("uid", uid));
			qparams.add(new BasicNameValuePair("target", target));
			qparams.add(new BasicNameValuePair("titles", titles));
			
			HttpGet httpGet = new HttpGet(ApiConstants.TRANSLATE_TITLE_URI+"?"+URLEncodedUtils.format(qparams, "UTF-8"));
			try {
				HttpResponse response=client.execute(httpGet);
				if(HttpStatus.SC_OK == response.getStatusLine().getStatusCode()){
					String result = EntityUtils.toString(response.getEntity());
					if(!StringUtils.isBlank(result)){
						Message msg = new Message();
						Bundle bundle = new Bundle();
						bundle.putString("code", target);
						bundle.putString("result", result);
						msg.setData(bundle);
						msg.what = TRANSLATE_MESSAGE_SUCCESS;
						translateHandler.sendMessage(msg);
					}
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
