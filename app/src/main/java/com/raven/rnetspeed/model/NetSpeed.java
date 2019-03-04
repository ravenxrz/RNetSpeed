package com.raven.rnetspeed.model;

import android.net.TrafficStats;

public class NetSpeed {
    private static final String TAG = NetSpeed.class.getSimpleName();
    private long lastTotalRxBytes = 0;
    private long lastTotalTxByes = 0;
    private long lastTimeStamp = 0;

    public long[] getNetSpeed(int uid) {
        long nowTotalRxBytes = getTotalRxBytes(uid);
        long nowTotalTxBytes = getTotalTxBytes(uid);
        long nowTimeStamp = System.currentTimeMillis();
        long downloadSpeed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));//毫秒转换
        long uploadSpeed = ((nowTotalTxBytes - lastTotalTxByes) * 1000 / (nowTimeStamp - lastTimeStamp));
        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;
        lastTotalTxByes = nowTotalTxBytes;
        return new long[]{uploadSpeed,downloadSpeed};
    }



    //getApplicationInfo().uid
    private long getTotalRxBytes(int uid) {
        return TrafficStats.getUidRxBytes(uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);//转为KB
    }
    private long getTotalTxBytes(int uid){
        return TrafficStats.getUidTxBytes(uid) == TrafficStats.UNSUPPORTED ? 0: (TrafficStats.getTotalTxBytes() / 1024);    //转换为KB
    }
}
