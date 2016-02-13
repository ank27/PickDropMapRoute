package com.example.ankurkhandelwal.routemap;

import android.app.Application;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Common extends Application {
    public static final LatLng ROYAL_PALM = new LatLng(19.1510440, 72.8905230);
    public static final LatLng POWAI = new LatLng(19.1202337, 72.898813);
    public static final LatLng CHANDIVALI = new LatLng(19.1109356, 72.8954901);
    public static final LatLng VIKHROLI = new LatLng(19.1028127, 72.9184178);
    public static final LatLng VERLI=new LatLng(19.0088792,72.8130712);
    public static LatLng pickselected=null;
    public static LatLng dropselected=null;
    public static LatLng userLocation=null;
    public static String pickPoint="";
    public static String dropPoint="";
    public static ArrayList<String> distance=new ArrayList<String>();
    public static ArrayList<String> time=new ArrayList<String>();
    public static boolean fromUsertoPick=true;
    @Override

    public void onCreate() {
        super.onCreate();
    }
}
