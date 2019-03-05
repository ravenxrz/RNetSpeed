package com.raven.rnetspeed;

import android.preference.PreferenceActivity;
import android.os.Bundle;
import android.text.Html;


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


    }
}
