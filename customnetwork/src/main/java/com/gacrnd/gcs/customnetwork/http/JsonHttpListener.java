package com.gacrnd.gcs.customnetwork.http;

import android.os.Handler;
import android.os.Looper;

import com.alibaba.fastjson.JSON;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Jack_Ou  created on 2020/11/18.
 */
public class JsonHttpListener<T> implements IHttpListener {

    // 用户用什么样的数据来接收
    private Class<T> response;

    // 以对象的形式把数据交给用户
    private IDataListener listener;

    //线程切换
    private Handler handler = new Handler(Looper.getMainLooper());

    public JsonHttpListener(Class<T> response, IDataListener listener) {
        this.response = response;
        this.listener = listener;
    }

    @Override
    public void onSuccess(InputStream inputStream) {
        // 得到网络回来的数据
        String content = getContent(inputStream);
        final T responseObject = JSON.parseObject(content,response);
        // 线程切换
        handler.post(new Runnable() {
            @Override
            public void run() {
                listener.onSuccess(responseObject);
            }
        });
    }

    private String getContent(InputStream inputStream) {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String temp = "";
        StringBuilder sb = new StringBuilder();
        try {
            while (((temp = br.readLine()) != null)){
                sb.append(temp + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString().replace("\n","");
    }

    @Override
    public void onFailure() {

    }
}
