package com.example.weixin50.test;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.example.weixin50.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ldh on 2017/5/23.
 */

public class HttpTest {

    public HttpTest(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    private RequestQueue requestQueue;



    public void jsonObjectRequest() {
        Log.d("duanhao", "jsonObject.toString()");
        Map<String, Object> map = new HashMap<>();
        String[] ss = {"AA", "bb", "cc", "dd"};
        map.put("name1", "value1");
        map.put("name2", "value2");
        map.put("list", ss);
        JSONObject jsonObject = new JSONObject(map);
        Log.d("duanhao", jsonObject.toString());
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, "http://www.baidu.com", jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.d("duanhao", jsonObject.toString());
                if (BuildConfig.DEBUG) Log.d("HttpTest", jsonObject.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d("duanhao", "volleyError.getMessage()");
            }
        }) {
            //注意此处override的getParams()方法,在此处设置post需要提交的参数根本不起作用
            //必须象上面那样,构成JSONObject当做实参传入JsonObjectRequest对象里
            //所以这个方法在此处是不需要的
//    @Override
//    protected Map<String, String> getParams() {
//          Map<String, String> map = new HashMap<String, String>();
//            map.put("name1", "value1");
//            map.put("name2", "value2");

//        return params;
//    }

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json; charset=UTF-8");
                return headers;
            }
        };
        requestQueue.add(jsonRequest);

    }

    public void setPrefer() {
        String url = "http://api.anjuke.com/weiliao/imcenter/setPrefer";
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", "ab07f96b1e05b2e80e10d7998e9aedf0");
        params.put("user_source", "4");
        ExtraValues values = new ExtraValues("海欣小区，金港花园", "三室一厅");

        String sprefer = "{\"preferCommunities\":\"下城 长庆\",\"preferProperties\":\"户型不限 120-150万元\"}";
        try {
            JSONObject json = new JSONObject(sprefer);
            params.put("prefer", json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = new JSONObject(params);
        Log.d("volley", "◀▬▬▬▬▬ /imcenter/setPrefer ▬▬▬▬post 参数：" + jsonObject.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.d("volley", "◀▬▬▬▬▬ /imcenter/setPrefer ▬▬▬▬ result：" + jsonObject.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d("volley", "◀▬▬▬▬▬ /imcenter/setPrefer ▬▬▬▬ errorMsg：" + volleyError.getMessage());
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    private void getPrefer() {
        String url = "http://api.anjuke.com/weiliao/imcenter/getPrefer";
        final Map<String, String> params = new HashMap<>();
        params.put("user_id", "ab07f96b1e05b2e80e10d7998e9aedf0");
        params.put("user_source", "4");
        JSONObject jsonObject = new JSONObject(params);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.d("volley", "▬▬▬▬▬▶ChatApi.URL_QKH_GET_PREFER ▬▬▬▬结果：" + jsonObject.toString());
                if (BuildConfig.DEBUG) Log.d("HttpTest", jsonObject.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d("volley", "▬▬▬▬▬▶ChatApi.URL_QKH_GET_PREFER ▬▬▬▬结果：" + volleyError.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //这个方法再StringRequest中才走,JsonObjectRequest不走
                Log.d("volley", "▬▬▬▬▬▶ChatApi.URL_QKH_GET_PREFER ▬▬▬▬参数：" + params.toString());
                return params;
            }
        };
        requestQueue.add(request);
    }
}
