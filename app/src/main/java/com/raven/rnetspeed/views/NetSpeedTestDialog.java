package com.raven.rnetspeed.views;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.raven.rnetspeed.R;

import java.math.BigDecimal;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.IRepeatListener;

public class NetSpeedTestDialog extends DialogPreference {


    private static final String TAG = NetSpeedTestDialog.class.getSimpleName();

    TextView curSpeedTv;
    TextView maxSpeedTv;
    TextView avegeSpeedTv;
    Button speedBt;
    Button exitBt;
//    final String START_TEST = "开始测速";
    Context mContext;
    DownloadSpeedTestTask downloadSpeedTestTask;


    public NetSpeedTestDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        /* init view */
        initViews(view);
        /* new测速线程 */
        downloadSpeedTestTask = new DownloadSpeedTestTask();
    }

    /**
     * 初始化views
     * @param view
     */
    private void initViews(View view) {
        curSpeedTv = view.findViewById(R.id.current_speed_tv);
        maxSpeedTv = view.findViewById(R.id.max_speed_tv);
        avegeSpeedTv = view.findViewById(R.id.averge_speed_tv);
        speedBt = view.findViewById(R.id.speed_bt);
        exitBt = view.findViewById(R.id.exit_bt);
        /* 设置bt的监听 */
        exitBt.setOnClickListener(exitListener);

        speedBt.setOnClickListener(speedBtListener);
    }

    View.OnClickListener speedBtListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if(speedBt.isClickable()){
                /* 开始测速 */
                speedBt.setClickable(false);
                speedBt.setBackground(mContext.getDrawable(R.drawable.shape_grey_bg));
//                downloadSpeedTestTask.execute();
            }
        }
    };
    View.OnClickListener exitListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(canDismissDialog()){
                NetSpeedTestDialog.this.getDialog().dismiss();
            }else{
                Toast.makeText(mContext,"请等待测速完成",Toast.LENGTH_SHORT).show();
                /* 一下代码应该在完成测速后更改 */
                speedBt.setClickable(true);
                speedBt.setBackground(mContext.getDrawable(R.drawable.selector_bt_bg));
            }
        }
    };


    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        this.getDialog().setCanceledOnTouchOutside(false); /* 点击外widnow不关闭 */
        this.getDialog().setCancelable(false);  /* 物理按键不可关闭 */
    }





    /**
     * 判定是否能够退出dialog
     * @return
     */
    private boolean canDismissDialog(){
        return speedBt.isClickable();
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        /* 销毁资源 */
        if(!downloadSpeedTestTask.isCancelled()){
            downloadSpeedTestTask.cancel(true);
        }
    }

    private class DownloadSpeedTestTask extends AsyncTask<Void,Void,String>{

        final BigDecimal base = new BigDecimal(1024);
        BigDecimal nettraffic ;
        BigDecimal currentTraffic;
        final int TOTALTIME = 20*1000;    /* 10s */
        final int INTERVAL = 1000;        /* report interval */

        @Override
        protected String doInBackground(Void... params) {
            nettraffic = new BigDecimal(0.0);

            SpeedTestSocket speedTestSocket = new SpeedTestSocket();

            speedTestSocket.startDownloadRepeat("http://ftp.sjtu.edu.cn/ubuntu-cd/18.04.2/ubuntu-18.04.2-desktop-amd64.iso", TOTALTIME, INTERVAL,
                    new IRepeatListener() {
                        @Override
                        public void onCompletion(SpeedTestReport report) {
                            Log.v("speedtest average", "[COMPLETED] rate in KB/s : " + nettraffic.divide(new BigDecimal(TOTALTIME/INTERVAL)));
                            /* 测速完成，开启关闭按钮 */
                            speedBt.setClickable(true);
                        }

                        @Override
                        public void onReport(SpeedTestReport report) {
                            currentTraffic = report.getTransferRateOctet().divide(base);
                            nettraffic = nettraffic.add(currentTraffic);
                            Log.v("speedtest current", "[REPORT] rate in Byte/s : " +currentTraffic );
                        }
                    });
            return null;
        }
    }
}
