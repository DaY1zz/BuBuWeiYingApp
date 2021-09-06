package com.example.androiddemo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.material.button.MaterialButtonToggleGroup;

public class AnswerActivity extends AppCompatActivity {

    private VideoView videoView;
    private TextView tv_title;
    private Button btn_answer1;
    private Button btn_answer2;
    private Button btn_answer3;

    private int flag = 0;
    private int videoIndex;
    private int gettingPoint;



    private MediaController mc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);

        videoView = findViewById(R.id.videoView);
        tv_title = findViewById(R.id.tv_Title);
        btn_answer1 = findViewById(R.id.btnKey1);
        btn_answer2 = findViewById(R.id.btnKey2);
        btn_answer3 = findViewById(R.id.btnKey3);

        mc = new MediaController(this);
        videoView.setMediaController(mc);

        Intent intent = getIntent();
        videoIndex = intent.getIntExtra("Video",0);


        String videoUri;
        String title;
        String answer1;
        String answer2;
        String answer3;

        switch (videoIndex){
            case 1:
                videoUri = "android.resource://" + getPackageName() + "/" + R.raw.video;
                title = getString(R.string.title1);
                answer1 = getString(R.string.answer1_1);
                answer2 = getString(R.string.answer1_2);
                answer3 = getString(R.string.answer1_3);
                break;
            case 2:
                videoUri = "android.resource://" + getPackageName() + "/" + R.raw.video2;
                title = getString(R.string.title2);
                answer1 = getString(R.string.answer2_1);
                answer2 = getString(R.string.answer2_2);
                answer3 = getString(R.string.answer2_3);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + videoIndex);
        }

        videoView.setVideoURI(Uri.parse(videoUri));
        tv_title.setText(title);
        btn_answer1.setText(answer1);
        btn_answer2.setText(answer2);
        btn_answer3.setText(answer3);

        videoView.seekTo( 1 );
        //videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                videoView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        videoView.start();
                    }
                });
            }
        });
    }

    public void changeStatus1(View view) {
        btn_answer2.setBackground(getDrawable(R.drawable.bc_view3));
        btn_answer3.setBackground(getDrawable(R.drawable.bc_view3));

        flag = 1;
        Log.i("按钮",String.valueOf(flag));
        view.setBackground(getDrawable(R.drawable.bc_view4));

    }
    public void changeStatus2(View view) {
        btn_answer1.setBackground(getDrawable(R.drawable.bc_view3));
        btn_answer3.setBackground(getDrawable(R.drawable.bc_view3));

        flag = 2;
        Log.i("按钮",String.valueOf(flag));
        view.setBackground(getDrawable(R.drawable.bc_view4));

    }
    public void changeStatus3(View view) {
        btn_answer1.setBackground(getDrawable(R.drawable.bc_view3));
        btn_answer2.setBackground(getDrawable(R.drawable.bc_view3));

        flag = 3;
        Log.i("按钮",String.valueOf(flag));
        view.setBackground(getDrawable(R.drawable.bc_view4));

    }

    public void submit(View view) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        switch (videoIndex){
            case 1:
            {
                if(flag==2){
                    //问题一回答正确,加50分
                    gettingPoint=50;
                    bundle.putInt("updatedPoint",gettingPoint);
                    intent.putExtra("bundle",bundle);
                    setResult(Activity.RESULT_OK,intent);
                    Toast.makeText(this,"回答正确！",Toast.LENGTH_SHORT).show();
                }else{
                    bundle.putInt("updatedPoint",gettingPoint);
                    intent.putExtra("bundle",bundle);
                    setResult(Activity.RESULT_OK,intent);
                    Toast.makeText(this,"回答错误",Toast.LENGTH_SHORT).show();
                }
                //数据回调
                finish();
                return;
            }
            case 2:{
                if(flag==3){
                    //问题二回答正确，加30分
                    gettingPoint=30;
                    bundle.putInt("updatedPoint",gettingPoint);
                    intent.putExtra("bundle",bundle);
                    setResult(Activity.RESULT_OK,intent);
                    Toast.makeText(this,"回答正确！",Toast.LENGTH_SHORT).show();
                }else{
                    //回答错误
                    bundle.putInt("updatedPoint",gettingPoint);
                    intent.putExtra("bundle",bundle);
                    setResult(Activity.RESULT_OK,intent);
                    Toast.makeText(this,"回答错误",Toast.LENGTH_SHORT).show();
                }
                //数据回调
                finish();
                return;
            }
        }
    }
}