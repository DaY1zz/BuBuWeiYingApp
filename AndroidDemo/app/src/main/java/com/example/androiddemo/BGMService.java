package com.example.androiddemo;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.view.View;

public class BGMService extends Service implements MediaPlayer.OnCompletionListener {

    MediaPlayer player;
    private final IBinder binder = new AudioBinder();

    public BGMService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        player = MediaPlayer.create(this, R.raw.bgm);
        player.setLooping(true);//设置为循环播放
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (!player.isPlaying()) {
            player.start();
        }
        else {
            player.pause();
        }
        return START_STICKY;
    }



    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        stopSelf();// 结束则结束Service
    }

    public void onDestroy() {
        super.onDestroy();
        if (player.isPlaying()) {
            player.stop();
        }
        player.release();
    }


    public class AudioBinder extends Binder {
        // 返回Service对象
        public BGMService getService() {
            return BGMService.this;
        }
    }

}

