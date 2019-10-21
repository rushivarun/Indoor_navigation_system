package com.example.devjamstry;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class ShowMapAdmin extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public String lat,lng;
    ArrayList<String> lat_arr,lng_arr;
    ArrayList<LatLng> latlng_array;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_map_admin);

        Bundle b = getIntent().getExtras();

        if (null != b) {
            lat_arr = b.getStringArrayList("lat_list");
            Log.i("Lat-List", "Passed Array List :: " + lat_arr);
            lng_arr = b.getStringArrayList("lng_list");
            Log.i("Lng-List", "Passed Array List :: " + lng_arr);

        }


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.g_map);
        mapFragment.getMapAsync(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        for (int i = 0;i<lat_arr.size();i++){
            latlng_array.add(new LatLng(Double.parseDouble(lat_arr.get(i)),Double.parseDouble(lng_arr.get(i))));
        }
        LatLng user = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));
        mMap.addMarker(new MarkerOptions().position(user).title("Admin's Current Location"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(user,24));


        mMap.addPolyline(new PolylineOptions().addAll(latlng_array).width(2f).color(R.color.colorAccent));

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
