package com.example.ankurkhandelwal.routemap;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

public class ListAdapter extends BaseAdapter{
        Context context;
        ListAdapter(Context c){
            context = c;
        }
        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, final View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.single_row, parent, false);
            TextView pickPoint = (TextView) row.findViewById(R.id.pickPoint);
            TextView dropPoint = (TextView) row.findViewById(R.id.dropPoint);

            TextView pickTime = (TextView) row.findViewById(R.id.pickTime);
            TextView dropTime = (TextView) row.findViewById(R.id.dropTime);
            Button show_map= (Button) row.findViewById(R.id.show_map_button);
            switch (position){
                case 0:
                    pickPoint.setText("Powai");
                    pickTime.setText("10:00");
                    dropPoint.setText("Chandivali");
                    dropTime.setText("01:00");
                    break;
                case 1:
                    pickPoint.setText("Powai");
                    pickTime.setText("10:00");
                    dropPoint.setText("Vikhroli");
                    dropTime.setText("01:00");
                    break;
                case 2:
                    pickPoint.setText("Worli");
                    pickTime.setText("10:00");
                    dropPoint.setText("Vikhroli");
                    dropTime.setText("01:00");
            }

            show_map.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent mapIntent=new Intent(context, MapsActivity.class);
                    if(position==0){
                        Common.pickselected=Common.POWAI;
                        Common.dropselected=Common.CHANDIVALI;
                        Common.pickPoint="Powai";
                        Common.dropPoint="Chandivali";
                    }else if(position==1){
                        Common.pickselected=Common.POWAI;
                        Common.dropselected=Common.VIKHROLI;
                        Common.pickPoint="Powai";
                        Common.dropPoint="Vikhroli";
                    }else {
                        Common.pickselected=Common.VERLI;
                        Common.dropselected=Common.VIKHROLI;
                        Common.pickPoint="Worli";
                        Common.dropPoint="Vikhroli";
                    }
                    context.startActivity(mapIntent);
                }
            });

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent mapIntent=new Intent(context, MapsActivity.class);
                    if(position==0){
                        Common.pickselected=Common.POWAI;
                        Common.dropselected=Common.CHANDIVALI;
                        Common.pickPoint="Powai";
                        Common.dropPoint="Chandivali";
                    }else if(position==1){
                        Common.pickselected=Common.POWAI;
                        Common.dropselected=Common.VIKHROLI;
                        Common.pickPoint="Powai";
                        Common.dropPoint="Vikhroli";
                    }else {
                        Common.pickselected=Common.VERLI;
                        Common.dropselected=Common.VIKHROLI;
                        Common.pickPoint="Worli";
                        Common.dropPoint="Vikhroli";
                    }
                    context.startActivity(mapIntent);
                }
            });

            return row;
        }
}
