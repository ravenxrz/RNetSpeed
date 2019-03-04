package com.raven.rnetspeed.views;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class AboutInfoDialog extends DialogPreference {
    public AboutInfoDialog(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public AboutInfoDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AboutInfoDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AboutInfoDialog(Context context) {
        super(context);
    }
}
