package jp.ac.tokushima_u.is.ll.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

//プレーヤーサービス
public class TTSService extends Service //(3)
    implements MediaPlayer.OnCompletionListener {
    private MediaPlayer player;//プレーヤー

    //サービス開始時に呼ばれる
    @Override
    public void onStart(Intent intent,int startID) {
        //ノティフィケーションの表示
//        showNotification(this,R.drawable.icon,
//            "BGMを再生します",
//            "プレイヤーサービス",
//            "プレイヤーサービスを開始しました");

        //サウンドの再生
		if(intent.getData()!=null)
			playSound(intent.getData());
    }

    //サービス解放時に呼ばれる
    @Override
    public void onDestroy() {
        //ノティフィケーションマネージャの取得
        NotificationManager nm;
        nm=(NotificationManager)getSystemService(
            Context.NOTIFICATION_SERVICE);

        //ノティフィケーションのキャンセル(6)
        nm.cancel(0);

        //サウンドの停止
        stopSound();

        //トーストの表示
        showToast(this,"プレーヤーサービスを停止しました");
    }

    //サービスへの通信チャンネルを戻す
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //サウンドの再生
    public void playSound(Uri uri) {
        try {
//        	Uri uri = Uri.parse("http://ll.is.tokushima-u.ac.jp/learninglog/api/translate/tts?ie=UTF-8&lang=en&text='japanese'");
        	player = new MediaPlayer();
        	player.setOnCompletionListener(this);
        	player.setDataSource(this, uri);
        	player.prepare();
//            player=MediaPlayer.create(this, uri);
            player.start();
            player.setOnCompletionListener(this);
        } catch (Exception e) {
        	Log.e("test", "exception occured",e);
        }
    }

    //サウンドの停止
    public void stopSound() {
        try {
            player.stop();
            player.setOnCompletionListener(null);
            player.release();
            player=null;
        } catch (Exception e) {
        }
    }

    //サウンド再生終了時に呼ばれる
    public void onCompletion(MediaPlayer mediaPlayer) {
        stopSound();
    }

    //ノティフィケーションの表示
//    private void showNotification(Context context,
//        int iconID,String ticker,String title,String message) {
//        //ノティフィケーションマネージャの取得(4)
//        NotificationManager nm;
//        nm=(NotificationManager)getSystemService(
//            Context.NOTIFICATION_SERVICE);
//
//        //ノティフィケーションオブジェクトの生成(5)
//        Notification notification=new Notification(iconID,
//            ticker,System.currentTimeMillis());
//        PendingIntent intent=PendingIntent.getActivity(context,0,
//            new Intent(context,net.npaka.serviceex.ServiceEx.class),0);
//        notification.setLatestEventInfo(context,
//            title,message,intent);
//
//        //ノティフィケーションのキャンセル(6)
//        nm.cancel(0);
//
//        //ノティフィケーションの表示(7)
//        nm.notify(0,notification);
//    }

    //トーストの表示
    private static void showToast(Context context,String text) {
        Toast.makeText(context,text,Toast.LENGTH_SHORT).show();
    }
}