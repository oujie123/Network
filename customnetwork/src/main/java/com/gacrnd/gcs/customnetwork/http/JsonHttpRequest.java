package com.gacrnd.gcs.customnetwork.http;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Jack_Ou  created on 2020/11/18.
 */
public class JsonHttpRequest implements IHttpRequst {

    private String url;
    private byte[] params;
    private IHttpListener listener;

    @Override
    public void setUrl(String url) {
        this.url=url;
    }

    @Override
    public void setParams(byte[] params) {
        this.params = params;
    }

    /**
     * 真实的网络操作
     *
     * HttpUrlConnection socket okhttp
     */
    @Override
    public void execute() {
        HttpURLConnection httpURLConnection = null;
        URL url = null;
        try {
            url = new URL(this.url);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(6000);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setInstanceFollowRedirects(true);
            httpURLConnection.setReadTimeout(3000);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type","application/json");
            httpURLConnection.connect();

            //使用字节流发送数据
            OutputStream out = httpURLConnection.getOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(out);
            bos.write(params);
            bos.flush();
            bos.close();
            out.close();

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                InputStream in = httpURLConnection.getInputStream();
                listener.onSuccess(in);
            } else {
                throw new RuntimeException("请求失败");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }

    @Override
    public void setListener(IHttpListener listener) {
        this.listener = listener;
    }
}
