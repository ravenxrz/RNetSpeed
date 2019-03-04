package com.raven.rnetspeed.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.raven.rnetspeed.R;


public class TestActivity extends AppCompatActivity {
    float x =0 ,y = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

       findViewById(R.id.linearlayout).setOnTouchListener(new View.OnTouchListener() {
           @Override
           public boolean onTouch(View v, MotionEvent event) {
               x = event.getRawX();
               y = event.getRawY();
               switch (event.getAction()){
                   case MotionEvent.ACTION_DOWN:
                       Log.i("Down","x = "+x+" y="+y);
                       break;
                   case MotionEvent.ACTION_MOVE:
                       Log.i("MOVE","x = "+x+" y="+y);
                       break;
                   case MotionEvent.ACTION_UP:
                       Log.i("UP","x = "+x+" y="+y);
                       break;
               }
               return true;
           }
       });
    }
}
