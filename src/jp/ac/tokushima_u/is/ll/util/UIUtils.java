package jp.ac.tokushima_u.is.ll.util;
import java.util.TimeZone;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.service.ContextAwareService;
import jp.ac.tokushima_u.is.ll.service.SyncService;
import jp.ac.tokushima_u.is.ll.ui.HomeActivity;
import jp.ac.tokushima_u.is.ll.ui.LoginActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.LevelListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class UIUtils {

    /**
     * Time zone to use when formatting all session times. To always use the
     * phone local time, use {@link TimeZone#getDefault()}.
     */
    public static TimeZone CONFERENCE_TIME_ZONE = TimeZone.getTimeZone("America/Los_Angeles");

    public static final long CONFERENCE_START_MILLIS = ParserUtils.parseTime(
            "2011-01-19T09:00:00.000-07:00");
    public static final long CONFERENCE_END_MILLIS = ParserUtils.parseTime(
            "2010-01-20T17:30:00.000-07:00");

    /** Flags used with {@link DateUtils#formatDateRange}. */
    private static final int TIME_FLAGS = DateUtils.FORMAT_SHOW_TIME
            | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY;

    private static final int BRIGHTNESS_THRESHOLD = 150;

//    /** {@link StringBuilder} used for formatting time block. */
//    private static StringBuilder sBuilder = new StringBuilder(50);
//    /** {@link Formatter} used for formatting time block. */
//    private static Formatter sFormatter = new Formatter(sBuilder, Locale.getDefault());
//
    private static StyleSpan sBoldSpan = new StyleSpan(Typeface.BOLD);

    public static void setTitleBarColor(View titleBarView, int color) {
        final ViewGroup titleBar = (ViewGroup) titleBarView;
        titleBar.setBackgroundColor(color);

        /*
         * Calculate the brightness of the titlebar color, based on the commonly known
         * brightness formula:
         *
         * http://en.wikipedia.org/wiki/HSV_color_space%23Lightness
         */
        int brColor = (30 * Color.red(color) +
                       59 * Color.green(color) +
                       11 * Color.blue(color)) / 100;
        if (brColor > BRIGHTNESS_THRESHOLD) {
            ((TextView) titleBar.findViewById(R.id.title_text)).setTextColor(
                    titleBar.getContext().getResources().getColor(R.color.title_text_alt));

            // Iterate through all children of the titlebar and if they're a LevelListDrawable,
            // set their level to 1 (alternate).
            // TODO: find a less hacky way of doing this.
            titleBar.post(new Runnable() {
                public void run() {
                    final int childCount = titleBar.getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        final View child = titleBar.getChildAt(i);
                        if (child instanceof ImageButton) {
                            final ImageButton childButton = (ImageButton) child;
                            if (childButton.getDrawable() != null &&
                                childButton.getDrawable() instanceof LevelListDrawable) {
                                ((LevelListDrawable) childButton.getDrawable()).setLevel(1);
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * Invoke "home" action, returning to {@link HomeActivity}.
     */
    public static void goHome(Context context) {
        final Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public static void goLogin(Context context) {
        final Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }
    
    public static void goSync(Context context) {
    	final Intent intent = new Intent(Intent.ACTION_SYNC, null, context, SyncService.class);
        context.startService(intent);
    }
    
    public static void goSync(Context context, Parcelable receiver) {
    	final Intent intent = new Intent(Intent.ACTION_SYNC, null, context, SyncService.class);
    	intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, receiver);
        context.startService(intent);
    }
    
    public static void goManualSync(Context context, Parcelable receiver) {
    	final Intent intent = new Intent(Intent.ACTION_SYNC, null, context,
				SyncService.class);
		intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, receiver);
		intent.putExtra(SyncService.EXTRA_SYNC_TYPE, SyncService.SYNC_TYPE_MANUAL);
		context.startService(intent);
    }
    
    
    public static void goContextAware(Context context) {
    	final Intent intent = new Intent(context, ContextAwareService.class);
        context.startService(intent);
    }
    
//    
//    public static void refreshRegisterId(Context context){
//    	String registerId = ContextUtil.getRegisterId(context);
//    	if(registerId==null||registerId.length()<=0){
//    		C2DMReceiver.refreshAppC2DMRegistrationState(context);
//    	}
//    }
    
    public static boolean checkUser(Context context){
	    SharedPreferences setting = context.getSharedPreferences(Constants.SETTING_INFOS_FILE, Context.MODE_PRIVATE);
        String username = setting.getString(Constants.SavedUserName, "");
        String password = setting.getString(Constants.SavedPassword, "");
        String userid = setting.getString(Constants.SavedUserId, "");
        if(!StringUtils.isBlank(username)&&!StringUtils.isBlank(password)&&!StringUtils.isBlank(userid))
        	return true;
        else
        	return false;
    }  
    
    /**
     * Invoke "search" action, triggering a default search.
     */
    public static void goSearch(Activity activity) {
        activity.startSearch(null, false, Bundle.EMPTY, false);
    }
    
    public static void goMap(Context context, Double lat, Double lng){
    	String uriString = "geo:"+lat+","+lng;
    	Uri uri = Uri.parse(uriString);
    	Intent intent = new Intent();
    	intent.setData(uri);
    	context.startActivity(intent);
    }

    /**
     * Format and return the given {@link Blocks} and {@link Rooms} values using
     * {@link #CONFERENCE_TIME_ZONE}.
     */
    public static String formatSessionSubtitle(long blockStart, long blockEnd,
            String roomName, Context context) {
        TimeZone.setDefault(CONFERENCE_TIME_ZONE);

        // NOTE: There is an efficient version of formatDateRange in Eclair and
        // beyond that allows you to recycle a StringBuilder.
        final CharSequence timeString = DateUtils.formatDateRange(context,
                blockStart, blockEnd, TIME_FLAGS);

        return context.getString(R.string.session_subtitle, timeString, roomName);
    }

    /**
     * Populate the given {@link TextView} with the requested text, formatting
     * through {@link Html#fromHtml(String)} when applicable. Also sets
     * {@link TextView#setMovementMethod} so inline links are handled.
     */
    public static void setTextMaybeHtml(TextView view, String text) {
        if (text.contains("<") && text.contains(">")) {
            view.setText(Html.fromHtml(text));
            view.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            view.setText(text);
        }
    }

    public static void setTitleColor(TextView title) {
        int colorId = android.R.color.primary_text_light;
//        int subColorId = android.R.color.secondary_text_light;

        final Resources res = title.getResources();
        title.setTextColor(res.getColor(colorId));
    }

    /**
     * Given a snippet string with matching segments surrounded by curly
     * braces, turn those areas into bold spans, removing the curly braces.
     */
    public static Spannable buildStyledSnippet(String snippet) {
        final SpannableStringBuilder builder = new SpannableStringBuilder(snippet);

        // Walk through string, inserting bold snippet spans
        int startIndex = -1, endIndex = -1, delta = 0;
        while ((startIndex = snippet.indexOf('{', endIndex)) != -1) {
            endIndex = snippet.indexOf('}', startIndex);

            // Remove braces from both sides
            builder.delete(startIndex - delta, startIndex - delta + 1);
            builder.delete(endIndex - delta - 1, endIndex - delta);

            // Insert bold style
            builder.setSpan(sBoldSpan, startIndex - delta, endIndex - delta - 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            delta += 2;
        }

        return builder;
    }
}
