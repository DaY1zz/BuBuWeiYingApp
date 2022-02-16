package com.example.androiddemo;

import static android.content.ContentValues.TAG;
import static java.lang.Math.abs;
import static java.lang.Math.pow;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.androiddemo.util.LoadingDialog;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public class WaitingActivity extends AppCompatActivity {

    private static final int CONNECT_SUCCESS = 2048;
    private static final int CONNECT_FAIL = 500;
    private static final int NOT_CONNECT = 9192;
    private static final int GET_DATA = 128;
    private static final int GET_RSSI = 201;
    private static final int RSSI_WEAK = 202;
    private static final int RSSI_STRONG = 203;
    private static final String RECEIVE_CODE = "receiveCode";

    private BluetoothAdapter mBluetoothAdapter;
    private ConnectThread mConnectThread = null;
    public CommunicationThread communicationThread;
    public static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private List<BluetoothDevice> mBluetoothDevices = new ArrayList<>();
    public DataApplication dataApp;
    private RelativeLayout rlBtset;
    private String mACAddr = null;     // MAC地址
    private boolean isFront = false; //当前本窗体是否在前台显示
    private ArrayAdapter<String> mConnectedAdapter;
    private ListView mLvConnectedDevices;

    private boolean startFlag = true;
    private int currentPoint = 0;
    private int totalPoint = 0;

    private TextView tv_currentPoint;
    private TextView tv_totalPoint;
    private Button btn_voice;
    private ImageView bt_img;

    private boolean isPlayingMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);
        dataApp = (DataApplication) getApplication();

        checkBTPermission();

        tv_currentPoint = findViewById(R.id.tv_currentPoint);
        tv_totalPoint = findViewById(R.id.tv_totalPoint );
        btn_voice = findViewById(R.id.btnVoice2);
        bt_img = findViewById(R.id.bt_img);

        Intent intent = getIntent();
        isPlayingMusic = intent.getBooleanExtra("isPlayingMusic",true);

        //判断是否继续播放音乐
        if(!isPlayingMusic){
            btn_voice.setBackground(getDrawable(R.mipmap.jingyin));
        }

        tv_currentPoint.setText(String.valueOf(currentPoint));
        tv_totalPoint.setText(String.valueOf(totalPoint));

        initView();
        System.out.println("------dataApp.getBTMAC() " + dataApp.getBTMAC());
        if(dataApp.getBTMAC() != null){
            mACAddr = dataApp.getBTMAC();
            BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(mACAddr);
            connect(bluetoothDevice);
        }else{
            showRlBtset(null);
        }
    }
    //动态获取权限
    private void checkBTPermission() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
                permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
                if(permissionCheck != 0){
                    this.requestPermissions(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, 1001); //any number
                }else{
                    Log.d(TAG,
                            "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
                }
            }
        }
        Log.d(TAG, "checkBTPermission: Finish");
    }

    private void initView() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "蓝牙未开启", Toast.LENGTH_LONG).show();
            finish();
        }

        rlBtset = findViewById(R.id.rl_btset);
        mLvConnectedDevices = findViewById(R.id.lv_connected_devices);
        mConnectedAdapter = new ArrayAdapter<String>(this, R.layout.btitem);
        mLvConnectedDevices.setAdapter(mConnectedAdapter);
        mLvConnectedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothDevice bluetoothDevice = mBluetoothDevices.get(i);
                if (bluetoothDevice != null) {
                    mACAddr = bluetoothDevice.getAddress();
                    connect(bluetoothDevice);
                }
            }
        });

        //定时获取蓝牙信号强度
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);
        handler.sendEmptyMessageDelayed(GET_RSSI, 0);
    }
    @Override
    public void onResume() {
        super.onResume();
        checkAlreadyConnect();
        isFront = true;
    }
    @Override
    public void onPause() {
        super.onPause();
        isFront = false;
    }
    @Override
    protected void onDestroy() {
        if (communicationThread != null) {
            communicationThread.interrupt();
            communicationThread.cancel();
            communicationThread = null;
        }
        if (mConnectThread != null) {
            mConnectThread.interrupt();
            mConnectThread.cancel();
            mConnectThread = null;
        }
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    int i=0;
    public void rollDice(View view) {
        AnimationDrawable dice = (AnimationDrawable) view.getBackground();
        if(startFlag){
            dice.start();
            startFlag = false;
        }
        else{
            dice.stop();
            startFlag = true;
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

            if(BluetoothProfile.STATE_CONNECTED == adapter.getProfileConnectionState(BluetoothProfile.HEADSET))
            {
                i=i+1;

            }
            if(i==0){
                Toast.makeText(WaitingActivity.this, "请插入游戏卡片", Toast.LENGTH_SHORT).show();//toast信息
            }
            if(i==1){
                answer1();
            }
            if(i==2){
                answer2();
                i=0;
            }
        }

    }

    public void answer1() {
        Intent intent = new Intent(this,AnswerActivity.class);
        intent.putExtra("Video",1);
        startActivityForResult(intent,0);
        Intent musicIntent = new Intent(this,BGMService.class);
        stopService(musicIntent);
    }

    public void answer2() {
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
        this.finish();
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

    //刷新已配对的蓝牙设备
    private void checkAlreadyConnect() {
        mConnectedAdapter.clear();
        mBluetoothDevices.clear();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mConnectedAdapter.add(device.getName() + "\t" + device.getAddress());
                mBluetoothDevices.add(device);
            }
        }
        mConnectedAdapter.notifyDataSetChanged();
    }
    //打开设置窗口
    public void showRlBtset(View view){
        checkAlreadyConnect();
        rlBtset.setVisibility(View.VISIBLE);
    }
    //关闭设置窗口
    public void hideRlBtset(View view){
        rlBtset.setVisibility(View.GONE);
    }
    //调用系统蓝牙设置页面
    public void connectAstt(View view) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    //连接蓝牙设备
    public void connect(BluetoothDevice device) {
        LoadingDialog.createLoadingDialog(WaitingActivity.this,"正在连接");
        if(mConnectThread != null){
            mConnectThread.cancel();
        }
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }

    //开始接收蓝牙信息
    private void startCommunicationThread() {
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    if (communicationThread != null) {
                        communicationThread.start();
                        break;
                    }
                }
            }
        }.start();
    }
    //接收蓝牙信息的进程
    private class CommunicationThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public CommunicationThread(BluetoothSocket socket) {
            Log.e(" ConnectedThread","已连接");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            dataApp.setBTMAC(mACAddr);
        }

        public void run() {
            if (Thread.interrupted()) {
                return;
            }
            Log.e("ConnectedThread","等待信息");
            byte[] buffer = new byte[4];
            int bytes;
            while (true) {
                synchronized (this) {
                    try {
                        // Read from the InputStream
                        bytes = mmInStream.read(buffer);
                        String reStr = new String(buffer);
                        System.out.println("------- buffer " + reStr);
                        Log.e("ConnectedThread",bytes + "bytes");

                        Message msg = new Message();
                        msg.what = GET_DATA;
                        Bundle bundle = new Bundle();
                        bundle.putString(RECEIVE_CODE, reStr);
                        msg.setData(bundle);
                        handler.sendMessage(msg);

                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    //连接蓝牙进程
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
        }
        public void run() {
            if (Thread.interrupted())
                return;
            setName("ConnectThread");
            mBluetoothAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
                dataApp.setBTMAC(mACAddr);
                Message msg = new Message();
                msg.what = CONNECT_SUCCESS;
                Bundle bundle = new Bundle();
                msg.setData(bundle);
                handler.sendMessage(msg);
            } catch (IOException e) {
                e.printStackTrace();
                handler.sendEmptyMessage(NOT_CONNECT);
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                Message msg = new Message();
                msg.what = CONNECT_FAIL;
                Bundle bundle = new Bundle();
                msg.setData(bundle);
                handler.sendMessage(msg);
                return;
            }
            communicationThread = new CommunicationThread(mmSocket);
            startCommunicationThread();
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mConnectThread = null;
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONNECT_FAIL:
                    LoadingDialog.closeDialog();
                    Toast.makeText(WaitingActivity.this, "蓝牙连接失败", Toast.LENGTH_LONG).show();
                    break;
                case CONNECT_SUCCESS:
                    bt_img.setBackgroundColor(Color.GREEN);
                    LoadingDialog.closeDialog();
                    hideRlBtset(null);
                    break;
                case GET_DATA:
                    if(isFront){
                        String REGEX = "[^(0-9)]";
                        String reStr = msg.getData().getString(RECEIVE_CODE);
                        String ticket = Pattern.compile(REGEX).matcher(reStr).replaceAll("").trim();
                        System.out.println("------- ticket " + ticket);
                        if(ticket.equals("1")){
                            answer1();
                        }else if(ticket.equals("4")){
                            answer2();
                        }else if(ticket.equals("85")){
                            victory(null);
                        }
                    }
                    break;
                case GET_RSSI:
                    if (mBluetoothAdapter.isDiscovering()) {
                        mBluetoothAdapter.cancelDiscovery();
                    }
                    mBluetoothAdapter.startDiscovery();
                    break;
                case RSSI_WEAK:
                    bt_img.setImageResource(R.mipmap.bs0);
                    break;
                case RSSI_STRONG:
                    bt_img.setImageResource(R.mipmap.bs1);
                    break;
            }
        }
    };
    //蓝牙信号强度回调
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 判断是当前设备
                if (device.getBondState() == BluetoothDevice.BOND_BONDED && device.getAddress().equals(mACAddr)) {
                    short rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
                    System.out.println("信号强度:"+rssi);
                    int iRssi = abs(rssi);
                    if(iRssi > 80){
                        handler.sendEmptyMessageDelayed(RSSI_WEAK, 0);
                    }else{
                        handler.sendEmptyMessageDelayed(RSSI_STRONG, 0);
                    }
                    // 将蓝牙信号强度换算为距离
                    double power = (iRssi - 59) / 25.0;
                    String mm = new Formatter().format("%.2f", pow(10, power)).toString();
                    System.out.println(device.getName() + ":" + device.getAddress() + " 距离：" + mm + "m");
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                handler.sendEmptyMessageDelayed(GET_RSSI, 2000);
            }
        }
    };
}