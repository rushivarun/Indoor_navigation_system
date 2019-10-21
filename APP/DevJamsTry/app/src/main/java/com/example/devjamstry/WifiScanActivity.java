package com.example.devjamstry;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.textclassifier.TextLinks;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.math.*;

public class WifiScanActivity extends AppCompatActivity {
    public WifiManager wifiManager;
    public ListView listView;
    public Button buttonScan;
    public String url = "https://reach-django.herokuapp.com/main/dbpost";
    public RequestQueue mRequestQueue;
    public JSONArray jsArray;
    public JSONObject jsObject,respObj;
    public List<ScanResult> results;
    public String totalTotalList = "[[12.9704386, 79.1600239, 2.51188643150958],[12.9705059, 79.1599716, 7.943282347242816],[12.9705323, 79.1600782, 17.78279410038923]]";
    public ArrayList<String> arrayList = new ArrayList<>();
    public ArrayList<Double> listLat = new ArrayList<>(),listLong = new ArrayList<>(),listTotal = new ArrayList<>(),distanceList = new ArrayList<>();
    public ArrayAdapter adapter;
    public String resp;
    public double[][] positions = new double[][]{{12.9704386,79.1600239},{12.9705059,79.1599716},{12.9705323,79.1600782}};
    public double[] distances = new double[]{2.51188643150958,7.943282347242816,17.78279410038923};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_scan);
        try {
            if(!(getIntent().getStringExtra("JSONObjectFromFirstAPI").equals(""))){
                 respObj= new JSONObject(getIntent().getStringExtra("JSONObjectFromFirstAPI"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        buttonScan = findViewById(R.id.scanBtn);
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanWifi();
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
        Toast.makeText(this, "Scanning WiFi ...", Toast.LENGTH_SHORT).show();
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
                distanceList.add(dist);

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
        /*
         * RSSI = TxPower - 10 * n * lg(d)
         * n = 2 (in free space)
         *
         * d = 10 ^ ((TxPower - RSSI) / (10 * n))
         */
        return Math.pow(10, ((double)txPower - rssi) / (10 * 2));
    }
    public void nextEvent(View V) throws JSONException {

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
        JSONObject finalJSON = new JSONObject();
        finalJSON.put("distances",totalTotalList);
        System.out.println(finalJSON);
        JSONArray Obj1 = respObj.getJSONArray("result");
        JSONObject Obj2 = finalJSON;
        JSONObject combined = new JSONObject();
        combined.put("result", Obj1);
        combined.put("distances", Obj2);
        Toast.makeText(this, "Final Response : " + combined, Toast.LENGTH_SHORT).show();
        mRequestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, combined, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Response : ",response.toString());
                resp = response.toString();
                Toast.makeText(WifiScanActivity.this, "Response is :: "+response, Toast.LENGTH_SHORT).show();
                Log.d("Response : ",response.toString());
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error : " ,error.toString());
                        Toast.makeText(WifiScanActivity.this, "Error : " + error, Toast.LENGTH_SHORT).show();
                    }
                });

        mRequestQueue.add(jsonObjectRequest);

    }
}
