package com.bodhi.llc.recyclerfastscrollwithalphabeticscroll;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String[] alphabetArr = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        ArrayList dataList = new ArrayList();
        for (int i = 0; i < alphabetArr.length; i++) {
            for (int j = 0; j < 10; j++) {
                dataList.add(new DataModel("" + alphabetArr[i] + "_Someone's Name " + j, " Lastname"));
            }
        }
        FastScrollRecyclerView simpleList = (FastScrollRecyclerView) findViewById(R.id.demo_list);
        simpleList.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        DemoListAdapter demolistAdapter = new DemoListAdapter(MainActivity.this, dataList);
        simpleList.setAdapter(demolistAdapter);
        final CircleView circle = (CircleView) findViewById(R.id.circle);
        findViewById(R.id.plus_zoom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                circle.zoomIn();
            }
        });

        findViewById(R.id.minus_zoom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                circle.zoomOut();
            }
        });

    }
}
