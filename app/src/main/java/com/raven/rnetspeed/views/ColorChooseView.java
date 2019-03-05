package com.raven.rnetspeed.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.raven.rnetspeed.R;
import com.raven.rnetspeed.util.DensityUtil;
import com.smart.colorview.normal.CircleColorView;
import com.smart.colorview.normal.model.CircleColorModel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorChooseView extends LinearLayout {


    private static final String TAG = ColorChooseView.class.getSimpleName();
    /* 颜色圆圈内部颜色 */
    private String [] circleColorList = new String[]{"#FFF44336","#FFE91E63","#FF9C27B0","#FF673AB7",
            "#FF2196F3","#FF795548","#FF9E9E9E","#FF607D8B"};
    /* 颜色圆圈外部line颜色 */
    private String [] outLineColorList = new String[]{"#FFe57373","#FFF06292","#FFBA68C8","#FF9575CD",
            "#FF64B5F6","#FFA1887F","#FFE0E0E0","#FF90A4AE"};
    /* 默认有多少个圆圈 */
    private static final int colorViewNum = 8;
    private CircleColorView [] circleColorViews;
    private int default_pos = 0;
    private Context mContext;

    /* 当前选中颜色点position */
    private int cur_pos = 0;

    /* 手动输入颜色 */
    private EditText mEdittext;



    public ColorChooseView(Context context) {
        this(context,null);
    }

    public ColorChooseView(Context context,  AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        /* init colorView Vars */
        circleColorViews = new CircleColorView[colorViewNum];
        for(int i = 0;i<colorViewNum;i++){
            circleColorViews[i] = new CircleColorView(context);
        }
        mEdittext = new EditText(context);

        /* init default pos */
        TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.ColorChooseView);
        default_pos = a.getInteger(R.styleable.ColorChooseView_default_position,0) % colorViewNum;
        a.recycle();

        initCircleColorOps();

        addCircleColors(context);
    }

    /**
     * 初始话颜色圆圈配置
     */
    private void initCircleColorOps(){
        /* 初始化Circle */
        ViewGroup.MarginLayoutParams mlp = new ViewGroup.MarginLayoutParams(200,200);
        mlp.leftMargin = 100;
        mlp.bottomMargin = 100;
        mlp.rightMargin = 100;
        mlp.topMargin  = 100;

        for(int i = 0;i < colorViewNum;i++){
            CircleColorModel circleColorModel = new CircleColorModel();
            circleColorModel.setCircleColor(Color.parseColor(circleColorList[i]));
            circleColorModel.setSelectUseOutline(true);
            circleColorModel.setOutlineStrokeWidth(4);
            circleColorModel.setOutlineStrokeColor(Color.parseColor(outLineColorList[i]));
            circleColorModel.setInnerType(CircleColorView.InnerType.INNER);
            circleColorModel.setInnerStrokeWidth(0);
            circleColorModel.setInnerStrokeDividerWidth(14);
            circleColorModel.setCircleSelected(false);
            circleColorViews[i].setCircleColorModel(circleColorModel);
            circleColorViews[i].setOnClickListener(circleClickListener);
            circleColorViews[i].setTag(i);
        }
        mEdittext.setHint("自定义（十六进制颜色),如白色FFFFFF");
    }

    View.OnClickListener circleClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            clearSelect();
            int index = (int) v.getTag();
            cur_pos = index;
            circleColorViews[index].setCircleSelected(true);
        }
    };

    /**
     * 设置view的margin
     * @param v
     * @param l
     * @param t
     * @param r
     * @param b
     */
    private void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    /**
     * 设置view集合的margin
     * @param v
     */
    private void setViewsMargin(View[] v){
        int marginDp = DensityUtil.dip2px(mContext,8);
        for (int i = 0; i < v.length; i++) {
            setMargins(v[i],marginDp,marginDp,marginDp,marginDp);
        }
    }


    /**
     * 添加配置好的圆圈到当前view中
     */
    private void addCircleColors(Context context){
        /* 第一行 */
        LinearLayout firstLine = new LinearLayout(context);
        TextView tv = new TextView(context);
        tv.setText("测试。。。");
        firstLine.setGravity(Gravity.CENTER);
        firstLine.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        firstLine.setOrientation(HORIZONTAL);
        firstLine.addView(circleColorViews[0]);
        firstLine.addView(circleColorViews[1]);
        firstLine.addView(circleColorViews[2]);
        firstLine.addView(circleColorViews[3]);

        /* 第二行 */
        LinearLayout secLine = new LinearLayout(context);
        secLine.setGravity(Gravity.CENTER);
        secLine.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        secLine.setOrientation(HORIZONTAL);
        secLine.addView(circleColorViews[4]);
        secLine.addView(circleColorViews[5]);
        secLine.addView(circleColorViews[6]);
        secLine.addView(circleColorViews[7]);

        /* 将两个LinearLayout加入到当前view中 */
        this.setGravity(Gravity.CENTER);
        this.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        this.setOrientation(VERTICAL);
        this.addView(firstLine);
        this.addView(secLine);

        /* add输入面板 */
        this.addView(mEdittext);

        /* 重新更改margins */
        setViewsMargin(circleColorViews);


        /* add success */
        Log.i(ColorChooseView.class.getSimpleName(),"Add");
    }

    /**
     * 清除所有选中状态
     */
    private void clearSelect(){
        for(int i = 0;i<colorViewNum;i++){
            circleColorViews[i].setCircleSelected(false);
        }
    }

    /**
     * 初始化是否有选中的颜色
     * @param color
     */
    public void initSelect(String color){
        clearSelect();
        if("".equals(color)){
            /* 默认为白色 */
//            circleColorViews[default_pos].setCircleSelected(true);
        }else{
            for(int i = 0;i<colorViewNum;i++) {
                if (color.equals(circleColorList[i])) {
                    circleColorViews[i].setCircleSelected(true);
                    cur_pos = i;
                    return;
                }
            }
            /* 代码执行到这儿，说明用户使用了自定义的颜色，所以不需要设置select */
        }
    }

   public String getColor(){
        String input = mEdittext.getText().toString();
        input = input.toUpperCase();
        if(checkInputColor(input)){ /* 首先检查是否有用户输入正确的颜色 */
            return"#ff"+input;
        }else{
            /* 输入不合法，返回当前选中颜色 */
            return circleColorList[cur_pos];
        }
   }

    /**
     * 检查用户输入的颜色是否正确
     * @param color
     * @return
     */
   private boolean checkInputColor(String color){
        if("".equals(color)){
            return false;
        }else{
            if(color.length() != 6){
                return false;
            }
            Pattern pattern = Pattern.compile("(\\d|[a-f]|[A-F])+");
            Matcher matcher = pattern.matcher(color);
            if(matcher.find() && matcher.group().length() == 6){
                return true;
            }else{
                return false;
            }
        }
   }

}
