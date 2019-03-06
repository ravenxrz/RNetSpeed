package com.raven.rnetspeed;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.text.Html;
import android.widget.Toast;


public class MainActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        /* 设置actionBar标题颜色 */
        android.app.ActionBar actionBar = getActionBar();
        if(actionBar != null)
        {
            getActionBar().setTitle(Html.fromHtml("<font color='#ffffff'>RNetSpeed </font>"));

        }
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new MainFragment())
                .commit();

        applyForFloatWindowsPermission();

    }
    private void applyForFloatWindowsPermission(){
        try{
            //判断当前系统版本
            if(Build.VERSION.SDK_INT>=23) {
                //判断权限是否已经申请过了（加上这个判断，则使用的悬浮窗的时候；如果权限已经申请则不再跳转到权限开启界面）
                if (!Settings.canDrawOverlays(this)){
                    //申请权限
                    Intent intent =new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, 1);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(Build.VERSION.SDK_INT>=23) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(MainActivity.this, "权限授予失败，无法开启悬浮窗,本程序自动结束", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "权限授予成功！", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
