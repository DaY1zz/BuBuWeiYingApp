package com.example.androiddemo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class WaitingActivity extends AppCompatActivity {

    private boolean startFlag = true;
    private int currentPoint = 0;
    private int totalPoint = 0;

    private TextView tv_currentPoint;
    private TextView tv_totalPoint;
    private Button btn_voice;

    private boolean isPlayingMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        tv_currentPoint = findViewById(R.id.tv_currentPoint);
        tv_totalPoint = findViewById(R.id.tv_totalPoint );
        btn_voice = findViewById(R.id.btnVoice2);

        Intent intent = getIntent();
        isPlayingMusic = intent.getBooleanExtra("isPlayingMusic",true);

        //判断是否继续播放音乐
        if(!isPlayingMusic){
            btn_voice.setBackground(getDrawable(R.mipmap.jingyin));
        }


        tv_currentPoint.setText(String.valueOf(currentPoint));
        tv_totalPoint.setText(String.valueOf(totalPoint));
    }

    public void rollDice(View view) {
        AnimationDrawable dice = (AnimationDrawable) view.getBackground();
        if(startFlag){
            dice.start();
            startFlag = false;
        }
        else{
            dice.stop();
            startFlag = true;
        }

    }

    public void answer1(View view) {
        Intent intent = new Intent(this,AnswerActivity.class);
        intent.putExtra("Video",1);
        startActivityForResult(intent,0);
        Intent musicIntent = new Intent(this,BGMService.class);
        stopService(musicIntent);
    }

    public void answer2(View view) {
        Intent intent = new Intent(this,AnswerActivity.class);
        intent.putExtra("Video",2);
        startActivityForResult(intent,0);
        Intent musicIntent = new Intent(this,BGMService.class);
        stopService(musicIntent);
    }

    public void victory(View view) {
        Intent intent = new Intent(this,SettlementActivity.class);
        intent.putExtra("currentPoint",currentPoint);
        intent.putExtra("totalPoint",totalPoint);
        intent.putExtra("isPlayingMusic",isPlayingMusic);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //答题界面返回数据后操作
        if(requestCode==0){
            Bundle bundle = data.getBundleExtra("bundle");
            currentPoint += bundle.getInt("updatedPoint");
            totalPoint += bundle.getInt("updatedPoint");
            tv_currentPoint.setText(String.valueOf(currentPoint));
            tv_totalPoint.setText(String.valueOf(totalPoint));

            if(!isPlayingMusic){
                btn_voice.setBackground(getDrawable(R.mipmap.yinyue));
                isPlayingMusic=true;
            }

            Intent intent = new Intent(WaitingActivity.this,BGMService.class);
            startService(intent);

        }
        //结算界面返回后操作
        else if(requestCode==1){

            isPlayingMusic = data.getBooleanExtra("isPlayingMusic",isPlayingMusic);
            if(!isPlayingMusic){
                btn_voice.setBackground(getDrawable(R.mipmap.jingyin));
            }
            else
            {
                btn_voice.setBackground(getDrawable(R.mipmap.yinyue));
            }
            currentPoint=0;
            tv_currentPoint.setText(String.valueOf(currentPoint));
        }



    }

    public void goBack(View view) {
    }

    public void changeMusicState(View view) {
        if(isPlayingMusic){
            Intent intent = new Intent(WaitingActivity.this,BGMService.class);
            startService(intent);
            view.setBackground(getDrawable(R.mipmap.jingyin));
            isPlayingMusic=false;
        }
        else {
            Intent intent = new Intent(WaitingActivity.this,BGMService.class);
            startService(intent);
            view.setBackground(getDrawable(R.mipmap.yinyue));
            isPlayingMusic=true;
        }

    }
}