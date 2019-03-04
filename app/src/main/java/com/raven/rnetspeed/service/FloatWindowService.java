package com.raven.rnetspeed.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.raven.rnetspeed.R;
import com.raven.rnetspeed.model.NetSpeed;


public class FloatWindowService extends Service {

    /* 上行下行控件 */
    TextView download;
    TextView upload;
    View view;  /* 总 view */

    /* 一些辅助变量 */
    int save_interval = 1000 * 10;    /* 十分钟存储一次 */
    int refresh_interval = 2000;     /* 1s更新ui一次 */
    WindowManager wm;       /* 窗体管理器 */
    WindowManager.LayoutParams wlp;     /* 窗体参数 */
    String dev = null;      /* 当前上网设备 wifi?mobile */
    Handler taskHandler = new Handler();        /* 网速显示handler */
    NetSpeed mNetSpeed = new NetSpeed();
//    int uid = -1;
    long[] currentSpeed;
//    int view_half_width,view_half_height;
    NetworkInfo mWifi;
    NetworkInfo mMobile;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        uid = getApplicationInfo().uid;     /* 当前进程id */
        /* 注册上网状态广播 */
        /* 注册监听网络状态变化的广播 */
        IntentFilter mNetFilter = new IntentFilter();
        mNetFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        mNetFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(devChangeReceiver, mNetFilter);
        /* 初始views*/
        initViews();
        /* 启动显示任务 */
        taskHandler.post(task);

        ConnectivityManager connManager;
        connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        mMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    }

    /**
     * 初始化views
     */
    private void initViews() {
        view = LayoutInflater.from(this).inflate(R.layout.floating_windows, null);
        download = view.findViewById(R.id.download);
        upload = view.findViewById(R.id.upload);
        LinearLayout linearLayout = view.findViewById(R.id.text_wrapper);
        wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        wlp = new WindowManager.LayoutParams();
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.N){
            wlp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else{
            wlp.type = WindowManager.LayoutParams.TYPE_TOAST;
        }
        wlp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wlp.gravity = Gravity.START | Gravity.TOP;
        wlp.format = PixelFormat.TRANSLUCENT;
        wlp.x = 300;
        wlp.y = 300;

        wlp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        /* 设置拖动监听 */
        view.setOnTouchListener(touchListener);

        /* 显示 */
        wm.addView(view, wlp); /* 显示完成 */
        taskHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i("Service","关闭");
                wm.removeViewImmediate(view);
            }
        },5000);
    }


    /**
     * 实时显示网速任务
     */
    Runnable task = new Runnable() {
        @Override
        public void run() {
            if(mWifi.isConnected()){
                currentSpeed = mNetSpeed.getWifiNetSpeed();
            }else if(mMobile.isConnected()){
                currentSpeed = mNetSpeed.getMobileNetSpeed();
            }else{
                currentSpeed = new long[]{0,0};
            }
            updateViewSpeed(currentSpeed[0], currentSpeed[1]);
            taskHandler.postDelayed(task, refresh_interval);     /* 又加入消息队列，这样可以反复处理这个任务 */
        }
    };

    /**
     * 拖动处理监听器
     */
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    updateViewPos(event.getRawX(), event.getRawY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    updateViewPos(event.getRawX(), event.getRawY());
                    break;
                case MotionEvent.ACTION_UP:
                    updateViewPos(event.getRawX(), event.getRawY());
                    break;
            }
            return true;
        }
    };

    /**
     * 网卡设备更改监听
     */
    private BroadcastReceiver devChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };


    /**
     * 更新view位置
     *
     * @param x
     * @param y
     */
    private void updateViewPos(float x, float y) {
        wlp.x = (int) x - view.getMeasuredWidth()/2;
        wlp.y = (int) y - view.getMeasuredHeight()/2;
        wm.updateViewLayout(view, wlp);
    }


    /**
     * 展示网速
     *
     * @param uploadSpeed   上传速度，kb
     * @param downloadSpeed 下载速度 kb
     */
    private void updateViewSpeed(float uploadSpeed, float downloadSpeed) {
        if (uploadSpeed > 1000) {
            upload.setText("↑"+String.format("%.1f",uploadSpeed / 1024) + " MB/s");
        } else {
            upload.setText("↑"+String.format("%.1f",uploadSpeed) + " KB/s");
        }
        if (downloadSpeed > 1000) {
            download.setText("↓"+String.format("%.1f",downloadSpeed / 1024)  + " MB/s");
        } else {
            download.setText("↓"+String.format("%.1f",downloadSpeed) + " KB/s");
        }
    }

    /**
     * 设置监听模式
     *
     * @param state 1： 仅下行， 2: 上下行
     */
    private void setMonitorState(int state) {
        if (1 == state) {
            upload.setVisibility(View.GONE);
        } else if (2 == state) {
            upload.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 存储从上次存储时间点到本次时间点的流量数据
     *
     * @param dev        设备，网卡（wifi，mobile)
     * @param netTraffic 网络流量总量
     */
    private void saveData(String dev, float netTraffic) {

    }

    public void setWindowVisible(boolean visible) {
        if (visible) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /* 撤销相关资源 */
        wm.removeViewImmediate(view);
        taskHandler.removeCallbacks(task);
        unregisterReceiver(devChangeReceiver);
    }
}
