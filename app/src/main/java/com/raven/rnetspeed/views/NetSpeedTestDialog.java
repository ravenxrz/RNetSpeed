package com.raven.rnetspeed.views;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.raven.rnetspeed.R;

import java.math.BigDecimal;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;

public class NetSpeedTestDialog extends DialogPreference {


    private static final String TAG = NetSpeedTestDialog.class.getSimpleName();

    /* 显示速度相关 */
    TextView curSpeedTv;
    TextView maxSpeedTv;
    TextView avegeSpeedTv;
    static final int CURRENT = 1;
    static final int MAX = 2;
    static final int AVERAGE = 3;
    Button speedBt;
    Button exitBt;

    LineChart chart;

    Context mContext;




    public NetSpeedTestDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        /* init view */
        initViews(view);
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
        chart = view.findViewById(R.id.speed_show);
        /* 设置bt的监听 */
        exitBt.setOnClickListener(exitListener);
        speedBt.setOnClickListener(speedBtListener);

        initChart();

    }

    private void initChart(){

        // enable description text
        chart.getDescription().setEnabled(true);
        Description description = new Description();
        description.setText("单位:KB/s");
        chart.setDescription(description);

        // enable touch gestures
        chart.setTouchEnabled(false);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true);

        // set an alternative background color
        chart.setBackgroundColor(Color.WHITE);

        LineData data = new LineData();

        // add empty data
        chart.setData(data);
//        addEntry(0);
//        // get the legend (only possible after setting data)
//        Legend l = chart.getLegend();

//
//
        XAxis xl = chart.getXAxis();
//        xl.setTypeface(tfLight);
//        xl.setTextColor(Color.WHITE);
        xl.setAxisMinimum(0);
        xl.setAxisMaximum(9);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
//        leftAxis.setTypeface(tfLight);
//        leftAxis.setTextColor(Color.WHITE);
//        leftAxis.set
        leftAxis.setAxisMaximum(1000);
//        leftAxis.calculate(0,1000);
        leftAxis.setAxisMinimum(0);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
    }



    private void addEntry(float y) {

        LineData data = chart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), y), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            chart.notifyDataSetChanged();

            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(120);
            // chart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            chart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // chart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "实时网速");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(ColorTemplate.getHoloBlue());
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
//        set.setValueTextColor(Color.BLACK);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }



    View.OnClickListener speedBtListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if(speedBt.isClickable()){
                /* 测试前，清空一些views */
                avegeSpeedTv.setText("0.0KB/s");
                maxSpeedTv.setText("0.0KB/s");
                curSpeedTv.setText("0.0KB/s");
                speedBt.setClickable(false);
                /* 重新初始化Chart Y轴 */
                chart.clearValues();
                chart.getAxisLeft().setAxisMaximum(1000);
                axisReseted = false;

                addEntry(0);
                speedBt.setBackground(mContext.getDrawable(R.drawable.shape_grey_bg));
                /* 开始测速 */
                new DownloadSpeedTestTask().execute();

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

            }
        }
    };


    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        this.getDialog().setCanceledOnTouchOutside(false); /* 点击外widnow不关闭 */
        this.getDialog().setCancelable(false);  /* 物理按键不可关闭 */
    }

    boolean axisReseted = false;
      Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BigDecimal speed = (BigDecimal) msg.obj;
            switch(msg.what){
                case CURRENT:
                    curSpeedTv.setText(formatSpeed(speed.floatValue()));
                    if(!axisReseted && speed.floatValue() > 1000){
                        axisReseted = true;
                        chart.getAxisLeft().resetAxisMaximum();
                    }
                    addEntry(speed.floatValue());
                    break;
                case MAX:
                    maxSpeedTv.setText(formatSpeed(speed.floatValue()));
                    break;
                case AVERAGE:
                    /* 开启可点击 */
                    speedBt.setClickable(true);
                    speedBt.setBackground(mContext.getDrawable(R.drawable.selector_bt_bg));
                    avegeSpeedTv.setText(formatSpeed(speed.floatValue()));
                    break;
                default:
                    Toast.makeText(mContext,"测速失败，请检查网络",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private String formatSpeed(float kbValue){
        if(kbValue > 1024){
            return String.format("%.1f",kbValue/1024) + "MB/s";
        }else{
            return String.format("%.1f",kbValue) + "KB/s";
        }
    }

    /**
     * 判定是否能够退出dialog
     * @return
     */
    private boolean canDismissDialog(){
        return speedBt.isClickable();
    }


    private class DownloadSpeedTestTask extends AsyncTask<Void,Void,String>{

        final BigDecimal base = new BigDecimal(1024);
        BigDecimal nettraffic ;
        BigDecimal currentTraffic;
        final int TOTALTIME = 8*1000;    /* 10s */
        final int INTERVAL = 1000;        /* report interval */
        float maxSpeed = 0.0f;

        @Override
        protected String doInBackground(Void... params) {
            nettraffic = new BigDecimal(0.0);


            SpeedTestSocket speedTestSocket = new SpeedTestSocket();

            speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {
                @Override
                public void onCompletion(SpeedTestReport report) {

                    /* 设置平均 */
                    BigDecimal avergaeSpeed = nettraffic.divide(new BigDecimal(TOTALTIME/INTERVAL));
                    handler.sendMessage(Message.obtain(handler,AVERAGE,avergaeSpeed));
                }

                @Override
                public void onProgress(float percent, SpeedTestReport report) {
                    currentTraffic = report.getTransferRateOctet().divide(base);
                    nettraffic = nettraffic.add(currentTraffic);

                    handler.sendMessage(Message.obtain(handler,CURRENT,currentTraffic));
                    if(maxSpeed < currentTraffic.floatValue()){
                        maxSpeed = currentTraffic.floatValue();
                        handler.sendMessage(Message.obtain(handler,MAX,currentTraffic));
                    }
                }

                @Override
                public void onError(SpeedTestError speedTestError, String errorMessage) {
                    /* 开启可点击 */
                    speedBt.setClickable(true);
                    speedBt.setBackground(mContext.getDrawable(R.drawable.selector_bt_bg));
                    Log.i(TAG,"error"+errorMessage);
                    handler.sendEmptyMessage(0);
                }
            });

            speedTestSocket.startFixedDownload("http://ftp.sjtu.edu.cn/ubuntu-cd/18.04.2/ubuntu-18.04.2-desktop-amd64.iso", TOTALTIME,INTERVAL);
            return null;
        }


    }
}
