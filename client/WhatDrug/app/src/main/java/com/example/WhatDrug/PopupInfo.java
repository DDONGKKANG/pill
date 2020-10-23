package com.example.WhatDrug;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.WhatDrug.R;

public class PopupInfo extends AppCompatActivity {

    Button okBtn, cancleBtn;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 상태바 제거
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.popup_info);

        okBtn = (Button) findViewById(R.id.okBtn);

        Intent intent = getIntent(); /*데이터 수신*/
        String name = intent.getExtras().getString("name"); // 약국 이름
        String location = intent.getExtras().getString("location"); // 약국 위치
        String call = intent.getExtras().getString("call"); // 약국 전화번호

        TextView pharmacy_name = (TextView) findViewById(R.id.pharmacy_name);
        TextView pharmacy_call = (TextView) findViewById(R.id.pharmacy_call);
        TextView pharmacy_location = (TextView) findViewById(R.id.pharmacy_location);

        pharmacy_name.setText(name);
        pharmacy_call.setText("전화번호 : " + call);
        pharmacy_location.setText("주소 : " + location);
    }

    //동작 버튼 글릭
    public void mOk(View v) {
        finish();
    }

    @Override
    public boolean onTouchEvent (MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        // 안드로이드 백버튼 막기
        return;
    }

}
