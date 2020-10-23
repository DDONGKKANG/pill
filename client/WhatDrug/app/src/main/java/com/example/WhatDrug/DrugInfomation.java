package com.example.WhatDrug;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.WhatDrug.R;

import java.io.BufferedInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;


public class DrugInfomation extends AppCompatActivity{
    private RecyclerAdapter adapter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = getSupportActionBar();

        // Custom Actionbar를 사용하기 위해 CustomEnabled을 true 시키고 필요 없는 것은 false 시킨다
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);            //액션바 아이콘을 업 네비게이션 형태로 표시합니다.
        actionBar.setDisplayShowTitleEnabled(false);        //액션바에 표시되는 제목의 표시유무를 설정합니다.
        actionBar.setDisplayShowHomeEnabled(false);            //홈 아이콘을 숨김처리합니다.


        //layout을 가지고 와서 actionbar에 포팅을 시킵니다.
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View actionbar = inflater.inflate(R.layout.custom_actionbar, null);

        actionBar.setCustomView(actionbar);

        //액션바 양쪽 공백 없애기
        Toolbar parent = (Toolbar)actionbar.getParent();
        parent.setContentInsetsAbsolute(0,0);

        // 뒤로가기 버튼 이벤트 처리
        ImageButton bt_close = (ImageButton) findViewById(R.id.btnBack);
        bt_close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onBackPressed();
            }
        });

        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().setElevation(0); // Action bar 그림자 제거

        setContentView(R.layout.drug_information);
        Intent intent = getIntent(); /*데이터 수신*/
        final String c_result[] = intent.getExtras().getStringArray("c_result"); /*배열*/

        // RecyclerView

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new RecyclerAdapter();
        recyclerView.setAdapter(adapter);

        List<String> listTitle = Arrays.asList("의약품제형", "분류명", "효능/효과", "복용법", "보관방법",
                "주의사항");

        if( c_result.length == 1 )
        {
            System.out.println(c_result[0]);
            Toast.makeText(
                    this, "사진을 새로 찍어 주세요", Toast.LENGTH_LONG).show();
            onBackPressed();
        } else {

            TextView d_name = (TextView) findViewById(R.id.drug_name);
            d_name.setText(c_result[0]);
            ImageView d_image = (ImageView) findViewById(R.id.drug_image);
            try {
                URL url = new URL(c_result[1]);
                URLConnection conn = url.openConnection();
                conn.connect();
                BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                Bitmap bm = BitmapFactory.decodeStream(bis);
                bis.close();
                d_image.setImageBitmap(bm);
            } catch (Exception e) {
                System.out.println(e);
            }

            List<String> listContent = Arrays.asList(c_result[2],c_result[3],c_result[4],
                    "내용 보기",c_result[6],"내용 보기");
            List<String> listContentPlus =
                    Arrays.asList(null, null, null, c_result[5], null, c_result[7]);

            for (int i = 0; i < listTitle.size(); i++) {
                // 각 List의 값들을 data 객체에 set 해줍니다.
                Data data = new Data();
                data.setTitle(listTitle.get(i));
                data.setContent(listContent.get(i));
                data.setContentPlus(listContentPlus.get(i));

                // 각 값이 들어간 data를 adapter에 추가합니다.
                adapter.addItem(data);
            }

            // adapter의 값이 변경되었다는 것을 알려줍니다.
            // 호출안되면 data 노출 안됨
            adapter.notifyDataSetChanged();
        }

        // Custom Dialog
        // 커스텀 다이얼로그를 호출할 버튼을 정의한다.
        Button button = (Button) findViewById(R.id.btnPlus);


        // 커스텀 다이얼로그 호출할 클릭 이벤트 리스너 정의
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 커스텀 다이얼로그를 생성한다. 사용자가 만든 클래스이다.
                CustomDialog customDialog = new CustomDialog(DrugInfomation.this);

                // 커스텀 다이얼로그를 호출한다.
                // 커스텀 다이얼로그의 결과를 출력할 TextView를 매개변수로 같이 넘겨준다.
                customDialog.callFunction(c_result[0]);
            }
        });

    }
}