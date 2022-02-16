package com.example.androiddemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androiddemo.entity.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StartActivity extends AppCompatActivity {

    private User preparedUser = new User("whu","123");
    private List<User> userList = new ArrayList<User>();
    private List<String> idList = new ArrayList<String>();
    private List<String> pswdList = new ArrayList<String>();

    private Button qqLogin;
    private Button vxLogin;
    private Button btn_voice;
    private ConstraintLayout startView;
    private ConstraintLayout registerView;
    private EditText id_rg;
    private EditText pswd_rg;
    private EditText pswd2_rg;
    private TextView tv_Tip;
    private EditText id_login;
    private EditText pswd_login;

    private boolean isPlayingMusic = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        userList.add(preparedUser);
        idList.add("whu");
        pswdList.add("123");

        qqLogin = findViewById(R.id.btnQQLogin);
        vxLogin = findViewById(R.id.btnVXLogin);
        startView = findViewById(R.id.viewGroup_start);
        registerView = findViewById(R.id.viewGroup_rg);
        id_rg = findViewById(R.id.et_userID_rg);
        tv_Tip = findViewById(R.id.tv_Tip);
        pswd_rg = findViewById(R.id.et_password_rg);
        pswd2_rg = findViewById(R.id.et_password2_rg);
        id_login = findViewById(R.id.et_userID);
        pswd_login = findViewById(R.id.et_password);
        btn_voice = findViewById(R.id.btnVoice);



        //设置两个icon大小
        Drawable qq = getDrawable(R.mipmap.qq);
        Drawable vx = getDrawable(R.mipmap.weixin);
        qq.setBounds(0,0,45,45);
        vx.setBounds(0,0,45,45);
        qqLogin.setCompoundDrawables(qq,null,null,null);
        vxLogin.setCompoundDrawables(vx,null,null,null);


    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        isPlayingMusic = intent.getBooleanExtra("isPlayingMusic",true);
        if(!isPlayingMusic){
            btn_voice.setBackground(getDrawable(R.mipmap.jingyin));
        }else{
            //播放背景音乐
            Intent musicIntent = new Intent(StartActivity.this,BGMService.class);
            startService(musicIntent);
        }
    }
    //点击返回按钮
    public void goBack(View view) {
        if(startView.getVisibility()==View.VISIBLE){
            finish();
        }
        else if(registerView.getVisibility()==View.VISIBLE){
            id_rg.setText("");
            pswd_rg.setText("");
            pswd2_rg.setText("");
            registerView.setVisibility(View.INVISIBLE);
            startView.setVisibility(View.VISIBLE);
        }
    }

    //点击注册切换到注册界面
    public void register(View view) {
        startView.setVisibility(View.INVISIBLE);
        registerView.setVisibility(View.VISIBLE);
    }

    //点击开始游戏按钮
    public void startGame(View view) {


        if(TextUtils.isEmpty(id_login.getText()) && TextUtils.isEmpty(pswd_login.getText())){
            Toast.makeText(StartActivity.this,"请输入账号密码",Toast.LENGTH_SHORT).show();
        }
        else{
            User user = new User(id_login.getText().toString(),pswd_login.getText().toString());
            if(userList.contains(user)){
                Toast.makeText(StartActivity.this,"登录成功",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(StartActivity.this,WaitingActivity.class);
                intent.putExtra("isPlayingMusic",isPlayingMusic);
                startActivity(intent);
            }
            else{
                Toast.makeText(StartActivity.this,"账号密码错误",Toast.LENGTH_SHORT).show();
            }
        }

    }

    //确认注册
    public void registerConfirm(View view) {
        Log.i("Click","点击确认");
        Drawable error_input = getDrawable(R.drawable.et_error_input);
        Drawable input = getDrawable(R.drawable.et_input);

        if(!TextUtils.isEmpty(id_rg.getText())&&!TextUtils.isEmpty(pswd_rg.getText())){
            //没有输入确认密码
            if(TextUtils.isEmpty(pswd2_rg.getText())){

                Log.i("Click","没有输入确认密码");
                Toast.makeText(StartActivity.this,"请输入确认密码",Toast.LENGTH_SHORT).show();
                tv_Tip.setVisibility(View.INVISIBLE);
            }
            //两次输入的密码不一致
            else if(!pswd_rg.getText().toString().equals(pswd2_rg.getText().toString())){

                Log.i("Click","两次输入的密码不一致");
                Toast.makeText(StartActivity.this,"两次输入的密码不一致",Toast.LENGTH_SHORT).show();
                tv_Tip.setVisibility(View.INVISIBLE);
            }
            //账号已存在
            else if(idList.contains(id_rg.getText().toString())){

                Log.i("Click","账号已存在");
                Toast.makeText(StartActivity.this,"该账号已存在",Toast.LENGTH_SHORT).show();
                tv_Tip.setVisibility(View.INVISIBLE);
            }
            //成功注册
            else{
                Log.i("Click","成功注册");
                User newUser = new User(id_rg.getText().toString(),pswd_rg.getText().toString());
                userList.add(newUser);
                idList.add(newUser.getId());
                pswdList.add(newUser.getPassword());
                Toast.makeText(StartActivity.this,"注册成功",Toast.LENGTH_SHORT).show();
                startView.setVisibility(View.VISIBLE);
                registerView.setVisibility(View.INVISIBLE);
            }
        }
        else
            Toast.makeText(StartActivity.this,"请输入有效信息",Toast.LENGTH_SHORT).show();

    }

    public void changeMusicState(View view) {
        if(isPlayingMusic){
            Intent intent = new Intent(StartActivity.this,BGMService.class);
            startService(intent);
            view.setBackground(getDrawable(R.mipmap.jingyin));
            isPlayingMusic=false;
        }
        else {
            Intent intent = new Intent(StartActivity.this,BGMService.class);
            startService(intent);
            view.setBackground(getDrawable(R.mipmap.yinyue));
            isPlayingMusic=true;
        }

    }
}