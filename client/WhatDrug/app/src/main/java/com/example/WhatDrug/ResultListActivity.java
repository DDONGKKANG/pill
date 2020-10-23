package com.example.WhatDrug;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import android.widget.ImageButton;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class ResultListActivity extends AppCompatActivity {
    String[] c_result;

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
        setContentView(R.layout.activity_result_list);

        ListView listView = (ListView) findViewById(R.id.listView);
        final PillAdapter adapter = new PillAdapter();

        c_result = new String[8];

        // CSV file
        readPillData();

        // Search Activity getIntent()
        Intent intent = getIntent(); /* 데이터 수신 */
        String name = intent.getExtras().getString("name");

        for(PillSample data : pillSamples) {
            String pill_name = data.getName();
            if ( pill_name.contains("(")) {
                int position = pill_name.indexOf("(");
                pill_name = pill_name.substring(0, position);

                Log.d("ResultListActivity", String.valueOf(position));
            }
            if( pill_name.contains(name) ) {

                adapter.addItem(new PillItem(
                        pill_name, data.getImg_uri(), "앞 : " + data.getPill_front(),
                        "뒤 : " + data.getPill_back(),
                        data.getShape(), data.getColor(), data.getClassfication(), data.getEffect(),
                        data.getTake(), data.getStore(), data.getWarning()));
            } else
                continue;
        }

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                PillItem item = (PillItem) adapter.getItem(position);

                Log.i("ResultListActivity", "item.getName(): " + item.getName());

                c_result[0] = item.getName();
                c_result[1] = item.getImg_pill();
                c_result[2] = item.getShape();
                c_result[3] = item.getClassification();
                c_result[4] = item.getEffect();
                c_result[5] = item.getTake();
                c_result[6] = item.getStore();
                c_result[7] = item.getWarning();


                Intent intent = new Intent(getApplicationContext(), DrugInfomation.class);
                intent.putExtra("c_result",c_result);
                startActivity(intent);

            }
        });
    }

    // CSV READ
    private List<PillSample> pillSamples = new ArrayList<>();
    private void readPillData() {
        InputStream is = getResources().openRawResource(R.raw.info_medicine);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8"))
        );

        String line = "";
        try {
            // Step over headers
            reader.readLine();
            Log.d("ResultListAcitivity", line);

            while ( ( line = reader.readLine()) != null) {
                // Split by '# '
                String[] tokens = line.split("#");

                // Read the data
                PillSample sample = new PillSample();
                sample.setNumber(tokens[0]);
                sample.setName(tokens[1]);
                sample.setImg_uri(tokens[2]);
                sample.setPill_front(tokens[3]);
                sample.setPill_back(tokens[4]);
                sample.setShape(tokens[5]);
                sample.setColor(tokens[6]);
                sample.setClassfication(tokens[7]);
                sample.setEffect(tokens[8]);
                sample.setTake(tokens[9]);
                sample.setStore(tokens[10]);
                sample.setWarning(tokens[11]);

                pillSamples.add(sample);

                Log.d("ResultListActivity", "Just created: " + sample);
            }
        } catch (IOException e) {
            Log.wtf("ResultListActivity", "Error reading data file on Line " + line, e);
            e.printStackTrace();
        }
    }

    class PillAdapter extends BaseAdapter {
        ArrayList<PillItem> items = new ArrayList<PillItem>();

        @Override
        public int getCount() {
            return items.size();
        }

        public void addItem(PillItem item) {
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
            PillItemView view = new PillItemView(getApplicationContext());

            PillItem item = items.get(position);
            view.setName(item.getName());
            view.setShape(item.getImg_front());
            view.setColor(item.getImg_back());

            return view;
        }
    }

}