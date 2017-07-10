package com.example.weixin50;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.example.weixin50.camera.ShootVideoActivity;
import com.example.weixin50.imageloader.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by ldh on 2016/8/31 0031.
 *
 */
public class ChatMainTabFragment extends Fragment {
    @Bind(R.id.tv_tab01)
    TextView mTv;
    @Bind({R.id.bt1, R.id.bt2, R.id.bt3, R.id.bt4, R.id.bt5})
    List<Button> bts;
    @Bind(R.id.iv_chat)
    ImageView iv_chat;

    private ImageLoader imageLoader;
    private RequestQueue requestQueue;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        requestQueue = Volley.newRequestQueue(getActivity());
        return inflater.inflate(R.layout.tab01, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        initViews();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    private void initViews() {
        bts.get(0).setOnClickListener(view -> jsonObjectRequest());

        bts.get(1).setOnClickListener(view -> testNotification());

        bts.get(2).setOnClickListener(view -> setPrefer());

        bts.get(3).setOnClickListener(view -> getPrefer());

        bts.get(4).setOnClickListener(view -> {
//            Intent intent = new Intent(getActivity(), FileProviderDemo.class);
//            startActivity(intent);
            gotoActivity(ShootVideoActivity.class);
        });
        testImageLoader();
    }

    private void testNotification() {

    }


    private void gotoActivity(Class<? extends AppCompatActivity> clazz) {
        Intent intent = new Intent(getContext(), clazz);
        startActivity(intent);
    }


    private void testImageLoader() {
        if (imageLoader == null) {
            imageLoader = new ImageLoader();
        }
        String url = "http://imgsrc.baidu.com/forum/pic/item/2fdda3cc7cd98d10ab49e9b2213fb80e7aec9090.jpg";
        imageLoader.displayImage(url, iv_chat);
    }

    public void jsonObjectRequest() {
        Log.d("duanhao", "jsonObject.toString()");
        Map<String, Object> map = new HashMap<>();
        String[] ss = {"AA", "bb", "cc", "dd"};
        map.put("name1", "value1");
        map.put("name2", "value2");
        map.put("list", ss);
        JSONObject jsonObject = new JSONObject(map);
        Log.d("volley", jsonObject.toString());
        mTv.setText(jsonObject.toString());
        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, "http://www.baidu.com", jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.d("volley", jsonObject.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d("volley", "volleyError.getMessage()");
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
//        ExtraValues values = new ExtraValues("海欣小区，金港花园", "三室一厅");

        String sprefer = "{\"preferCommunities\":\"下城 长庆\",\"preferProperties\":\"户型不限 120-150万元\"}";
        try {
            JSONObject json = new JSONObject(sprefer);
            params.put("prefer", json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = new JSONObject(params);
        mTv.setText("◀▬▬▬▬▬ /imcenter/setPrefer ▬▬▬▬post 参数：" + jsonObject.toString());
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
                mTv.setText(jsonObject.toString());
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
