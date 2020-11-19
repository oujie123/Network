package com.gacrnd.gcs.customnetwork.http;

/**
 * @author Jack_Ou  created on 2020/11/18.
 */
public class NetFramework {

    public static <T,R> void sendJsonRequest(String url,T requestParams,Class<R> response,IDataListener listener){
        IHttpRequst requst = new JsonHttpRequest();
        IHttpListener httpListener = new JsonHttpListener<>(response,listener);
        HttpTask task = new HttpTask(url,requestParams,httpListener,requst);
        ThreadManager.getInstance().addTask(task);
    }
}
