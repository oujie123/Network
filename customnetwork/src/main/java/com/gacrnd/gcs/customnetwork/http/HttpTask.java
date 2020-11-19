package com.gacrnd.gcs.customnetwork.http;

import com.alibaba.fastjson.JSON;

import java.io.UnsupportedEncodingException;

/**
 * 组装并执行
 *
 * @author Jack_Ou  created on 2020/11/18.
 */
public class HttpTask<T> implements Runnable {

    private IHttpRequst mRequst;

    public HttpTask(String url, T requestData, IHttpListener listener, IHttpRequst mRequst) {
        this.mRequst = mRequst;
        mRequst.setUrl(url);
        mRequst.setListener(listener);
        // 此处可以分情况解析请求参数，有可能是json,xml,protobuf
        if (requestData !=null) {
            String data = JSON.toJSONString(requestData);
            try {
                mRequst.setParams(data.getBytes("utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        mRequst.execute();
    }
}
