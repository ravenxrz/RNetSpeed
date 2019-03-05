package com.raven.rnetspeed;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.widget.Toast;

import com.raven.rnetspeed.service.FloatWindowService;


public class MainFragment extends PreferenceFragment implements ServiceConnection {

    /* sharedpreference 读取数据 */
    private SharedPreferences msp;

    /* service对象，用于service与this的通信 */
    private FloatWindowService floatWindowService;
    private boolean bindSuccess;

    /* 控件key */
    private static final String WINDOWS_SATATE = "prf_window_state";    /* 悬浮窗开关状态 */
    private static final String MONITOR_STATE = "prf_monitor_state";    /* 监控模式 */
    private static final String FONT_COLOR = "prf_font_color";      /* 字体颜色 */
    private static final String FONT_SIZE = "prf_font_size";        /* 字体大小 */
    private static final String NET_STATISCICS = "net_statistics";     /* 流量统计 */
    private static final String NET_SPEED_TEST = "net_speed_test";      /* 网速测试 */
    private static final String THANKS = "thanks";      /* 特别鸣谢 */
    private static final String AUTHOR = "about";       /* 关于作者 */


    /* 控件状态改变监听器 */
    private Preference.OnPreferenceChangeListener prfChangeListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String key = preference.getKey();
            if (WINDOWS_SATATE.equals(key)) {
                boolean open = Boolean.valueOf((Boolean) newValue);
                if(open){
                    /* 打开 */
                    floatWindowService.setWindowVisible(true);
                }else{
                    /* 关闭 */
                    floatWindowService.setWindowVisible(false);
                }
            } else if (MONITOR_STATE.equals(key)) {
                floatWindowService.setMonitorState(Integer.valueOf((String)newValue));
            } else if (FONT_COLOR.equals(key)) {

            } else if (FONT_SIZE.equals(key)) {

            }
            return true;
        }
    };

    /* 控件点击事件监听器 */
    private Preference.OnPreferenceClickListener prfClickListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            String key = preference.getKey();
            if (NET_STATISCICS.equals(key)) {
                myToast("流量统计");
            } else if (NET_SPEED_TEST.equals(key)) {
                myToast("网速测试");
            } else if (THANKS.equals(key)) {
                myToast("特别感谢");
            } else if (AUTHOR.equals(key)) {
                myToast("作者信息");
            }
            return true;
        }
    };

    private void myToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* 设置默认值 */
        PreferenceManager.setDefaultValues(getActivity(), R.xml.main_preferences, false);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.main_preferences);

        /* 初始化view */
        initPreferenceViews();

        /* 初始话msp */
        msp = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        /* 启动流量悬浮窗口 */
        Intent service = new Intent(getActivity(),FloatWindowService.class);
        getActivity().bindService(service,this,Context.BIND_AUTO_CREATE);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /* 销毁资源 */
        if(bindSuccess){
            getActivity().unbindService(this);
        }

    }

    /**
     * 初始化views
     */
    private void initPreferenceViews() {
        /* 状态改变监听 */
        findPreference(WINDOWS_SATATE).setOnPreferenceChangeListener(prfChangeListener);
        findPreference(MONITOR_STATE).setOnPreferenceChangeListener(prfChangeListener);
        findPreference(FONT_COLOR).setOnPreferenceChangeListener(prfChangeListener);
        findPreference(FONT_SIZE).setOnPreferenceChangeListener(prfChangeListener);
        /* 控件点击监听 */
        findPreference(NET_STATISCICS).setOnPreferenceClickListener(prfClickListener);
        findPreference(NET_SPEED_TEST).setOnPreferenceClickListener(prfClickListener);
        findPreference(THANKS).setOnPreferenceClickListener(prfClickListener);
        findPreference(AUTHOR).setOnPreferenceClickListener(prfClickListener);
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        floatWindowService = ((FloatWindowService.MyBinder)service).getService();
        if(null != floatWindowService){
            Log.i(MainFragment.class.getSimpleName(),"service绑定成功");
            bindSuccess = true;
            /* bind成功后，初始化floatWindow的显示 */
            initFloatWindowDisplay();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    private void initFloatWindowDisplay(){
        /* 所有view操作初始完成后，开始做数据恢复操作 */
        boolean windowOpened = msp.getBoolean(WINDOWS_SATATE, false);
        /* 1 代表仅下行 */
        int monitorState = Integer.valueOf(msp.getString(MONITOR_STATE, "1"));
        int fontColor = msp.getInt(FONT_COLOR, 0x000fff);
        int fontSize = msp.getInt(FONT_SIZE, 100);

        floatWindowService.setWindowVisible(windowOpened);
        floatWindowService.setMonitorState(monitorState);
    }
}
