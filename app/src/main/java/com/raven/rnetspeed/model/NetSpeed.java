package com.raven.rnetspeed.model;

import android.net.TrafficStats;

public class NetSpeed {
    private static final String TAG = NetSpeed.class.getSimpleName();
    private long lastTotalWiFiRxBytes = 0;
    private long lastTotalWiFiTxByes = 0;
    private long lastTotalMobileRxBytes = 0;
    private long lastTotalMobileTxBytes = 0;
    private long lastTimeWiFiStamp = 0;
    private long lastTimeMobileStamp = 0;

    public long[] getWifiNetSpeed() {
        long nowTotalRxBytes = getTotalWiFiRxBytes();
        long nowTotalTxBytes = getTotalWiFiTxBytes();
        long nowTimeStamp = System.currentTimeMillis();
        long downloadSpeed = ((nowTotalRxBytes - lastTotalWiFiRxBytes) * 1000 / (nowTimeStamp - lastTimeWiFiStamp));//毫秒转换
        long uploadSpeed = ((nowTotalTxBytes - lastTotalWiFiTxByes) * 1000 / (nowTimeStamp - lastTimeWiFiStamp));
        lastTimeWiFiStamp = nowTimeStamp;
        lastTotalWiFiRxBytes = nowTotalRxBytes;
        lastTotalWiFiTxByes = nowTotalTxBytes;
        return new long[]{uploadSpeed,downloadSpeed};
    }

    //getApplicationInfo().uid
    private long getTotalWiFiRxBytes() {
        return TrafficStats.getTotalRxBytes() == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);//转为KB
    }
    private long getTotalWiFiTxBytes(){
        return TrafficStats.getTotalTxBytes() == TrafficStats.UNSUPPORTED ? 0: (TrafficStats.getTotalTxBytes() / 1024);    //转换为KB
    }

    public long[] getMobileNetSpeed(){
        long nowTotalRxBytes = getTotalMobileRxBytes();
        long nowTotalTxBytes = getTotalMobileTxBytes();
        long nowTimeStamp = System.currentTimeMillis();
        long deltaTime = nowTimeStamp - lastTimeMobileStamp;
        long downloadSpeed = ((nowTotalRxBytes - lastTotalMobileRxBytes) * 1000 / (nowTimeStamp - lastTimeMobileStamp));//毫秒转换
        long uploadSpeed = ((nowTotalTxBytes - lastTotalMobileTxBytes) * 1000 / (nowTimeStamp - lastTimeMobileStamp));
        lastTimeMobileStamp = nowTimeStamp;
        lastTotalMobileTxBytes = nowTotalTxBytes;
        lastTotalMobileRxBytes = nowTotalRxBytes;
        return new long[]{uploadSpeed,downloadSpeed};
    }

    private long getTotalMobileRxBytes(){
        return TrafficStats.getMobileRxBytes() == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getMobileRxBytes() / 1024);//转为KB
    }
    private long getTotalMobileTxBytes(){
        return TrafficStats.getMobileTxBytes() == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getMobileTxBytes() / 1024);//转为KB
    }


}
