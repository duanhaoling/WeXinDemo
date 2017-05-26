package com.example.weixin50.test;


import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ldh on 2016/9/2 0002.
 */
public class Test {
    public static void main(String[] args){
        Map<String, Object> map = new HashMap<>();
        String[] ss = {"AA","bb","cc","dd"};
        map.put("name1", "value1");
        map.put("name2", "value2");
//        map.put("list", ss);
        JSONObject jsonObject = new JSONObject(map);

        System.out.print(jsonObject.toString());
    }
}
