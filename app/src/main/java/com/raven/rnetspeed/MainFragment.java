package com.raven.rnetspeed;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.raven.rnetspeed.service.FloatWindowService;
import com.raven.rnetspeed.util.DensityUtil;


public class MainFragment extends PreferenceFragment implements ServiceConnection {
    private static final String TAG = MainFragment.class.getSimpleName();

    /* sharedpreference 读取数据 */
    private SharedPreferences msp;

    /* service对象，用于service与this的通信 */
    private FloatWindowService floatWindowService;
    private boolean bindSuccess;

    private EditTextPreference editTextPreference;
    private EditText refreshIntervalEdit;

    /* 控件key */
    private static final String WINDOWS_SATATE = "prf_window_state";    /* 悬浮窗开关状态 */
    private static final String MONITOR_STATE = "prf_monitor_state";    /* 监控模式 */
    private static final String REFRESH_INTERVAL = "prf_refresh_interval";  /* 网速显示刷新间隔 */
    private static final String FONT_COLOR = "prf_font_color";      /* 字体颜色 */
    private static final String FONT_SIZE = "prf_font_size";        /* 字体大小 */
//    private static final String NET_STATISCICS = "net_statistics";     /* 流量统计 */
    private static final String NET_SPEED_TEST = "net_speed_test";      /* 网速测试 */
    private static final String ABOUNT = "about";       /* 关于 */


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
                String color = (String) newValue;
                floatWindowService.setTextColor(Color.parseColor(color));
            } else if (FONT_SIZE.equals(key)) {
                floatWindowService.setTextSize((int)newValue);
            }else if(REFRESH_INTERVAL.equals(key)){
                int value = Integer.valueOf((String) newValue);
                if(checkRefreshIntervalLegal(value,true)){
                    floatWindowService.setRefreshInterval(value);
                }
            }
            return true;
        }
    };


    /**
     * 检验新的刷新间隔是否符合要求
     * @param value
     * @param needPrompt 是否需要Toast提示
     * @return
     */
    private boolean checkRefreshIntervalLegal(int value,boolean needPrompt){
        if(value<= 0){
            if(needPrompt){
                Toast.makeText(getActivity(),"间隔不能小于0",Toast.LENGTH_SHORT).show();
            }
            return false;
        }else if(value <= 500){
            if(needPrompt){
                Toast.makeText(getActivity(),"间隔不能低于500ms，间隔过低会增大耗电量哦",Toast.LENGTH_SHORT).show();
            }
            return false;
        }else if(value >= 10*1000){
            if(needPrompt){
                Toast.makeText(getActivity(),"间隔不能大于10s,间隔过大就不准确了呢了呢",Toast.LENGTH_SHORT).show();
            }
            return false;
        }else{
            return  true;
        }
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

    /**
     * 初始化views
     */
    private void initPreferenceViews() {
        /* 状态改变监听 */
        findPreference(WINDOWS_SATATE).setOnPreferenceChangeListener(prfChangeListener);
        findPreference(MONITOR_STATE).setOnPreferenceChangeListener(prfChangeListener);
        findPreference(FONT_COLOR).setOnPreferenceChangeListener(prfChangeListener);
        findPreference(FONT_SIZE).setOnPreferenceChangeListener(prfChangeListener);
        findPreference(REFRESH_INTERVAL).setOnPreferenceChangeListener(prfChangeListener);
        /* 限制只能输入数字 */
        editTextPreference = ((EditTextPreference)findPreference(REFRESH_INTERVAL));
        refreshIntervalEdit = editTextPreference.getEditText();
        refreshIntervalEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
        refreshIntervalEdit.setHint("输入网速刷新间隔(单位ms)");

    }

    @Override
    public void onResume() {
        super.onResume();
        /* 启动流量悬浮窗口 */
        Intent service = new Intent(getActivity(),FloatWindowService.class);
        getActivity().bindService(service,this,Context.BIND_AUTO_CREATE);
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
        String fontColor = msp.getString(FONT_COLOR, "#FFFFFFFF");
        int fontSize = msp.getInt(FONT_SIZE, 5);
        /* 保存的是dp，这里需要转为px */
        fontSize = DensityUtil.dip2px(getActivity(),fontSize);
        String intervalStr = msp.getString(REFRESH_INTERVAL,"1000");
        int value = Integer.valueOf(intervalStr);

        floatWindowService.setWindowVisible(windowOpened);
        floatWindowService.setMonitorState(monitorState);
        floatWindowService.setTextColor(Color.parseColor(fontColor));
        floatWindowService.setTextSize(fontSize);
        if(checkRefreshIntervalLegal(value,false)){
            floatWindowService.setRefreshInterval(value);
            editTextPreference.setText(value+"");
        }else{
            value = 1000;
            floatWindowService.setRefreshInterval(value);
            editTextPreference.setText(value+"");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /* 销毁资源 */
        if(bindSuccess){
            getActivity().unbindService(this);
        }

    }
}
