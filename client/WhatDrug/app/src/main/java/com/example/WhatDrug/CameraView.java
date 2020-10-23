package com.example.WhatDrug;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import static android.content.ContentValues.TAG;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class CameraView extends View {

    public CameraView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Display disp = ((WindowManager)this.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        float left = (float)(disp.getWidth()*0.25);
        float top = (float)(disp.getHeight()*0.25);
        float len = (float)(disp.getWidth()/2);

        // 배경
        Paint paint = new Paint();

        // 하얀 사각형 테두리
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(15.0F);
        canvas.drawRect(left, top, left+len, top+len, paint);

        // 버튼 사각형
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xaa000000);
        canvas.drawRect(0, (float)(disp.getHeight()*0.75), disp.getWidth(), disp.getHeight(), paint);
        Log.i(TAG, "Height: " + disp.getHeight()*0.75 );
    }
}
