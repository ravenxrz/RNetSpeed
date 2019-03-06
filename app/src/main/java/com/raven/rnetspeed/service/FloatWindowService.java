package com.raven.rnetspeed.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.raven.rnetspeed.R;
import com.raven.rnetspeed.model.NetSpeed;


public class FloatWindowService extends Service {

    private static final String TAG = FloatWindowService.class.getSimpleName();

    /* 上行下行控件 */
    TextView download;
    TextView upload;
    View view;  /* 总 view */

    /* 一些辅助变量 */
    int save_interval = 1000 * 10;    /* 十分钟存储一次 */
    int refresh_interval = 2000;     /* 1s更新ui一次 */
    WindowManager wm;       /* 窗体管理器 */
    WindowManager.LayoutParams wlp;     /* 窗体参数 */
//    String dev = null;      /* 当前上网设备 wifi?mobile */
    Handler taskHandler = new Handler();        /* 网速显示handler */
    NetSpeed mNetSpeed = new NetSpeed();
//    int uid = -1;
    long[] currentSpeed;
//    int view_half_width,view_half_height;
    NetworkInfo mWifi;
    NetworkInfo mMobile;
    /* 记录最后移动坐标的sp */
    SharedPreferences coodinatorSp;
    int finalX,finalY;
    ConnectivityManager connManager;

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    /**
     * 用于Service和Activity通信的辅助类
     */
    public class MyBinder extends android.os.Binder {
        public FloatWindowService getService(){
            return FloatWindowService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        uid = getApplicationInfo().uid;     /* 当前进程id */
        /* 注册上网状态广播 */
        /* 注册监听网络状态变化的广播 */
//        IntentFilter mNetFilter = new IntentFilter();
//        mNetFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
//        mNetFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
//        registerReceiver(devChangeReceiver, mNetFilter);
        /* init sp */
        coodinatorSp = getSharedPreferences("final coordinator",MODE_PRIVATE);
        /* 初始views*/
        initViews();
        /* 启动显示任务 */
        taskHandler.post(task);

        connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

    }

    /**
     * 初始化views
     */
    private void initViews() {
        view = LayoutInflater.from(this).inflate(R.layout.floating_windows, null);
        download = view.findViewById(R.id.download);
        upload = view.findViewById(R.id.upload);
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
        /* 获取x，y的最终坐标 */
        String coord = coodinatorSp.getString("coord","0,0");
        Log.i(TAG,coord);
        int x = Integer.parseInt(coord.split(",")[0]);
        int y = Integer.parseInt(coord.split(",")[1]);
        wlp.x = x;
        wlp.y = y;

        wlp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        /* 设置拖动监听 */
        view.setOnTouchListener(touchListener);

        /* 显示 */
        wm.addView(view,wlp);
    }


    /**
     * 实时显示网速任务
     */
    Runnable task = new Runnable() {
        @Override
        public void run() {
            mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            mMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if(mWifi.isConnected()){
//                Log.i(TAG,"wifi connected");
                currentSpeed = mNetSpeed.getWifiNetSpeed();
            }else if(mMobile.isConnected()){
                currentSpeed = mNetSpeed.getMobileNetSpeed();
//                Log.i(TAG,"mobile connected");
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
//    private BroadcastReceiver devChangeReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//        }
//    };


    /**
     * 更新view位置
     *
     * @param x
     * @param y
     */
    private void updateViewPos(float x, float y) {
        finalX = (int) x;
        finalY = (int) y;
        wlp.x = finalX- view.getMeasuredWidth()/2;
        wlp.y = finalY - view.getMeasuredHeight()/2;
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
    public void setMonitorState(int state) {
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

    public void setTextColor(int color){
        upload.setTextColor(color );
        download.setTextColor(color);
    }

    public void setTextSize(float size){
        upload.setTextSize(size);
        download.setTextSize(size);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        /* 撤销相关资源 */
        wm.removeViewImmediate(view);
        taskHandler.removeCallbacks(task);
        /* 写入窗口的最后坐标 */
        SharedPreferences.Editor editor = coodinatorSp.edit();
        editor.putString("coord",finalX+","+finalY);
        editor.commit();
//        unregisterReceiver(devChangeReceiver);
    }


}
