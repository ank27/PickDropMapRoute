package com.example.ankurkhandelwal.routemap;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;


public class MainActivity extends AppCompatActivity{
    ListView listView;
    ListAdapter listAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("PickUp-Drop Route Map");
        toolbar.setNavigationIcon(R.drawable.map_logo);
        listView=(ListView) findViewById(R.id.listView);
        listAdapter=new ListAdapter(this);
        listView.setAdapter(listAdapter);

    }
}
