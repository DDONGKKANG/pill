package com.example.WhatDrug;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

class SingerItemView extends LinearLayout {
    TextView textView;
    TextView textView2;
    TextView textView3;
    ImageView imageView;

    public SingerItemView(Context context) {
        super(context);
        init(context);
    }

    public SingerItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.singer_item, this, true);
        textView = (TextView) findViewById(R.id.textView);
        textView2 = (TextView) findViewById(R.id.week);
        textView3 = (TextView) findViewById(R.id.time);
        imageView = (ImageView) findViewById(R.id.imageView);
    }
    void setImg(int img_pill) { imageView.setImageResource(img_pill); }

    void setName(String name_pill) { textView.setText(name_pill); }

    void setWeek(String week_pill) { textView2.setText(week_pill); }

    void setTime(String time_pill) { textView3.setText(time_pill);}

}




