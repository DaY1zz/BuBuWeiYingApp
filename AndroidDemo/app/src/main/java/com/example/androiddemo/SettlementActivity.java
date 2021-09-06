package com.example.androiddemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SettlementActivity extends AppCompatActivity {

    private TextView tv_totalPoint_sm;
    private TextView tv_currentPoint_sm;
    private Button btn_voice;

    private boolean isPlayingMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settlement);

        tv_totalPoint_sm = findViewById(R.id.tv_totalPoint_sm);
        tv_currentPoint_sm = findViewById(R.id.tv_currentPoint_sm);
        btn_voice = findViewById(R.id.btnVoice3);

        Intent intent = getIntent();
        tv_currentPoint_sm.setText(String.valueOf(intent.getIntExtra("currentPoint",0)));
        tv_totalPoint_sm.setText(String.valueOf(intent.getIntExtra("totalPoint",0)));
        isPlayingMusic = intent.getBooleanExtra("isPlayingMusic",true);

        //判断是否继续播放音乐
        if(!isPlayingMusic){
            btn_voice.setBackground(getDrawable(R.mipmap.jingyin));
        }

    }

    public void exitGame(View view) {
        Intent intent = new Intent(this,StartActivity.class);
        intent.putExtra("isPlayingMusic",isPlayingMusic);
        startActivity(intent);
    }

    public void tryAgain(View view) {

        Intent intent = new Intent();
        intent.putExtra("isPlayingMusic",isPlayingMusic);
        setResult(RESULT_OK,intent);
        finish();
    }

    public void changeMusicState(View view) {
        if(isPlayingMusic){
            Intent intent = new Intent(SettlementActivity.this,BGMService.class);
            startService(intent);
            view.setBackground(getDrawable(R.mipmap.jingyin));
            isPlayingMusic=false;
        }
        else {
            Intent intent = new Intent(SettlementActivity.this,BGMService.class);
            startService(intent);
            view.setBackground(getDrawable(R.mipmap.yinyue));
            isPlayingMusic=true;
        }

    }
}