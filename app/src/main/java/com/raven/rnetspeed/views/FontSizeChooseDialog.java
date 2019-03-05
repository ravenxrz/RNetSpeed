package com.raven.rnetspeed.views;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import com.raven.rnetspeed.R;
import com.raven.rnetspeed.util.DensityUtil;


/**
 * 字体大小弹窗
 */

public class FontSizeChooseDialog extends DialogPreference implements SeekBar.OnSeekBarChangeListener {

    public static final String TAG = FontSizeChooseDialog.class.getSimpleName();

    private Context mContext;

    public FontSizeChooseDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        SeekBar textSizeBar = view.findViewById(R.id.text_size_bar);
        /* 默认是4 */
        int curSize = getPersistedInt(5);
        textSizeBar.setProgress(curSize-1);
        textSizeBar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        /* 因为默认的textSize是5，而默认的progress是4，所以这里需要加上偏差1 */
        progress += 1;
        callChangeListener(DensityUtil.dip2px(mContext,progress));
        persistInt(progress);
        notifyChanged();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
