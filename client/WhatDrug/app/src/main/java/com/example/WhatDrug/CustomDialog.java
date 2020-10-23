package com.example.WhatDrug;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomDialog implements TimePicker.OnTimeChangedListener {

    private Context context;
    ToggleButton toggleButton1;
    ToggleButton toggleButton2;
    ToggleButton toggleButton3;
    ToggleButton toggleButton4;
    ToggleButton toggleButton5;
    ToggleButton toggleButton6;
    ToggleButton toggleButton7;
    TimePicker timePicker;
    String hour;
    String min;

    EditText editText;


    String week[] = new String[8];

    public CustomDialog(Context context) {
        this.context = context;
    }

    // 호출할 다이얼로그 함수를 정의한다.
    public void callFunction(final String main_label) {

        // 커스텀 다이얼로그를 정의하기위해 Dialog클래스를 생성한다.
        final Dialog dlg = new Dialog(context);

        // 액티비티의 타이틀바를 숨긴다.
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 커스텀 다이얼로그의 레이아웃을 설정한다.
        dlg.setContentView(R.layout.custom_dialog);

        // 커스텀 다이얼로그의 각 위젯들을 정의한다.
        final EditText editText = (EditText) dlg.findViewById(R.id.mesgase);
        final Button okButton = (Button) dlg.findViewById(R.id.okButton);
        final Button cancelButton = (Button) dlg.findViewById(R.id.cancelButton);

        toggleButton1 = (ToggleButton) dlg.findViewById(R.id.toggle_sun);
        toggleButton2 = (ToggleButton) dlg.findViewById(R.id.toggle_mon);
        toggleButton3 = (ToggleButton) dlg.findViewById(R.id.toggle_tue);
        toggleButton4 = (ToggleButton) dlg.findViewById(R.id.toggle_wed);
        toggleButton5 = (ToggleButton) dlg.findViewById(R.id.toggle_thu);
        toggleButton6 = (ToggleButton) dlg.findViewById(R.id.toggle_fri);
        toggleButton7 = (ToggleButton) dlg.findViewById(R.id.toggle_sat);

        timePicker = (TimePicker) dlg.findViewById(R.id.timePicker);

        /* 다이얼로그에서 시간 값 입력 안할 시 현재 시간 입력위한 시각 구하기 */
        long now = System.currentTimeMillis();
        Date date = new Date(now);

        SimpleDateFormat hour_1 = new SimpleDateFormat("HH");
        final String cur_hour = hour_1.format(date);

        SimpleDateFormat min_1 = new SimpleDateFormat("mm");
        final String cur_min = min_1.format(date);


        // 커스텀 다이얼로그를 노출한다.
        dlg.show();
        editText.setHint(main_label);

        ToggleAction(); // 요일 선택
        timePicker.setOnTimeChangedListener(this);  // 시간 선택

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // '확인' 버튼 클릭시 메인 액티비티에서 설정한 main_label에
                // 커스텀 다이얼로그에서 입력한 메시지를 대입한다.

                if ( editText.getText().toString().length() == 0 ) {
                    editText.setText(main_label);
                }
                if ( hour == null || min == null) {
                    hour = cur_hour;
                    min = cur_min;
                }

                Intent intent = new Intent(context.getApplicationContext(), Main2Activity.class);
                intent.putExtra("name", editText.getText().toString());
                intent.putExtra("week", week);
                intent.putExtra("time_hour", hour);
                intent.putExtra("time_min", min);
                context.startActivity(intent);

                // 커스텀 다이얼로그를 종료한다.
                dlg.dismiss();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "취소 했습니다.", Toast.LENGTH_SHORT).show();

                // 커스텀 다이얼로그를 종료한다.
                dlg.dismiss();
            }
        });
    }

    private void ToggleAction() {

        toggleButton1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if ( isChecked == true) {
                    toggleButton1.setTextColor(Color.RED);
                    week[0] = "일";
                } else
                    week[0] = "0";
            }
        });
        toggleButton2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if ( isChecked == true) {
                    toggleButton2.setTextColor(Color.RED);
                    week[1] = "월";
                } else
                    week[1] = "0";
            }
        });
        toggleButton3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if ( isChecked == true) {
                    toggleButton3.setTextColor(Color.RED);
                    week[2] = "화";
                } else
                    week[2] = "0";
            }
        });
        toggleButton4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if ( isChecked == true) {
                    toggleButton4.setTextColor(Color.RED);
                    week[3] = "수";
                } else
                    week[3] = "0";
            }
        });
        toggleButton5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if ( isChecked == true) {
                    toggleButton5.setTextColor(Color.RED);
                    week[4] = "목";
                } else
                    week[4] = "0";
            }
        });
        toggleButton6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if ( isChecked == true) {
                    toggleButton6.setTextColor(Color.RED);
                    week[5] = "금";
                } else
                    week[5] = "0";
            }
        });
        toggleButton7.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if ( isChecked == true) {
                    toggleButton7.setTextColor(Color.RED);
                    week[6] = "토";
                } else
                    week[6] = "0";
            }
        });
    }

    @Override
    public void onTimeChanged(TimePicker timePicker, int hour1, int min1) {
        hour = String.valueOf(hour1);
        min = String.valueOf(min1);
    }
}

