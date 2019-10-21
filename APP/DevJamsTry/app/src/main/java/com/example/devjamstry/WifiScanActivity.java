package com.example.devjamstry;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.accessibilityservice.FingerprintGestureController;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WifiScanActivity extends AppCompatActivity {
    SwipeRefreshLayout pullToRefresh;
    public WifiManager wifiManager;
    public ListView listView;
    public FloatingActionButton buttonScan;
    ArrayList<String> latArray = new ArrayList<>();
    ArrayList<String> lngArray = new ArrayList<>();
    public String url = "https://reach-django.herokuapp.com/main/dbpost";
    public String url4 = "https://reach-django.herokuapp.com/main/fingerprint";
    public RequestQueue mRequestQueue;
    public JSONArray jsArray;
    public JSONObject jsObject,respObj;
    public List<ScanResult> results;
    public String totalTotalList = "[[12.9704386, 79.1600239, 2.51188643150958],[12.9705059, 79.1599716, 7.943282347242816],[12.9705323, 79.1600782, 17.78279410038923]]";
    public ArrayList<String> arrayList = new ArrayList<>();
    public ArrayList<Double> listLat = new ArrayList<>();
    public ArrayList<Double> listLong = new ArrayList<>();
    public ArrayList<Double> listTotal = new ArrayList<>();
    public ArrayList<String> distanceList = new ArrayList<String>();
    public ArrayAdapter adapter;
    public String resp;
    public double[][] positions = new double[][]{{12.9704386,79.1600239},{12.9705059,79.1599716},{12.9705323,79.1600782}};
    public double[] distances = new double[]{2.51188643150958,7.943282347242816,17.78279410038923};
    private RequestQueue mRequestQueue2;
    private String url2 = "https://indoor-nav.herokuapp.com/location";
    private String url3 = "https://indoor-nav.herokuapp.com/grid_id";

    private String xlat=null,ylong=null;
    private RequestQueue mRequestQueue3;
    public String home_lat,home_lng;
    private RequestQueue mRequestQueue4;
    private String blockID,dis,grid_lat,grid_lng;
    private ArrayList<String> bssidlist = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_scan);
        home_lat = getIntent().getStringExtra("Lat");
        home_lng = getIntent().getStringExtra("Lng");
        System.out.println(home_lat+ " : " + home_lng);
        try {
            String response = getIntent().getStringExtra("JSONObjectFromFirstAPI");
            if(response!=null){
                 respObj= new JSONObject(response);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        /*pullToRefresh =  findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                scanWifi();
            }
        });*/
        buttonScan = findViewById(R.id.rescan);
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanWifi();
            }
        });

        FloatingActionButton fab = findViewById(R.id.ggetLatLng);
        fab.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                try {
                    getLatLng();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        listView = findViewById(R.id.wifiList);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "WiFi is disabled ... We need to enable it", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }
        ////////SMV
        /*listLat.add(12.9692640);listLong.add(79.1573102);
        listLat.add(12.9695835);listLong.add(79.1575214);
        listLat.add(12.9695499);listLong.add(79.1579338);
        listLat.add(12.9692349);listLong.add(79.1581306);
        listLat.add(12.9688964);listLong.add(79.1579442);
        listLat.add(12.9689026);listLong.add(79.1575204);*/
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////TT
        /*listLat.add(12.9708048);listLong.add(79.1589048);
        listLat.add(12.9708751);listLong.add(79.1594955);
        listLat.add(12.9709234);listLong.add(79.1599904);
        listLat.add(12.9704301);listLong.add(79.1601054);
        listLat.add(12.9703098);listLong.add(79.1595582);
        listLat.add(12.9702794);listLong.add(79.1590037);*/
        //////////////////////////////////////////////////////////////////////////////////////////////////////////
        ///////TT GALLERY
        listLat.add(12.9704386);listLong.add(79.1600239);
        listLat.add(12.9705059);listLong.add(79.1599716);
        listLat.add(12.9705323);listLong.add(79.1600782);


        /////////////////////////////////////////////////////////////////////////////////////////////////////////

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);
        scanWifi();
    }
    public void scanWifi(){
        arrayList.clear();
        distanceList.clear();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        Toast.makeText(this, "Scanning WiFi ...", Toast.LENGTH_LONG).show();
    }
    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            results = wifiManager.getScanResults();
            System.out.println("RSSI :: "  + wifiManager.getConnectionInfo().getRssi());
            unregisterReceiver(this);

            int c = 0;
            jsObject = new JSONObject();
            for (ScanResult scanResult : results) {
                double dist = GetDistanceFromRssiAndTxPowerOn1m(scanResult.level,-20);
                distanceList.add(String.valueOf(dist));
                bssidlist.add(scanResult.BSSID);
                if(scanResult.level > -70) {
                    arrayList.add("SSID : " + scanResult.SSID + " -------> BSSID : " + scanResult.BSSID + " -------> RSSI : " + scanResult.level + " ------> Distance : " + dist);
                    adapter.notifyDataSetChanged();
                }
            }
            for (ScanResult scanResult : results) {
                double dist = GetDistanceFromRssiAndTxPowerOn1m(scanResult.level,-20);
                try {
                    if(c==3){break;}
                    jsObject.put(scanResult.SSID,dist);
                    c++;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public double GetDistanceFromRssiAndTxPowerOn1m(double rssi, int txPower)
    {
        return Math.pow(10, ((double)txPower - rssi) / (10 * 2));
    }
    public void nextMapLayout(View V) throws JSONException {

        int lol = 0;
        /*for(int i=0;i<3;i++){
            listTotal.clear();
            double lat = listLat.get(i);
            double lng = listLong.get(i);
            double dist = distances[lol];
            listTotal.add(lat);
            listTotal.add(lng);
            listTotal.add(dist);
            lol++;
            totalTotalList.add(listTotal);
            System.out.println(totalTotalList);
        }*/
        /*JSONObject finalJSON = new JSONObject();
        finalJSON.put("distances",totalTotalList);
        System.out.println(finalJSON);

        JSONObject Obj2 = finalJSON;*/
        JSONArray Obj1 = respObj.getJSONArray("result");
        JSONObject combined = new JSONObject();
        combined.put("result", Obj1);
        combined.put("distances", distanceList);
        Toast.makeText(this, "Final Response : " + combined.get("distances"), Toast.LENGTH_LONG).show();
        mRequestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, combined, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Response : ",response.toString());
                resp = response.toString();
                Log.d("Response : ",response.toString());
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error : " ,error.toString());
                    }
                });

        mRequestQueue.add(jsonObjectRequest);

        getLatLng();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                mRequestQueue3 = Volley.newRequestQueue(WifiScanActivity.this);

                JSONObject jsonObject3 = null;
                try {
                    jsonObject3 = new JSONObject("{\"data\":{\"user_lat\":" + xlat + ",\"user_lng\":" + ylong + ",\"home_lat\":" + home_lat + ",\"home_lng\":" + home_lng + "}}");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                System.out.println(jsonObject3);
                JsonObjectRequest jsonObjectRequest3 = new JsonObjectRequest(url3, jsonObject3, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response : ", response.toString());
                        System.out.println(response.toString());

                        try {
                            grid_lat = response.getString("lat");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            grid_lng = response.getString("lng");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            blockID = response.getString("gid");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        });

                mRequestQueue3.add(jsonObjectRequest3);
            }
        }, 4000);

        ArrayList<String> FingerPrint = new ArrayList<>();
        ArrayList<ArrayList<String>> arr = new ArrayList<>();
        for(int i = 0;i<5;i++){
            FingerPrint.clear();
            FingerPrint.add(bssidlist.get(i));
            FingerPrint.add(distanceList.get(i));
            arr.add(FingerPrint);
        }
        final JSONObject fourth = new JSONObject();
        fourth.put("block_id",blockID);
        System.out.println(fourth);
        fourth.put("fingerprint",arr);
        System.out.println(fourth);

        final Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRequestQueue4 = Volley.newRequestQueue(WifiScanActivity.this);
                JsonObjectRequest jsonObjectRequest4 = new JsonObjectRequest(url4, fourth, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response : ",response.toString());

                    }
                },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("Error : " ,error.toString());

                            }
                        });

                mRequestQueue4.add(jsonObjectRequest4);

            }
        }, 6000);

        final Handler handler3 = new Handler();
        handler3.postDelayed(new Runnable() {
            @Override
            public void run() {
                System.out.println(xlat + " : " + ylong);
                Intent i = new Intent(WifiScanActivity.this,ShowMapAdmin.class);
                i.putExtra("lat_list",latArray);
                i.putExtra("lng_list",lngArray);
                startActivity(i);
            }
        }, 7000);


    }

    public void getLatLng() throws JSONException {

        JSONObject jsssss = new JSONObject();
        jsssss.put("distances",totalTotalList);
        mRequestQueue2 = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest2 = new JsonObjectRequest(url2, jsssss, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Response : ",response.toString());
                try {
                    xlat = response.getString("x");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    ylong = response.getString("y");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error : " ,error.toString());

                    }
                });


        latArray.add(xlat);
        lngArray.add(ylong);
        mRequestQueue2.add(jsonObjectRequest2);
    }
}
