package com.raven.rnetspeed.views;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

/**
 * 自定义字体颜色选择弹窗
 */
public class FontColorChooseDialog extends DialogPreference {
    public FontColorChooseDialog(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public FontColorChooseDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FontColorChooseDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FontColorChooseDialog(Context context) {
        super(context);
    }
}
