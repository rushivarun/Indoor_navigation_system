package com.example.devjamstry;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.textclassifier.TextLinks;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.android.volley.Request.Method.POST;

public class AdminCalculation extends AppCompatActivity {

    public String lat,lng;
    public String resp;

    public RequestQueue mRequestQueue;
    public String url = "https://indoor-nav.herokuapp.com/grid_data";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_calculation);
        lat = getIntent().getStringExtra("Lat");
        lng = getIntent().getStringExtra("Lng");

    }
    public void sendVolleyReq(View V) throws JSONException {
        //RequestQueue initialized
        mRequestQueue = Volley.newRequestQueue(this);

        JSONObject jsonObject = new JSONObject("{\"data\":{\"lat\":"+Double.parseDouble(lat)+",\"lng\":"+Double.parseDouble(lng)+"}}");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Response : ",response.toString());
                resp = response.toString();
                Toast.makeText(AdminCalculation.this, "Response is :: "+response, Toast.LENGTH_SHORT).show();
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(AdminCalculation.this, "Error : " + error, Toast.LENGTH_SHORT).show();
                    }
                });

        mRequestQueue.add(jsonObjectRequest);
    }
    public void goToWifiScanActivity(View V){
        Intent i = new Intent(AdminCalculation.this,WifiScanActivity.class);
        System.out.println(resp);
        i.putExtra("JSONObjectFromFirstAPI",resp);
        startActivity(i);

    }
}
