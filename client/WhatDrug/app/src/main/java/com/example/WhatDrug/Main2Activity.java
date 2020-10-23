package com.example.WhatDrug;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.WhatDrug.R;

import java.util.ArrayList;

/**
 * 서피스뷰를 이용해 미리보기 화면을 만든 후 사진찍기를 하는 방법에 대해 알 수 있습니다.
 *
 * @author Mike
 *
 */
public class Main2Activity extends AppCompatActivity {
    public TextView textView;

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
        setContentView(R.layout.activity_main2);
        getSupportActionBar().setElevation(0); // Action bar 그림자 제거

        textView = (TextView) findViewById(R.id.textView1);

        // CustomDialog에서 데이터받기( name, week(요일/배열), time_hour, time_min )
        StringBuilder days = new StringBuilder("");

        Intent intent = getIntent(); /*데이터 수신*/

        String name = intent.getExtras().getString("name");
            String week[] = intent.getExtras().getStringArray("week");
            String hour = intent.getExtras().getString("time_hour");
            String min = intent.getExtras().getString("time_min");

            for ( String day : week) {
                if( day != null ) {
                    days.append(day);
                    days.append(" ");
                } else continue;
            }
            String dayOftheWeek = String.valueOf(days);

            ListView listView = (ListView) findViewById(R.id.listView);
            final SingerAdapter adapter = new SingerAdapter();

            adapter.addItem(new SingerItem(name, dayOftheWeek, hour + "시" + min + "분", R.drawable.pill));

            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    SingerItem item = (SingerItem) adapter.getItem(position);
                }
            });


        // 버튼 이벤트 처리
        Button btn_camera = (Button) findViewById(R.id.btn_camera);
        btn_camera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
                startActivity(intent);
            }
        });

        // 버튼 이벤트 처리
        Button btn_shape = (Button) findViewById(R.id.btn_shape);
        btn_shape.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void openOptionsMenu() {
        super.openOptionsMenu();
    }

    class SingerAdapter extends BaseAdapter {
        ArrayList<SingerItem> items = new ArrayList<SingerItem>();

        @Override
        public int getCount() {
            return items.size();
        }

        public void addItem(SingerItem item) {
            items.add(item);
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            SingerItemView view = new SingerItemView(getApplicationContext());

            SingerItem item = items.get(position);
            view.setName(item.getName());
            view.setWeek(item.getWeek());
            view.setTime(item.getTime());
            view.setImg(item.getImg());
            return view;
        }
    }

}
